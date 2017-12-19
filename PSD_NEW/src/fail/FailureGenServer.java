package fail;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import zooKeeper.ZKClient;

public class FailureGenServer {
	
	public static void main(String[] args) throws Exception {
		new FailureGenServer(args);
	}
	
	public FailureGenServer(String[] args) {
		
		System.out.println("Starting the load balancer...");
		
		//args[0] - failureGenerator IP
		//args[1] - failureGenerator Port
		//args[2] - ZooKeeper IP
		
		try {
			ZKClient zooKeeper = null;
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
			
			System.setProperty("java.rmi.server.hostname", args[0]);
			FailureGen fg = new FailureGen(zooKeeper);
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry.rebind("FailureGenerator", fg);
		} catch (RemoteException e) {
			System.err.println("Error in getting registry");
		} catch (Exception e) {
			System.err.println("Error starting the server");
			e.printStackTrace();
		}
		
	}

}
