package utilities;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import server.IWideBox;

public class Cache {

	HashMap<String, IWideBox> appServers;

	public Cache() {
		appServers = new HashMap<String, IWideBox>();
	}

	public IWideBox get(String server) {
		if(appServers.get(server) == null) {
			Registry registry;
			IWideBox wb = null;
			String[] split = server.split(":");
			try {
				registry = LocateRegistry.getRegistry(split[0], 
						Integer.parseInt(split[1]));
				wb = (IWideBox) registry.lookup("WideBoxServer");
				appServers.put(server, wb);
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
			return wb;
		} else
			return appServers.get(server);
	}

	public void list() {
		System.out.println(appServers);
	}


}
