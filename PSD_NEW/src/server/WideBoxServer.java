package server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import db.IWideBoxDB;
import zooKeeper.IZKClient;

public class WideBoxServer {

	private static final int WIDEBOXCLIENT_PORT = 5000;
	private static final int WIDEBOXDB_PORT = 5001;
	
	private static final String MY_IP = "127.0.0.1";
	
	private static final String WIDEBOXDB_IP = "127.0.0.1";
	

	public static void main(String[] args) throws Exception {

		new WideBoxServer(args);

	}

	public WideBoxServer(String[] args) {
		System.out.println("Starting the server...");

		IWideBoxDB wideboxDBStub = null;
		IWideBox widebox = null;
		IZKClient zooKeeper = null;
		
		//args[0] appServer IP
		//args[1] appServer port
		//args[2] dbServer IP, change this for multiple db servers, probably ZooKeeper
		//args[3] dbServer Port
		//args[4] zooKeeper IP
		//args[5] zooKeeper Port
		
		System.setProperty("java.rmi.server.hostname", args[0]);

		try {
			Registry registry = LocateRegistry.getRegistry(args[4], 
					Integer.parseInt(args[5]));
			zooKeeper = (IZKClient) registry.lookup("ZooKeeperServer");
			System.out.println("Connected to ZooKeeper");
			
			
			registry = LocateRegistry.getRegistry(args[2], 
					Integer.parseInt(args[3]));
			wideboxDBStub = (IWideBoxDB) registry.lookup("WideBoxDBServer");
			System.out.println("Connected to DB");
		} catch (RemoteException e) {
			
			System.err.println("Error in getting registry");
			 
		} catch (NotBoundException e) {
			System.err.println("Error in getting the WideBoxDB or ZKClient");
		}

		try {
			widebox = new WideBoxImpl(wideboxDBStub);
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry.rebind("WideBoxServer", widebox);
			zooKeeper.createAppServerNode(args[0], args[1]);
		} catch (RemoteException e) {
			
			System.err.println("Error in creating the WideBoxServer registry");
			e.printStackTrace(); 
		} catch (Exception e) {
			System.err.println("Widebox - Error trying to start the server!");
			e.printStackTrace();
		}
	}
}