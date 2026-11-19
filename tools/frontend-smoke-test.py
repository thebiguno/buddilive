#!/usr/bin/env python3
"""Render the production frontend in isolated Chrome with an empty-account API fixture."""
import argparse
import functools
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import threading
from urllib.parse import urlsplit


REPO = Path(__file__).resolve().parent.parent


class FixtureHandler(SimpleHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def do_GET(self):
        path = urlsplit(self.path).path
        if path == '/buddilive/':
            body = '''<!doctype html><html><head><title>Buddi Live frontend smoke test</title>
<link rel="stylesheet" href="buddilive-react/assets/index.css"></head><body><div id="root"></div>
<script>
window.BUDDI_CONFIG = {locale: 'en_CA', currency: 'CAD', i18n: {}};
const failures = [];
window.addEventListener('error', event => { failures.push(event.message); });
window.addEventListener('unhandledrejection', event => { failures.push(String(event.reason)); });
setTimeout(() => {
  const root = document.getElementById('root');
  const mobile = /Mobi/.test(navigator.userAgent);
  const expected = mobile ? 'Accounts' : 'My Accounts';
  const rendered = root.children.length > 0 && root.textContent.includes(expected);
  document.documentElement.dataset.smokeResult = rendered && !failures.length ? 'passed' : 'failed';
  document.documentElement.dataset.smokeErrors = JSON.stringify(failures);
}, 5000);
</script><script type="module" src="buddilive-react/assets/index.js"></script></body></html>'''
            self.send_response(200)
            self.send_header('Content-Type', 'text/html; charset=utf-8')
            self.end_headers()
            self.wfile.write(body.encode())
        elif path.startswith('/buddilive/data/') or path.startswith('/buddilive/stores/'):
            payload = {} if path.endswith('/translations') else {'success': True, 'data': [], 'children': []}
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps(payload).encode())
        else:
            self.path = path.removeprefix('/buddilive/buddilive-react')
            super().do_GET()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--chrome', default=os.environ.get('CHROME_BIN'))
    parser.add_argument('--assets', type=Path, default=REPO / 'WebContent/buddilive-react/assets')
    args = parser.parse_args()
    chrome = args.chrome or shutil.which('google-chrome') or shutil.which('chromium')
    if not chrome:
        chrome = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
    assert Path(chrome).is_file(), 'Set CHROME_BIN or pass --chrome with a Chrome/Chromium executable'
    assert (args.assets / 'index.js').is_file(), 'Build the frontend first'
    handler = functools.partial(FixtureHandler, directory=str(args.assets.resolve().parent))
    server = ThreadingHTTPServer(('127.0.0.1', 0), handler)
    worker = threading.Thread(target=server.serve_forever, daemon=True)
    worker.start()
    try:
        with tempfile.TemporaryDirectory(prefix='buddilive-browser-test-') as temporary:
            for mobile in [False, True]:
                mode = 'mobile' if mobile else 'desktop'
                command = [chrome, '--headless', '--disable-gpu', '--no-first-run',
                           '--no-default-browser-check', '--disable-background-networking',
                           '--disable-extensions', f'--user-data-dir={temporary}/{mode}',
                           '--virtual-time-budget=8000', '--dump-dom', '--window-size=1280,900']
                if mobile:
                    command.append('--user-agent=Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/142.0.0.0 Mobile Safari/537.36')
                command.append(f'http://127.0.0.1:{server.server_port}/buddilive/')
                result = subprocess.run(command, capture_output=True, text=True, timeout=45)
                assert result.returncode == 0, result.stderr
                assert 'data-smoke-result="passed"' in result.stdout, result.stdout[:2000] + result.stderr[-3000:]
                print(f'{mode}: production bundle rendered the account screen without uncaught JavaScript errors')
    finally:
        server.shutdown()
        server.server_close()


if __name__ == '__main__':
    main()
