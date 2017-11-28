package fail;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import zooKeeper.IZKClient;

public class FailureGen extends UnicastRemoteObject implements IFailureGen {

	private static final long serialVersionUID = -3899176830026285501L;
	private IZKClient zk;

	public FailureGen(IZKClient zooKeeper) throws RemoteException{
		zk = zooKeeper;
	}

	@Override
	public void crash(String type, String ip, int port) throws RemoteException {
		if(type.equals("LoadBalancer")) {
			//crash loadBalancer
			List<String> lbs = zk.getAllLBNodes();
			String[] split = lbs.get(0).split(":");
			String cmd = "powershell.exe kill " +  split[2];
			try {
					Runtime.getRuntime().exec(cmd);
					System.out.println("LoadBalancer killed");
			} catch (IOException e) {
					e.printStackTrace();
			}
			
		} else if(type.equals("WideBoxServer")) {
			//crash appServer
			List<String> appServers = zk.getAllAppServerNodes();
			for(int i = 0; i < appServers.size(); i++) {
				String[] split = appServers.get(i).split(":");
				if(split[0].equals(ip) && split[1].equals(String.valueOf(port))) {
					String cmd = "powershell.exe kill " +  split[2];
					try {
							Runtime.getRuntime().exec(cmd);
							System.out.println("AppServer killed");
					} catch (IOException e) {
							e.printStackTrace();
					}
					break;
				}
					
			}
		} else {
			//crash db
			List<String> appServers = zk.getAllDBNodes();
			for(int i = 0; i < appServers.size(); i++) {
				String[] split = appServers.get(i).split(":");
				if(split[0].equals(ip) && split[1].equals(String.valueOf(port))) {
					String cmd = "powershell.exe kill " +  split[2];
					try {
							Runtime.getRuntime().exec(cmd);
							System.out.println("DBServer killed");
					} catch (IOException e) {
							e.printStackTrace();
					}
					break;
				}
					
			}
		}
	}

	@Override
	public void reset(String type, String ip, int port) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

}
