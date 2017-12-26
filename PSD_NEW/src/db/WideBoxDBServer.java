package db;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import utilities.Status;
import zooKeeper.ZKClient;

public class WideBoxDBServer {
	
	private static SwingWorkerRealTime mySwingWorker;
	private static SwingWrapper<XYChart> sw;
	private static XYChart chart;
		
	public static void main(String[] args) throws Exception {

		IWideBoxDB db = null;
		ZKClient zooKeeper = null;
		IWideBoxDB primaryServer;
		
		
		//args[0] DBServer IP
		//args[1] DBServer port
		//args[2] zooKeeper IP
		//args[3] numberOfTheatres
		//args[4] P/S (Primary/Secondary)
		
		try {
			System.setProperty("java.rmi.server.hostname", args[0]);
			
			try {
				zooKeeper = new ZKClient(args[2], new LinkedBlockingQueue<WatchedEvent>());
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (KeeperException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.out.println("Connected to ZooKeeper");
			String[] pid =  ManagementFactory.getRuntimeMXBean().getName().split("@");
			
			int [] range = null;
			
			if(args[4].equals("P")) {
				range = zooKeeper.createDBNode(args[0], args[1], Integer.parseInt(args[3]),
					pid[0]);
				db = new WideBoxDB(range, true);
				Registry registry2 = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
				registry2.rebind("WideBoxDBServer", db);
			} else {
				String[] temp = zooKeeper.createSecondaryDBNode(args[0], args[1], Integer.parseInt(args[3]),
						pid[0]);
				//TODO
				int [] rangeS = new int [3];
				rangeS [0] = Integer.parseInt(temp[0]) - Integer.parseInt(temp[3]) + 1;
				rangeS [1] = Integer.parseInt(temp[0]);
				rangeS [2] = Integer.parseInt(temp[3]);
				
				db = new WideBoxDB(rangeS, false);
				Registry registry2 = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
				registry2.rebind("WideBoxDBServer", db);
				
				Registry primaryRegistry = LocateRegistry.getRegistry(temp[1], Integer.parseInt(temp[2]));
				primaryServer = (IWideBoxDB) primaryRegistry.lookup("WideBoxDBServer");
				primaryServer.connectToSecondary(args[0], Integer.parseInt(args[1]));
				primaryServer = null;
			}
			
		    go(db);
			
			System.out.println("DB loaded\n");
			System.out.println("Commands:");
			System.out.println("-->'print db n-theatre' command to print status of a theatre");
			System.out.println("-->'full n-theatre' to full a theatre");
			System.out.println("-->'get n-theatre'");
			System.out.println("-->'put n-theatre n-seat newValue oldValue'");
			//
			
		} catch (RemoteException e) {
			System.err.println("Error in getting registry");
			e.printStackTrace();
			 
		}  catch (Exception e) {
			System.err.println("WideBoxDBServer - Error trying  to start the server!");
			e.printStackTrace();
		}
		
		Scanner sc = new Scanner(System.in);
		try {
			while(true) {
				String command = sc.nextLine();
				String[] split = command.split(" ");
				if (split[0].equals("print"))
					db.printStatus(split[2]);
				if(split[0].equals("full"))
					db.fullTheatre(split[1]);
				if(split[0].equals("get"))
					System.out.println(db.get(split[1]));
				if(split[0].equals("put"))
					db.put(split[1], split[2], Status.valueOf(split[3]), Status.valueOf(split[4]));
				if(split[0].equals("sort")) 
					System.out.println(new TreeMap<>(db.listSeats(split[1])));
				
				
			}
		} finally {
			sc.close();
		}
		

	}
	
	private static void go(IWideBoxDB db) {

	    // Create Chart
	    chart = QuickChart.getChart("DB stats", "Time(sec)", "Rate", "Rates", new double[]{0}, new double[]{0});
	    chart.getStyler().setLegendVisible(false);
	    chart.getStyler().setXAxisTicksVisible(true);

	    // Show it
	    sw = new SwingWrapper<XYChart>(chart);
	    sw.displayChart();

	    mySwingWorker = new SwingWorkerRealTime(sw, chart, db);
	    mySwingWorker.execute();
	  }

}
