package loadGen;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import db.IWideBoxDB;
import server.IWideBox;
import server.Message;

public class Generator {
	
	public static AtomicInteger rate;
	public static AtomicInteger duration;
	
	public static AtomicInteger requests;
	
	private static final String SERVER_IP = "10.101.148.10";
	private static final int SERVER_PORT = 5000;
	private static final String DB_IP = "10.101.148.10";
	private static final int DB_PORT = 5001;
	
	private static final int NRCL = 100000;
	private static final int def = 20;
	
	

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Registry registry = LocateRegistry.getRegistry(SERVER_IP, SERVER_PORT);
		IWideBox wb = (IWideBox) registry.lookup("WideBoxServer");
		Registry registry2 = LocateRegistry.getRegistry(DB_IP, DB_PORT);
		IWideBoxDB wbDB = (IWideBoxDB) registry2.lookup("WideBoxDBServer");
		requests = new AtomicInteger();
		
		Requests req = new Requests();
		req.setWideBox(wb, wbDB);
		
		System.out.println("LOAD GENERATOR STARTED");
		while(true) {
			System.out.println("Comando do tipo: ORIGIN,TARGET,OPERATION,RATE,DURATION");
			System.out.println("Comando----->");
			String command = sc.nextLine();
			String[] split = command.split(",");
			//db.printStatus(split[2]);
			ExecutorService executor = Executors.newWorkStealingPool();
			
			Random rand = new Random();
		
			
			if (split[0].equals("single") && 
					split[1].equals("single") && split[2].equals("query")) {
				
				if(split[3].equals(" ")) {
					Message m = wb.search();
					executor.submit(() -> {
						while(true) {
							long t0 = System.nanoTime();
							req.singleIdsingleTheatreQuery(rand.nextInt(NRCL+1), m.getTheatres().get(rand.nextInt(NRCL+1)));
							long t1 = System.nanoTime();
							System.out.println("Request took " + TimeUnit.NANOSECONDS.toMillis(t1-t0) + "ms");
							requests.incrementAndGet();
						}
					});
					//Executor for server request rate
					executor.submit(() -> {
						while(true) {
							System.out.println("App server, serving " + req.getRateActualRateAppServerRequests() 
								+ " req/sec ");
						}
					});
					//Executor for db request rate
					executor.submit(() -> {
						while(true) {
							System.out.println("DB server, serving " + req.getRateActualRateDBServerRequests() 
							+ " req/sec ");
						}
					});
					Thread.sleep(Integer.parseInt(split[4])* 1000);
					executor.shutdownNow();
				}
			}
			
			if (split[0].equals("single") && 
					split[1].equals("single") && split[2].equals("purchase")) {
				System.out.println("ClientId,TheatreId:");
				vals = sc.nextLine();
				split2 = command.split(",");
				singleIdsingleTheatrePurchase(split2[0], split2[1])
			}
			
			if (split[0].equals("single") && 
					split[1].equals("random") && split[2].equals("purchase")) {
				
			}
			
	}
	

}
