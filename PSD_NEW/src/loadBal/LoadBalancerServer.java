package loadBal;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import zooKeeper.IZKClient;

public class LoadBalancerServer {
	
	public static void main(String[] args) throws Exception {
		new LoadBalancerServer(args);
	}
	
	public LoadBalancerServer(String[] args) {
		
		System.out.println("Starting the load balancer...");
		
		//args[0] - Load Balancer IP
		//args[1] - Load Balancer Port
		//args[2] - ZooKeeper IP
		//args[3] - ZooKeeper Port
		
		try {
			//get the zookeeper client
			Registry registry = LocateRegistry.getRegistry(args[2], 
					Integer.parseInt(args[3]));
			IZKClient zooKeeper = (IZKClient) registry.lookup("ZooKeeperServer");
			System.out.println("Connected to ZooKeeper");
			
			System.setProperty("java.rmi.server.hostname", args[0]);
			ILoadBalancer loadbalancer = new LoadBalancerImpl(zooKeeper);
			registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry.rebind("LoadBalancer", loadbalancer);
		} catch (RemoteException e) {
			System.err.println("Error in getting registry");
		} catch (Exception e) {
			System.err.println("Error starting the server");
			e.printStackTrace();
		}
		
	}

}
