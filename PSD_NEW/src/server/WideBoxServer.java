package server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import db.IWideBoxDB;
import db.WideBoxDB;

public class WideBoxServer {

	private static final int WIDEBOXCLIENT_PORT = 5000;
	private static final int WIDEBOXDB_PORT = 5001;
	
	private static final String MY_IP = "10.101.148.96";
	
	private static final String WIDEBOXDB_IP = "10.101.148.96";
	

	public static void main(String[] args) throws Exception {

		new WideBoxServer(args);

	}

	public WideBoxServer(String[] args) {
		System.out.println("Starting the server...");

		IWideBoxDB wideboxDBStub = null;
		IWideBox widebox = null;

		try {
			System.setProperty("java.rmi.server.hostname", MY_IP);
			Registry registry = LocateRegistry.getRegistry(WIDEBOXDB_IP, WIDEBOXDB_PORT);
			wideboxDBStub = (IWideBoxDB) registry.lookup("WideBoxDBServer");
			System.out.println("Connected to DB");
		} catch (RemoteException e) {
			
			System.err.println("Error in getting registry");
			 
		} catch (NotBoundException e) {
			System.err.println("Error in getting the WideBoxDB");
		}

		try {
			widebox = new WideBoxImpl(wideboxDBStub);
			Registry registry = LocateRegistry.createRegistry(WIDEBOXCLIENT_PORT);
			registry.rebind("WideBoxServer", widebox);
		} catch (RemoteException e) {
			
			System.err.println("Error in creating the WideBoxServer registry");
			e.printStackTrace(); 
		} catch (Exception e) {
			System.err.println("Widebox - Error trying to start the server!");
			e.printStackTrace();
		}
	}
}