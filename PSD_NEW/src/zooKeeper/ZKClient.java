package zooKeeper;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import db.IWideBoxDB;
import javafx.util.Pair;
import loadBal.ILoadBalancer;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;

public class ZKClient {

	private ZooKeeper zk;
	private BlockingQueue<WatchedEvent> events;
	private BlockingQueue<String> queue;

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

				String ip1 = new String(zk.getData(root + "/" + s, false, null));
				String ip = new String(zk.getData(root + "/" + s, new Watcher() {
					//NODE DOWN!
					public void process(WatchedEvent event) {
						try {
							if(event.getType().equals(Watcher.Event.EventType.NodeDeleted))
								queue.put(ip1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}, null));
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

						if(event.getType().equals(Watcher.Event.EventType.NodeChildrenChanged) 
								&& !event.getState().equals(KeeperState.Expired) && 
								!event.getState().equals(KeeperState.Disconnected))
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
							if(event.getType().equals(Watcher.Event.EventType.NodeDeleted))
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

	public int [] createDBNode(String ip, String port, int numberOfTheatres, String pid) {
		int [] res = new int [3];
		try {
			int numberDBs = 1;
			String root = "/DBServers";
			Stat stat = zk.exists(root, false);

			List<String> dbNodes = getAllDBNodes();

			if(!dbNodes.isEmpty()) {
				List<IWideBoxDB> dbServers = new ArrayList<IWideBoxDB>();
				numberDBs = dbNodes.size()+1;
				int numOfTheatresPerDB = numberOfTheatres/numberDBs;
				res[2] = numOfTheatresPerDB;
				int oldNumOfTheatresPerDB = numberOfTheatres/(numberDBs - 1);
				int moving = oldNumOfTheatresPerDB - numOfTheatresPerDB;
				String ipPort = ip + ":" + port +  ":" + pid + ":P"; 
				if(stat != null) {
					String node = zk.create(root + "/server", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
							CreateMode.EPHEMERAL_SEQUENTIAL);

					System.out.println("ZooKeeper created node " + node);
				}

				res[0] = numOfTheatresPerDB * dbNodes.size() + 1;
				res[1] = res[0] + numOfTheatresPerDB - 1;


				int rangeMin = 0;// numOfTheatresPerDB * dbNodes.size() + 1;
				int rangeMax = 0; // rangeMin + numOfTheatresPerDB - 1;

				for(int i = 0; i < dbNodes.size(); i++) {
					String [] split = dbNodes.get(i).split(":");
					try {
						Registry reg = LocateRegistry.getRegistry(split[0], 
								Integer.parseInt(split[1]));

						IWideBoxDB db = (IWideBoxDB) reg.lookup("WideBoxDBServer");
						dbServers.add(db);
					} catch (Exception e) {
						continue;
					} 
				}

				for(int j = 0; j < dbServers.size() - 1; j++) {
					IWideBoxDB db = dbServers.get(j);
					rangeMin = numOfTheatresPerDB * j + 1;
					rangeMax = rangeMin + numOfTheatresPerDB - 1;
					String [] split = dbNodes.get(j+1).split(":");

					db.newRange(numOfTheatresPerDB, rangeMin, rangeMax, split);
				}
				IWideBoxDB db = dbServers.get(dbServers.size()-1);
				rangeMin = numOfTheatresPerDB * (dbServers.size()-1) + 1;
				rangeMax = rangeMin + numOfTheatresPerDB - 1;
				db.newRange(numOfTheatresPerDB, rangeMin, rangeMax,ipPort.split(":"));

			} else {

				String ipPort = ip + ":" + port + ":" + pid + ":P"; 
				//create group membership node
				String node = zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				System.out.println("ZooKeeper created node " + node);
				//creates child node that represents him
				node = zk.create(root + "/server", ipPort.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created node " + node);
				res[0] = 1;
				res[1] = 1500;
				res[2] = 1500;


			}
		} catch (KeeperException | InterruptedException e1) {
			e1.printStackTrace();
			res = null;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	public String[] createSecondaryDBNode(String ip, String port, int numberOfTheatres, String pid) {
		int numberDBs = 1;
		int secCount = 0;

		List<String> dbNodes = getAllDBNodes();

		if(!dbNodes.isEmpty()) {
			numberDBs = dbNodes.size();

		} 
		int numOfTheatresPerDB = numberOfTheatres/numberDBs;
		String res[] = new String[4];
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
					if (ipNew[3].equals("S") && temp < Integer.parseInt(ipNew[2])) {
						secCount ++;
					}
				}

				boolean first = false;

				if (secCount == 0) {
					temp = numOfTheatresPerDB;

					first = true;
				}

				tempNode = getDBNodeNameByOrder(secCount);

				String ipDB = ip + ":" + port + ":" + pid + ":S";

				String node = zk.create(root + "/server", ipDB.getBytes(), Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);
				//3 FASE PASSAR PARA EPHEMERAL, POR ENQUANTO NAO HA HEARTBEATS
				System.out.println("ZooKeeper created Secondary DB node " + node);

				String[] aa = new String(zk.getData(tempNode, false, null)).split(":");
				int rangeMin = numOfTheatresPerDB * secCount + 1;
				res[0] = Integer.toString(rangeMin + numOfTheatresPerDB - 1);
				res[1] = aa[0];
				res[2] = aa[1];
				res[3] = Integer.toString(numOfTheatresPerDB);

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

	public String getDBNodeNameByOrder(int order) {
		String root = "/DBServers";
		int count = 0;
		try {
			List<String> children = zk.getChildren(root, true);
			Collections.sort(children);

			for(String s : children) {
				String ip = new String(zk.getData(root + "/" + s, false, null));
				if (ip.endsWith("P")) {
					if(count == order)
						return root + "/" + s;
					else
						count++;
				}

			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
			new ArrayList<>();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Collections.sort(result);
		return result;
	}

	public List<Pair<String, String>> getAllDBNodesPairs(boolean first) {
		String root = "/DBServers";
		List<Pair<String, String>> result = new ArrayList<>();
		try {
			List<String> children = zk.getChildren(root, new Watcher() {
				//Added children
				public void process(WatchedEvent event) {
					try {
						if(first) {
							if(event.getType().equals(Watcher.Event.EventType.NodeChildrenChanged) 
									&& !event.getState().equals(KeeperState.Expired) && 
									!event.getState().equals(KeeperState.Disconnected))
								queue.put("");
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			Collections.sort(children);
			int count = 0;
			int count2 = 0;
			int i = 0;
			for(String s : children) {
				count2=0;
				final int j = i;
				String ip1 = new String(zk.getData(root + "/" + s,false, null));
				String ip = new String(zk.getData(root + "/" + s,new Watcher() {
					//NODE DOWN!
					public void process(WatchedEvent event) {
						try {
							if(event.getType().equals(Watcher.Event.EventType.NodeDeleted) 
									&& ip1.endsWith("P"))
								queue.put(String.valueOf(j)+":down");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}, null));
				if (ip.endsWith("P")) {

					for(String s1 : children) {
						String ip2 = new String(zk.getData(root + "/" + s1, false, null));
						if(ip2.endsWith("S")  ) {
							if(count == count2) {
								result.add(new Pair<String, String>(ip, ip2));
								break;
							} else
								count2++;
						}

					}
					count ++;

				} 
				i++;

			}

		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return result;
	}

	public BlockingQueue<String> getQueue() {
		return queue;
	}

	public void setQueue(BlockingQueue<String> queue) {
		this.queue = queue;
	}



}
