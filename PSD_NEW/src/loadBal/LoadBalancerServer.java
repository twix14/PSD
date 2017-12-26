package loadBal;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import db.IWideBoxDB;
import db.SwingWorkerRealTime;
import zooKeeper.ZKClient;

public class LoadBalancerServer {
	
	private static LoadBalancerCharts mySwingWorker;
	private static SwingWrapper<XYChart> sw;
	private static XYChart chart;
	
	public static void main(String[] args) throws Exception {
		new LoadBalancerServer(args);
	}
	
	public LoadBalancerServer(String[] args) {
		
		System.out.println("Starting the load balancer...");
		
		//args[0] - Load Balancer IP
		//args[1] - Load Balancer Port
		//args[2] - ZooKeeper IP
		
		try {
			//get the zookeeper client
			ZKClient zooKeeper = null;
			BlockingQueue<String> events =  new LinkedBlockingQueue<String>();
			try {
				zooKeeper = new ZKClient(args[2], new LinkedBlockingQueue<WatchedEvent>());
				zooKeeper.setQueue(events);
				System.out.println("Connected to ZooKeeper");
				zooKeeper.setQueue(events);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (KeeperException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			String[] pid =  ManagementFactory.getRuntimeMXBean().getName().split("@");
			zooKeeper.createLBNode(args[0], args[1], pid[0]);
			
			System.setProperty("java.rmi.server.hostname", args[0]);
			ILoadBalancer loadbalancer = new LoadBalancerImpl(zooKeeper, events);
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
			registry.rebind("LoadBalancer", loadbalancer);
			
			go(loadbalancer);
			
		} catch (RemoteException e) {
			System.err.println("Error in getting registry");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Error starting the server");
			e.printStackTrace();
		}
		
	}
	
	private static void go(ILoadBalancer lb) {

	    // Create Chart
	    chart = QuickChart.getChart("System Throughput", "Time(sec)", "Ops(sec)", "Throughput", new double[]{0}, new double[]{0});
	    chart.getStyler().setLegendVisible(false);
	    chart.getStyler().setXAxisTicksVisible(true);

	    // Show it
	    sw = new SwingWrapper<XYChart>(chart);
	    sw.displayChart();

	    mySwingWorker = new LoadBalancerCharts(sw, chart, lb);
	    mySwingWorker.execute();
	  }

}
