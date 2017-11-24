package server;

import java.lang.management.ManagementFactory;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import db.IWideBoxDB;
import zooKeeper.IZKClient;

public class WideBoxServer {

	public static void main(String[] args) throws Exception {

		new WideBoxServer(args);

	}

	public WideBoxServer(String[] args) {
		System.out.println("Starting the server...");

		IWideBox widebox = null;
		IZKClient zooKeeper = null;
		
		//args[0] appServer IP
		//args[1] appServer port
		//args[2] zooKeeper IP
		//args[3] zooKeeper Port
		
		System.setProperty("java.rmi.server.hostname", args[0]);

		try {
			Registry registry = LocateRegistry.getRegistry(args[2], 
					Integer.parseInt(args[3]));
			zooKeeper = (IZKClient) registry.lookup("ZooKeeperServer");
			System.out.println("Connected to ZooKeeper");
			
			widebox = new WideBoxImpl(zooKeeper);
			registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry.rebind("WideBoxServer", widebox);
			String[] pid =  ManagementFactory.getRuntimeMXBean().getName().split("@");
			zooKeeper.createAppServerNode(args[0], args[1], pid[0]);
			
		} catch (RemoteException e) {
			
			System.err.println("Error in creating the WideBoxServer registry");
			e.printStackTrace(); 
		} catch (Exception e) {
			System.err.println("Widebox - Error trying to start the server!");
			e.printStackTrace();
		}
	}
}