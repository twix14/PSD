package db;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class WideBoxDBServer {
	
	private static final String MY_IP = "10.101.148.96";
	
	public static void main(String[] args) throws Exception {

		IWideBoxDB db = null;
		
		try {
			System.setProperty("java.rmi.server.hostname", MY_IP);
			db = new WideBoxDB();
			Registry registry = LocateRegistry.createRegistry(5001);
			registry.rebind("WideBoxDBServer", db);
			System.out.println("DB loaded\n");
			System.out.println("'print db n-theatre' command to print status of a theatre");
			
			
			//
		} catch (Exception e) {
			System.err.println("WideBoxDBServer - Error trying  to start the server!");
		}
		
		Scanner sc = new Scanner(System.in);
		while(true) {
			String command = sc.nextLine();
			String[] split = command.split(" ");
			db.printStatus(split[2]);
		}
		

	}

}
