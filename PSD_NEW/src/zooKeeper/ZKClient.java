package zooKeeper;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;

public class ZKClient extends UnicastRemoteObject implements IZKClient{

	private static final long serialVersionUID = 8762037743612400725L;
	private ZooKeeper zk;

	public ZKClient() throws IOException, KeeperException, InterruptedException{
		zk = new ZooKeeper("", 2000, new Watcher() {
            public void process(WatchedEvent event) {
            }
        });
		//2nd argument session timeout!
	}
	
	//GROUP MEMBERSHIP

	@Override
	public int createAppServerNode(String ip, String port, String pid) throws RemoteException {
		try {
			String root = "/appServers";
			//root node exists 
			Stat stat = zk.exists(root, false);
			String ipPort = ip + ":" + port + ":" + pid; 
			if(stat != null) {
				//creates child node that represents him
				
				String node = zk.create(root + "/server", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created node " + node);
			} else {
				//create group membership node
				String node = zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				System.out.println("ZooKeeper created node " + node);
				//creates child node that represents him
				node = zk.create(root + "/server", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created node " + node);
			}
		} catch (KeeperException e) {
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		} 

		return 0;
	}



	@Override
	public List<String> getAllAppServerNodes() throws RemoteException {
		String root = "/appServers";
		List<String> result = new ArrayList<>();
		try {
			List<String> children = zk.getChildren(root, true);

			for(String s : children) {
				String ip = new String(zk.getData(root + "/" + s, false, null));
				result.add(ip);
				System.out.println("AppServer with IP:Port-" + ip + " is on the group!");
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public int createLBNode(String ip, String port, String pid) {
		try {
			String root = "/loadBalancers";
			//root node exists 
			Stat stat = zk.exists(root, false);
			String ipPort = ip + ":" + port + ":" + pid; 
			if(stat != null) {
				//creates child node that represents him
				
				String node = zk.create(root + "/lb", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper create node " + node);
			} else {
				//create group membership node
				String node = zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				System.out.println("ZooKeeper created node " + node);
				//creates child node that represents him
				node = zk.create(root + "/lb", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created node " + node);
			}
		} catch (KeeperException e) {
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		} 
		
		
		return 0;
	}
	
	public List<String> getAllLBNodes() throws RemoteException {
		String root = "/loadBalancers";
		List<String> result = new ArrayList<>();
		try {
			List<String> children = zk.getChildren(root, true);

			for(String s : children) {
				String ip = new String(zk.getData(root + "/" + s, false, null));
				result.add(ip);
				System.out.println("LoadBalancerServer with IP:Port-" + ip + " is on the group!");
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	@Override
	public int createDBNode(String ip, String port, int numberOfTheatres, int numberDBs, String pid) throws RemoteException {
		int numOfTheatresPerDB = numberOfTheatres/numberDBs;
		int res = 0;
		
		try {
			String root = "/DBServers";
			//root node exists 
			Stat stat = zk.exists(root, false);
			String ipPort = ip + ":" + port + ":" + numOfTheatresPerDB + ":" + pid; 
			if(stat != null) {
				//creates child node that represents him
				
				//get the number of theatre on used
				
				List<String> children = zk.getChildren(root, true);
				String[] split = new String(zk.getData(root + "/" + children.get(children.size()-1), false, null)).split(":");
				
				String ipDB = ip + ":" + port + ":" + (Integer.parseInt(split[2]) + numOfTheatresPerDB) + ":" + pid;
				String node = zk.create(root + "/server", ipDB.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created node " + node);
				res = Integer.parseInt(split[2])+ numOfTheatresPerDB;
			} else {
				//create group membership node
				String node = zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				System.out.println("ZooKeeper created node " + node);
				//creates child node that represents him
				node = zk.create(root + "/server", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created node " + node);
				res = numOfTheatresPerDB;
			}
		} catch (KeeperException e) {
			e.printStackTrace();
			res = -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
			res = -1;
		} 

		return res;
	}
	
	@Override
	public List<String> getAllDBNodes() throws RemoteException {
		String root = "/DBServers";
		List<String> result = new ArrayList<>();
		try {
			List<String> children = zk.getChildren(root, true);

			for(String s : children) {
				String ip = new String(zk.getData(root + "/" + s, false, null));
				result.add(ip);
				System.out.println("DBServer with IP:Port-" + ip + " is on the group!");
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}



}
