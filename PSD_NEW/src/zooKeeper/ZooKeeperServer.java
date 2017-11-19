package zooKeeper;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ZooKeeperServer {

	public static void main(String[] args) {
		new ZooKeeperServer(args);
	}
	
	public ZooKeeperServer(String[] args) {
		
		//args[0] ZooKeeperServer IP
		//args[1] ZooKeeperServer Port
		
		System.setProperty("java.rmi.server.hostname", args[0]);
		
		IZKClient zk = null;
		
		try {
			zk = new ZKClient();
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry.rebind("ZooKeeperServer", zk);
			System.out.println("ZooKeeper started!");
		} catch (Exception e) {
			System.err.println("ZooKeeperServer - Error trying to start the ZooKeeperServer!");
			e.printStackTrace();
		}
		
	}

}
