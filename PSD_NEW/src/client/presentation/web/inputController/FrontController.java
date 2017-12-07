package client.presentation.web.inputController;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.controller.web.inputController.actions.Action;
import client.controller.web.inputController.actions.ChooseSeatAction;
import client.controller.web.inputController.actions.QueryTheatreAction;
import client.controller.web.inputController.actions.SeatReplyAction;
import client.controller.web.inputController.actions.UnknownAction;
import loadBal.ILoadBalancer;
import server.IWideBox;
import utilities.Cache;
import zooKeeper.IZKClient;


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

	private static final String ZOOKEEPER_PORT = "5003";
	private static final String ZOOKEEPER_IP = "10.101.148.59";

	private static AtomicInteger serialClient;
	public static Cache cache;

	static final String ACTION_PATH = "/action";
	private InitialContext context;
	private static IZKClient zk;
	private static List<ILoadBalancer> lbs;
	private static IWideBox server;

	/**
	 * Maps http actions to the objects that are going to handle them
	 */
	protected HashMap<String, String> actionHandlers;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String actionURL = request.getPathInfo();
		//String actionJNDI = getActionHandlerAddress(ACTION_PATH + actionURL);
		Action actionCommand = null;
		switch (actionURL) {

		case "/QueryTheatre":
			actionCommand = new QueryTheatreAction();
			break;

		case "/seatReply":
			actionCommand = new SeatReplyAction();
			break;

		case "/chooseSeat":
			actionCommand = new ChooseSeatAction();
			break;

		default:
			actionCommand = new UnknownAction();
			break;
		}
		actionCommand.process(request, response);
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public static ILoadBalancer getLoadBalancer() {
		return lbs.get(0);
	}
	
	public static void removeLoadBalancer(){
		lbs.remove(0);
	}

	public static void setServer(String server2) {
		server = cache.get(server2);
	}

	public static IWideBox getServer() {
		return server;
	}

	public InitialContext getInitialContex() {
		return context;
	}

	public static int getId() {
		return serialClient.incrementAndGet();
	}


	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() {
		cache = new Cache();
		actionHandlers = new HashMap<>();
		actionHandlers.put("unknownAction", "java:module/UnknownAction");
		actionHandlers.put("searchTheatres", "java:module/SearchTheatresAction");
		actionHandlers.put("action/QueryTheatre", "java:module/QueryTheatreAction");
		//actionHandlers.put("", value)
		serialClient = new AtomicInteger();
		try {
			context = new InitialContext();
			Registry registry = LocateRegistry.getRegistry(ZOOKEEPER_IP, 
					Integer.parseInt(ZOOKEEPER_PORT));
			zk = (IZKClient) registry.lookup("ZooKeeperServer");

			List<String> lbsZK = zk.getAllLBNodes();
			for(String s : lbsZK){
				String[] split = s.split(":");
				registry = LocateRegistry.getRegistry(split[0],
						Integer.parseInt(split[1]));
				ILoadBalancer server = null;
				try {
					server = (ILoadBalancer) registry.lookup("LoadBalancer");
					lbs.add(server);
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			}

			//actionHandlers.put("WideBoxServer", getWideBoxServer().toString());
			//actionHandlers.put("WideBoxServer", "rmi://" + SERVER_IP +":" + SERVER_PORT	+ "/WideBoxServer" );
		} catch (Exception e) {
			// It was not able to load properties file.
			// Bad luck, all action will be dispatched to the UnknownAction
		}		
	}
}
