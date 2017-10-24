package db;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WideBoxDBServer {
	
	private static final String MY_IP = "127.0.0.1";
	
	public static void main(String[] args) throws Exception {

		try {
			System.setProperty("java.rmi.server.hostname", MY_IP);
			IWideBoxDB db = new WideBoxDB();
			Registry registry = LocateRegistry.createRegistry(5001);
			registry.rebind("WideBoxDBServer", db);
			System.out.println("DB loaded");
			
			//
		} catch (Exception e) {
			System.err.println("WideBoxDBServer - Error trying to start the server!");
		}
		
		

	}

}
