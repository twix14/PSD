package db;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.TreeMap;

import utilities.Status;

public class WideBoxDBServer {
	
	private static final String MY_IP = "192.168.1.229";
	
	public static void main(String[] args) throws Exception {

		IWideBoxDB db = null;
		
		try {
			System.setProperty("java.rmi.server.hostname", MY_IP);
			db = new WideBoxDB();
			Registry registry = LocateRegistry.createRegistry(5001);
			registry.rebind("WideBoxDBServer", db);
			System.out.println("DB loaded\n");
			System.out.println("Commands:");
			System.out.println("-->'print db n-theatre' command to print status of a theatre");
			System.out.println("-->'full n-theatre' to full a theatre");
			System.out.println("-->'get n-theatre'");
			System.out.println("-->'put n-theatre n-seat newValue oldValue'");
			
			//
		} catch (Exception e) {
			System.err.println("WideBoxDBServer - Error trying  to start the server!");
		}
		
		Scanner sc = new Scanner(System.in);
		try {
			while(true) {
				String command = sc.nextLine();
				String[] split = command.split(" ");
				if (split[0].equals("print"))
					db.printStatus(split[2]);
				if(split[0].equals("full"))
					db.fullTheatre(split[1]);
				if(split[0].equals("get"))
					System.out.println(db.get(split[1]));
				if(split[0].equals("put"))
					db.put(split[1], split[2], Status.valueOf(split[3]), Status.valueOf(split[4]));
				if(split[0].equals("sort")) 
					System.out.println(new TreeMap<>(db.listSeats(split[1])));
				
				
			}
		} finally {
			sc.close();
		}
		

	}

}
