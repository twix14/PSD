package db;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import utilities.Status;
import zooKeeper.ZKClient;

public class WideBoxDBServer {
		
	public static void main(String[] args) throws Exception {

		IWideBoxDB db = null;
		ZKClient zooKeeper = null;
		
		//args[0] DBServer IP
		//args[1] DBServer port
		//args[2] zooKeeper IP
		//args[3] numberOfTheatres
		//args[4] numberDBs
		
		try {
			System.setProperty("java.rmi.server.hostname", args[0]);
			
			try {
				zooKeeper = new ZKClient(args[2], new LinkedBlockingQueue<WatchedEvent>());
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (KeeperException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.out.println("Connected to ZooKeeper");
			String[] pid =  ManagementFactory.getRuntimeMXBean().getName().split("@");
			int last = zooKeeper.createDBNode(args[0], args[1], Integer.parseInt(args[3]), Integer.parseInt(args[4]),
					pid[0]);
			db = new WideBoxDB(last, (Integer.parseInt(args[3])/Integer.parseInt(args[4])));
			
			Registry registry2 = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry2.rebind("WideBoxDBServer", db);
			System.out.println("DB loaded\n");
			System.out.println("Commands:");
			System.out.println("-->'print db n-theatre' command to print status of a theatre");
			System.out.println("-->'full n-theatre' to full a theatre");
			System.out.println("-->'get n-theatre'");
			System.out.println("-->'put n-theatre n-seat newValue oldValue'");
			
			//
			
		} catch (RemoteException e) {
			System.err.println("Error in getting registry");
			e.printStackTrace();
			 
		}  catch (Exception e) {
			System.err.println("WideBoxDBServer - Error trying  to start the server!");
			e.printStackTrace();
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
