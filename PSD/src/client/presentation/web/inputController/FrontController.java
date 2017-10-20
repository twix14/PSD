package client.presentation.web.inputController;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.controller.web.inputController.actions.Action;
import client.controller.web.inputController.actions.UnknownAction;
import server.WideBoxServer;


/**
 * An input controller that handles the requests of a use case.
 * Think of objects of this class as stateless use case controllers.
 *  
 * HTTP requests are dispatched to this class based on an URL of
 * the form: server-address:port/AppROOT/useCaseHandler/action
 * See subclass annotations @WebServlet an notice the * in the URL
 * schema. 
 * 
 * A request is handled generically by using the action in the URL,
 * obtained using request.getPathInfo(), and dispatching it to its
 * action object (using the command pattern). The action object is
 * taken from a map defined by each specific use case handler (using
 * the template method pattern. See the init method). 
 *
 * The map follows the prototype pattern for holding the classes 
 * (prototypes) for creating objects that handle each client request 
 * for a specific action. We need to create new objects, since objects
 * of this class may have multiple threads (each processing a
 * different request) and it will cause race conditions in the
 * attributes of the requests. Imagine that a request is being
 * processed by a NewCustomerAction, and a new NewCustomerAction
 * arrives. Which request is going to be used in the NewCustomerAction
 * object? To avoid this, we create new objects to handle each
 * request.
 *
 * Bare in mind that multiple threads of objects of this class
 * may be created to handle simultaneous requests, so you must
 * be aware of race conditions. Notice that the only instance
 * attribute is used for reading as doGets or doPosts happen, 
 * but it is only written in the init method, which according
 * to the servlet life cycle is only called once at servlet
 * instantiation. Vide http://download.oracle.com/javaee/6/api/
 * javax/servlet/GenericServlet.html#init(javax.servlet.ServletConfig)
 * 
 * GoF Patterns used: Command, Template method, Prototype
 * 
 * @author fmartins
 *
 */
@WebServlet(FrontController.ACTION_PATH + "/*")
public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final int SERVER_PORT = 1616;
	private static final int SERVER_IP = 1616;
	
	static final String ACTION_PATH = "/action";
	private InitialContext context;
	/**
	 * Maps http actions to the objects that are going to handle them
	 */
	protected HashMap<String, String> actionHandlers;
	
	private Properties appProperties;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String actionURL = request.getPathInfo();
		String actionJNDI = getActionHandlerAddress(ACTION_PATH + actionURL);
		Action actionCommand;
		try {
			actionCommand = (Action) context.lookup(actionJNDI);
		} catch (NamingException e) {
			actionCommand = new UnknownAction();
		}
		actionCommand.process(request, response);
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}


	/**
	 * @param action The http request action.
	 * @return the action associated with the http request.
	 * In case no action is defined, the unknown action is
	 * returns.
	 */
	private String getActionHandlerAddress(String action) {
		String result = actionHandlers.get(action);
		if (result == null)
			result = actionHandlers.get("unknownAction");
		return result;
	}
	
	protected WideBoxServer getWideBoxServer() {
		try {
			Registry registry = LocateRegistry.getRegistry("WideBoxServer", SERVER_PORT);
			return (WideBoxServer) registry.lookup("WideBoxServer");
		} catch (Exception e) {
			System.err.println("Error in Servlet");
			return null;
		}
	}
	
	public InitialContext getInitialContex() {
		return context;
	}

	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() {
		String propertiesFileName = "/WEB-INF/app.properties";
		actionHandlers = new HashMap<>();
		actionHandlers.put("unknownAction", "java:module/UnknownAction");
		appProperties = new Properties();
		try {
			context = new InitialContext();
			appProperties.load(getClass().getResourceAsStream(propertiesFileName)); 
			for (Entry<Object, Object> keyValue : appProperties.entrySet()) {
				if (keyValue.getKey() instanceof String) {
					String key = (String) keyValue.getKey();
					if(key.startsWith("appRoot"))
						actionHandlers.put(key.substring(7), (String) keyValue.getValue());
				}
			}
		
		//actionHandlers.put("WideBoxServer", getWideBoxServer().toString());
		actionHandlers.put("WideBoxServer", "rmi://" + Integer.toString(SERVER_IP) +":" + Integer.toString(SERVER_PORT) 
																						+ "/WideBoxServer" );
		} catch (Exception e) {
			// It was not able to load properties file.
			// Bad luck, all action will be dispatched to the UnknownAction
		}		
	}
}
