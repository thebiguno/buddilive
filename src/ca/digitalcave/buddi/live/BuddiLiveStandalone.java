package ca.digitalcave.buddi.live;

import java.io.File;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.webapp.WebAppContext;

public class BuddiLiveStandalone {

	/**
	 * Start the embedded Jetty server for testing / standalone use.
	 */
	public static void main(String[] args) throws Exception {
		final Server server = new Server(Integer.getInteger("buddilive.port", 8686));
		final URL warUrl = new File(System.getProperty("buddilive.webRoot", "WebContent")).toURI().toURL();
		final WebAppContext context = new WebAppContext();
		context.setWar(warUrl.toExternalForm());
		context.setContextPath("/buddilive");
		
		// Eclipse and Ant already put the application and WEB-INF/lib on the
		// launcher's classpath. Share those classes instead of loading Restlet
		// again from the web root with a different class identity.
		context.setParentLoaderPriority(true);
		server.setHandler(context);
		
		server.start();
		server.join();
	}
}
