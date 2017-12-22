package server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import db.IWideBoxDB;
import zooKeeper.ZKClient;

public class WideBoxServer {

	public static void main(String[] args) throws Exception {

		new WideBoxServer(args);

	}

	public WideBoxServer(String[] args) {
		System.out.println("Starting the server...");

		IWideBox widebox = null;
		
		//args[0] appServer IP
		//args[1] appServer port
		//args[2] zooKeeper IP
		
		System.setProperty("java.rmi.server.hostname", args[0]);

		try {
			ZKClient zooKeeper = null;
			BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
			try {
				zooKeeper = new ZKClient(args[2], new LinkedBlockingQueue<WatchedEvent>());
				zooKeeper.setQueue(queue);
				System.out.println("Connected to ZooKeeper");
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (KeeperException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			widebox = new WideBoxImpl(zooKeeper, queue);
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
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