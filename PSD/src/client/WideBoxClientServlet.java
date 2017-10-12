package client;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import server.WideBoxServer;

/**
 * Servlet implementation class WideBoxClientServlet
 */
@WebServlet("/LoginCheck")
public class WideBoxClientServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	WideBoxServer widebox;

	protected WideBoxServer getWideBoxServer() {
		try {
			Registry registry = LocateRegistry.getRegistry(getRegistryHost(), getRegistryPort());
			return (WideBoxServer) registry.lookup(getRegistryName());
		} catch (Exception e) {
			System.err.println("Error in Servlet");
			return null;
		}
	}
	
	private String getRegistryName() {
	    String name = getInitParameter("registryName");
	    return (name == null ? "WideBoxServer" : name);
	  }

	  private String getRegistryHost() {
	    return getInitParameter("registryHost");
	  }

	  private int getRegistryPort() {
	    try { return Integer.parseInt(getInitParameter("registryPort")); }
	    catch (NumberFormatException e) { return Registry.REGISTRY_PORT; }
	  }

	/**
     * @see HttpServlet#HttpServlet()
     */
    public WideBoxClientServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
	    // req .... REQUEST

	    // Get a daytime object if we haven't before
	    if (widebox == null) {
	      widebox = getWideBoxServer();
	      if (widebox == null)
	        System.err.println("Unavailable");
	    }

	    // widebox.cenas() ....
	   
	}
}
