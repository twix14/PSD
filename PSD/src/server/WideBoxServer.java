package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WideBoxServer {
	
	private static final int WIDEBOX_PORT = 1616;

	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting the server...");
		IWideBox widebox = new WideBoxImpl();
		
		Registry registry = null;
		
		try {
			registry = LocateRegistry.createRegistry(WIDEBOX_PORT);
			registry.rebind("WideBoxServer", widebox);
		} catch (Exception e) {
			System.err.println("Widebox - Error trying to start the server!");
		}
	}

}
