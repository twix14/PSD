package fail;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import zooKeeper.IZKClient;

public class FailureGenServer {
	
	public static void main(String[] args) throws Exception {
		new FailureGenServer(args);
	}
	
	public FailureGenServer(String[] args) {
		
		System.out.println("Starting the load balancer...");
		
		//args[0] - failureGenerator IP
		//args[1] - failureGenerator Port
		//args[2] - ZooKeeper IP
		//args[3] - ZooKeeper Port
		
		try {
			//get the zookeeper client
			Registry registry = LocateRegistry.getRegistry(args[2], 
					Integer.parseInt(args[3]));
			IZKClient zooKeeper = (IZKClient) registry.lookup("ZooKeeperServer");
			System.out.println("Connected to ZooKeeper");
			
			System.setProperty("java.rmi.server.hostname", args[0]);
			FailureGen fg = new FailureGen(zooKeeper);
			registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry.rebind("FailureGenerator", fg);
		} catch (RemoteException e) {
			System.err.println("Error in getting registry");
		} catch (Exception e) {
			System.err.println("Error starting the server");
			e.printStackTrace();
		}
		
	}

}
