package loadBal;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import server.IWideBox;
import server.Message;
import zooKeeper.ZKClient;

public class LoadBalancerImpl  extends UnicastRemoteObject implements ILoadBalancer{

	private static final long serialVersionUID = 8376276164490217884L;

	//Queue for incoming messages, 2000 max messages at a time
	private BlockingQueue<Message> messages;

	//List with starting servers
	private List<IWideBox> servers;
	private List<String> serversClient;
	//Lock for round robin
	int roundRobin;
	
	private AtomicInteger primary;

	//Maximum operations with the current available app servers
	private int startMax;

	private ZKClient zooKeeper;

	public LoadBalancerImpl(ZKClient zooKeeper, BlockingQueue<String> events) throws RemoteException {
		this.zooKeeper = zooKeeper;

		//get all ips and remote appserver objects
		List<String> ips = this.zooKeeper.getAllAppServerNodes();
		servers = new LinkedList<>();
		serversClient = new LinkedList<>();
		primary = new AtomicInteger(0);
		roundRobin = 0;
		
		/*
		 * The Round Robin algorithm is best for clusters 
		 * consisting of servers with identical specs
		 */
		for(String ip: ips) {
			String[] split = ip.split(":");
			Registry registry = LocateRegistry.getRegistry(split[0],
					Integer.parseInt(split[1]));
			IWideBox server = null;
			try {
				server = (IWideBox) registry.lookup("WideBoxServer");
				servers.add(server);
				serversClient.add(ip);
			} catch (NotBoundException e) {
				System.err.println("Problem connecting with AppServer"
						+ "with Ip:Port-" + ip);
				e.printStackTrace();
			}
			System.out.println("Connected to AppServer with Ip:Port-"
					+ ip);
		}
		this.messages = new LinkedBlockingQueue<>(ips.size()*1000);
		startMax = ips.size()*1000;
		
		ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
		
		Runnable task = () -> {
			while(true) {
				if(!events.isEmpty()) {
					String we = null;
					try {
						we = events.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					//AppServer offline
					System.out.println("App Server died, removing it from appServers list...");
					int index = serversClient.indexOf(we);
					serversClient.remove(index);
					servers.remove(index);
				}
			}
		};

		es.execute(task);

		System.out.println("Ready to go!");
	}

	@Override
	public Message requestSearch() throws RemoteException {
		
		if(primary.get() == 0) {
			System.out.println("I'm the primary!");
			primary.incrementAndGet();
		}

		if(messages.size() >= startMax)
			return new Message(Message.BUSY);
		else 
			messages.add(new Message("Request"));

		//change next server to be assigned
		roundRobin++;
		
		int serv = roundRobin % servers.size();
		String s = serversClient.get(serv);

		//dispatch request to next server using round robin!
		Message result = null;
		try {
			IWideBox server = servers.get(serv);
			
			result = server.search();
			while(true) {
				if(result.getStatus().equals("Retry"))
					result = server.search();
				else
					break;
			}
			
			result.setServer(serversClient.get(serv));
		} catch (IndexOutOfBoundsException e) {
			return requestSearch();
		} catch (RemoteException e) {
			//server offline
			if(serversClient.contains(s)) {
				System.out.println("AppServer is offline, trying another one");
				servers.remove(serv);
				serversClient.remove(serv);
			}
			
			return requestSearch();
		}

		//is it important to get the exact same message that you placed?????
		//to be tested!!!!
		try {
			messages.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public Message requestSeatAvailable(int clientId, String theatre) throws RemoteException {
		if(primary.get() == 0) {
			System.out.println("I'm the primary!");
			primary.incrementAndGet();
		}
		
		if(messages.size() >= startMax)
			return new Message(Message.BUSY);
		else 
			messages.add(new Message("Request"));

		//change next server to be assigned
		roundRobin++;
		
		int serv = roundRobin % servers.size();
		String s = serversClient.get(serv);
		
		//dispatch request to next server using round robin!
		Message result = null;
		try {
			IWideBox server = servers.get(serv);
			
			result = server.seatsAvailable(clientId, theatre);
			while(true){
				if(result.getStatus().equals("Retry"))
					result = server.seatsAvailable(clientId, theatre);
				else
					break;
			}
			
		
			//ADD SERVER IP OR IWIDEBOX SO THE CLIENT CAN KNOW WHO HANDLES ITS REQUEST
			result.setServer(serversClient.get(serv));
		} catch (IndexOutOfBoundsException e) {
			return requestSeatAvailable(clientId, theatre);
		} catch (RemoteException e) {
			//server offline
			if(serversClient.contains(s)) {
				System.out.println("AppServer is offline, trying another one");
				servers.remove(serv);
				serversClient.remove(serv);
			}
			
			return requestSeatAvailable(clientId, theatre);
		}
		
		//is it important to get the exact same message that you placed?????
		//to be tested!!!!
		try {
			messages.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public void addServer(String server2) {
		if(!serversClient.contains(server2)) {
			String[] split = server2.split(":");
			try {
				Registry registry = LocateRegistry.getRegistry(split[0],
					Integer.parseInt(split[1]));
				IWideBox server = (IWideBox) registry.lookup("WideBoxServer");
				servers.add(server);
				serversClient.add(server2);
			} catch (Exception e) {
				System.err.println("Problem connecting with AppServer"
						+ "with Ip:Port-" + server2);
				e.printStackTrace();
			}
			System.out.println("Added AppServer with Ip:Port-"
					+ server2);
		}
	}
}
