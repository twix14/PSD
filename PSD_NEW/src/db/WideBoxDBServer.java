package db;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WideBoxDBServer {
	
	public static void main(String[] args) throws Exception {

		try {
			IWideBoxDB db = new WideBoxDB();
			Registry registry = LocateRegistry.createRegistry(5001);
			registry.rebind("WideBoxDBServer", db);
			//
		} catch (Exception e) {
			System.err.println("WideBoxDBServer - Error trying to start the server!");
		}
		
		

	}

}
