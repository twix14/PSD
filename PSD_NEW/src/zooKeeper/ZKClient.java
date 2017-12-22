package zooKeeper;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import javafx.util.Pair;
import loadBal.ILoadBalancer;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;

public class ZKClient {

	private ZooKeeper zk;
	private BlockingQueue<WatchedEvent> events;

	public ZKClient(String ip, BlockingQueue<WatchedEvent> events) throws IOException, KeeperException, InterruptedException{
		zk = new ZooKeeper(ip+":2181", 4000, new Watcher() {
			public void process(WatchedEvent event) {
			}
		});
		//2nd argument session timeout!
		this.events = events;
	}

	//GROUP MEMBERSHIP

	public int createAppServerNode(String ip, String port, String pid) {
		try {
			String root = "/appServers";
			//root node exists 
			Stat stat = zk.exists(root, false);
			String ipPort = ip + ":" + port + ":" + pid; 
			if(stat != null) {
				//creates child node that represents him

				String node = zk.create(root + "/server", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created node " + node);
			} else {
				//create group membership node
				String node = zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				System.out.println("ZooKeeper created node " + node);
				//creates child node that represents him
				node = zk.create(root + "/server", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created node " + node);
			}
			List<String> lbNodes = getAllLBNodes();
			if(!lbNodes.isEmpty()) {
				for(int i = 0; i < lbNodes.size(); i++) {
					String [] split = lbNodes.get(i).split(":");
					try {
						Registry reg = LocateRegistry.getRegistry(split[0], 
								Integer.parseInt(split[1]));

						ILoadBalancer lb = (ILoadBalancer) reg.lookup("LoadBalancer");
						lb.addServer(ipPort);
					} catch (Exception e) {
						continue;
					} 
				}
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

	public List<String> getAllAppServerNodes() {
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
						CreateMode.EPHEMERAL_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper create node " + node);
			} else {
				//create group membership node
				String node = zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				System.out.println("ZooKeeper created node " + node);
				//creates child node that represents him
				node = zk.create(root + "/lb", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);
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

	public List<String> getAllLBNodes() {
		String root = "/loadBalancers";
		List<String> result = new ArrayList<>();
		try {
			List<String> children = zk.getChildren(root,  new Watcher() {
				//Added children
				public void process(WatchedEvent event) {
					try {
						//event can only be nodeChildrenChanged
						events.put(event);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});

			for(String s : children) {
				String ip = new String(zk.getData(root + "/" + s,  new Watcher() {
					//NODE DOWN!
					public void process(WatchedEvent event) {
						try {
							events.put(event);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}, null));
				result.add(ip);
				System.out.println("LoadBalancerServer with IP:Port-" + ip + " is on the group!");
			}
		} catch (KeeperException e) {
			new ArrayList<>();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}

	public int createDBNode(String ip, String port, int numberOfTheatres, int numberDBs, String pid) {
		int numOfTheatresPerDB = numberOfTheatres/numberDBs;
		int res = 0;

		try {
			String root = "/DBServers";
			//root node exists 
			Stat stat = zk.exists(root, false);
			String ipPort = ip + ":" + port + ":" + numOfTheatresPerDB + ":" + pid + ":P"; 
			if(stat != null) {
				//creates child node that represents him

				//get the number of theatre on used

				List<String> children = zk.getChildren(root, true);
				Collections.sort(children);

				String[] split = new String(zk.getData(root + "/" + children.get(children.size()-1), false, null)).split(":");

				String ipDB = ip + ":" + port + ":" + (Integer.parseInt(split[2]) + numOfTheatresPerDB) + ":" + pid + ":P";
				String node = zk.create(root + "/server", ipDB.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);
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
						CreateMode.EPHEMERAL_SEQUENTIAL);
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

	public String[] createSecondaryDBNode(String ip, String port, int numberOfTheatres, int numberDBs, String pid) {
		int numOfTheatresPerDB = numberOfTheatres/numberDBs;
		String res[] = new String[3];
		int temp = 0;
		String tempNode = null;
		String[] ipNew = null;

		try {
			String root = "/DBServers";
			//root node exists 
			Stat stat = zk.exists(root, false);
			if(stat != null) {
				//creates child node that represents him

				//get the number of theatre on used

				List<String> children = zk.getChildren(root, true);
				Collections.sort(children);

				for (String s: children) {
					ipNew = new String(zk.getData(root + "/" + s, false, null)).split(":");
					if (ipNew[4].equals("S") && temp < Integer.parseInt(ipNew[2])) {
						temp = Integer.parseInt(ipNew[2]);
					}
				}

				boolean first = false;

				if (temp == 0) {
					temp = numOfTheatresPerDB;
					
					first = true;
				}
				
				tempNode = getDBNodeNameByData(first? temp : temp + numOfTheatresPerDB);

				String ipDB = ip + ":" + port + ":" + (first? temp : temp + numOfTheatresPerDB) + ":" + pid + ":S";

				String node = zk.create(root + "/server", ipDB.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created Secondary DB node " + node);

				String[] aa = new String(zk.getData(tempNode, false, null)).split(":");
				res[0] = Integer.toString((first? temp : temp + numOfTheatresPerDB));
				res[1] = aa[0];
				res[2] = aa[1];

			}
		} catch (KeeperException e) {
			e.printStackTrace();
			res = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			res = null;
		}

		return res;
	}

	public String getDBNodeNameByData(int temp) {
		String root = "/DBServers";
		List<String> result = new ArrayList<>();
		try {
			List<String> children = zk.getChildren(root, true);

			for(String s : children) {
				String ip = new String(zk.getData(root + "/" + s, false, null));
				if (ip.endsWith("P") && ip.contains(String.valueOf(temp))) {
					return root + "/" + s;
				}
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Collections.sort(result);
		return "";
	}

	public List<String> getAllDBNodes() {
		String root = "/DBServers";
		List<String> result = new ArrayList<>();
		try {
			List<String> children = zk.getChildren(root, true);

			for(String s : children) {
				String ip = new String(zk.getData(root + "/" + s, false, null));
				if (ip.endsWith("P")) {
					result.add(ip);
					System.out.println("DBServer with IP:Port-" + ip + " is on the group!");
				}
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Collections.sort(result);
		return result;
	}
	
	public List<Pair<String, String>> getAllDBNodesPairs() {
		String root = "/DBServers";
		List<Pair<String, String>> result = new ArrayList<>();
		try {
			List<String> children = zk.getChildren(root, true);
			Collections.sort(children);

			for(String s : children) {
				String ip = new String(zk.getData(root + "/" + s, false, null));
				String[] split1 = ip.split(":");
				if (ip.endsWith("P")) {
					for(String s1 : children) {
						String ip2 = new String(zk.getData(root + "/" + s1, false, null));
						String[] split2 = ip.split(":");
						if(ip2.endsWith("S") && split1[2].equals(split2[2]) ) {
							result.add(new Pair<String, String>(ip, ip2));
							break;
						}
					}
					
				} 
			}
			
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}



}
