#!/usr/bin/env python3
"""Check local/deployment startup against an isolated Derby database, twice."""
import argparse
import json
import os
from pathlib import Path
import shutil
import socket
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
import zipfile


def check_startup(repo, java, web, database, log_path, existing, packaged):
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        port = sock.getsockname()[1]
    config = web / 'WEB-INF/classes/config.properties'
    config.parent.mkdir(parents=True, exist_ok=True)
    config.write_text('db.driver=org.apache.derby.jdbc.EmbeddedDriver\n'
                      f'db.url=jdbc:derby:{database}' + ('' if existing else ';create=true') +
                      '\ndb.query=VALUES 1\n')
    if packaged:
        # A deployed WAR must supply its own application classes and libraries.
        launcher = database.parent / 'launcher'
        class_file = Path('ca/digitalcave/buddi/live/BuddiLiveStandalone.class')
        (launcher / class_file).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(repo / 'build/temp/bin' / class_file, launcher / class_file)
        entries = [str(launcher), 'lib/*']
        entries.extend(str(jar) for jar in (web / 'WEB-INF/lib').glob('slf4j-*.jar'))
    else:
        # Eclipse keeps compiled application classes outside WEB-INF/classes,
        # and also puts WEB-INF/lib on the launcher's classpath.
        # Include libraries contributed by referenced Eclipse projects, in
        # classpath order, so stale sibling dependencies cannot escape testing.
        referenced_libraries = []
        moss_classes = None
        for entry in ET.parse(repo / '.classpath').getroot().findall('classpathentry'):
            project_path = entry.get('path', '')
            if entry.get('kind') == 'src' and project_path.startswith('/'):
                project = repo.parent / project_path.lstrip('/')
                classes = project / 'build/classes'
                assert classes.is_dir(), f'Build the referenced project first: {project}'
                referenced_libraries.extend([str(classes), str(project / 'src')])
                if project.name == 'ca.digitalcave.moss.restlet':
                    moss_classes = classes
                for library in ET.parse(project / '.classpath').getroot().findall('classpathentry'):
                    if library.get('kind') == 'lib':
                        referenced_libraries.append(str(project / library.get('path')))
        entries = ['build/temp/bin', 'src', str(web / 'WEB-INF/classes'),
                   *referenced_libraries,
                   'lib/*', 'WebContent/WEB-INF/lib/*']
    classpath = os.pathsep.join(entries)
    command = [java, f'-Dbuddilive.port={port}', f'-Dbuddilive.webRoot={web}',
               f'-Xlog:class+load=info:file={log_path}.classes',
               f'-Dderby.stream.error.file={database.parent / "derby.log"}', '-cp', classpath,
               'ca.digitalcave.buddi.live.BuddiLiveStandalone']
    with log_path.open('w') as log:
        process = subprocess.Popen(command, cwd=repo, stdout=log, stderr=log)
        try:
            base_url = f'http://localhost:{port}/buddilive/'
            deadline = time.monotonic() + 45
            while True:
                try:
                    with urllib.request.urlopen(base_url, timeout=30) as response:
                        body = response.read().decode('utf-8')
                        assert response.status == 200 and '<title>Buddi Live</title>' in body, body
                    break
                except urllib.error.HTTPError:
                    raise
                except urllib.error.URLError:
                    if process.poll() is not None or time.monotonic() >= deadline:
                        raise RuntimeError('Jetty did not become ready')
                    time.sleep(0.25)
            request = urllib.request.Request(base_url + 'index', method='DELETE',
                                             headers={'Accept': 'application/json'})
            try:
                urllib.request.urlopen(request, timeout=10)
                raise AssertionError('Expected a JSON 405 response')
            except urllib.error.HTTPError as error:
                assert error.code == 405, error.code
                assert json.loads(error.read())['success'] is False
        finally:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()
    output = log_path.read_text()
    assert 'NoClassDefFoundError' not in output, output
    assert 'Command execution complete' in output, output
    if not packaged:
        assert moss_classes is not None, 'Expected the Eclipse Moss project dependency'
        class_log = Path(str(log_path) + '.classes').read_text()
        expected = 'ca.digitalcave.moss.restlet.CookieAuthenticator source: file:'
        assert any(expected in line and str(moss_classes.resolve()) in line
                   for line in class_log.splitlines()), 'Moss was not loaded from the compiled source project'
    if existing:
        assert 'no changesets to execute' in output, output
    else:
        assert 'Update has been successful' in output, output
    print(('Existing' if existing else 'Fresh') + ' database: migrations, rendered HTML, JSON 405 passed')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--war', type=Path, help='Test a converted WAR instead of WebContent')
    args = parser.parse_args()
    repo = Path(__file__).resolve().parent.parent
    java = str(Path(os.environ['JAVA_HOME']) / 'bin/java') if os.environ.get('JAVA_HOME') else 'java'
    assert (repo / 'build/temp/bin').is_dir(), 'Run ant jar first'
    with tempfile.TemporaryDirectory(prefix='buddilive-smoke-') as temporary:
        temp = Path(temporary)
        web = temp / 'web'
        if args.war:
            with zipfile.ZipFile(args.war.resolve()) as archive:
                archive.extractall(web)
        else:
            shutil.copytree(repo / 'WebContent/WEB-INF', web / 'WEB-INF')
            shutil.copy2(repo / 'WebContent/index.html', web / 'index.html')
            # Keep compiled classes outside the web root, as in an Eclipse launch.
            for class_file in (web / 'WEB-INF/classes').rglob('*.class'):
                class_file.unlink()
        try:
            for existing in [False, True]:
                check_startup(repo, java, web, temp / 'database',
                              temp / ('restart.log' if existing else 'startup.log'), existing,
                              packaged=bool(args.war))
        except Exception:
            for log in temp.glob('*.log'):
                print(f'--- {log.name} ---\n{log.read_text()}')
            raise


if __name__ == '__main__':
    main()
