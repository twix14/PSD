package loadBal;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import zooKeeper.ZKClient;

public class LoadBalancerServer {
	
	public static void main(String[] args) throws Exception {
		new LoadBalancerServer(args);
	}
	
	public LoadBalancerServer(String[] args) {
		
		System.out.println("Starting the load balancer...");
		
		//args[0] - Load Balancer IP
		//args[1] - Load Balancer Port
		//args[2] - ZooKeeper IP
		
		try {
			//get the zookeeper client
			ZKClient zooKeeper = null;
			try {
				zooKeeper = new ZKClient(args[2], new LinkedBlockingQueue<WatchedEvent>());
				System.out.println("Connected to ZooKeeper");
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (KeeperException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			String[] pid =  ManagementFactory.getRuntimeMXBean().getName().split("@");
			zooKeeper.createLBNode(args[0], args[1], pid[0]);
			
			System.setProperty("java.rmi.server.hostname", args[0]);
			ILoadBalancer loadbalancer = new LoadBalancerImpl(zooKeeper);
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry.rebind("LoadBalancer", loadbalancer);
		} catch (RemoteException e) {
			System.err.println("Error in getting registry");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error starting the server");
			e.printStackTrace();
		}
		
	}

}
