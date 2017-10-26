package loadGen;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Generator {
	
	private AtomicInteger rate;
	private AtomicInteger duration;
	
	private static final String SERVER_IP = "10.101.148.10";
	private static final int SERVER_PORT = 5000;
	
	

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		System.out.println("LOAD GENERATOR STARTED");
		while(true) {
			System.out.println("Comando do tipo: ORIGIN,TARGET,OPERATION,RATE,DURATION");
			System.out.println("Comando----->");
			String command = sc.nextLine();
			String[] split = command.split(" ");
			//db.printStatus(split[2]);
		}
	}
	

}
