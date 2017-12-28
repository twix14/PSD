package fail;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class FailureGenerator {

	public static void main(String[] args) {
		Registry registry;
		Scanner sc = new Scanner(System.in);
		while(true) {
			try {
				System.out.println("Ip 'space' Port of the server you want want to "
						+ "crash 'space' 0 for crash 1 for reset 'space' "
						+ "0 for loadBalancer 1 for appServer 2 for Db" );
				String s = sc.nextLine();
				String[] ipPort = s.split(" ");
				//PORT OF FAILURE GENERATOR IS ALWAYS 5010
				registry = LocateRegistry.getRegistry(ipPort[0], 5030);
				IFailureGen fg = (IFailureGen) registry.lookup("FailureGenerator");
				
				if(ipPort[2].equals("0")) {
					
					if(ipPort[3].equals("0")) {
						fg.crash("LoadBalancer", ipPort[0], Integer.parseInt(ipPort[1]));
						System.out.println("The load balancer is now down!");
					}else if(ipPort[3].equals("1")) {
						fg.crash("WideBoxServer", ipPort[0], Integer.parseInt(ipPort[1]));
						System.out.println("The DB Server is now down!");
					}else {
						fg.crash("WideBoxDBServer", ipPort[0], Integer.parseInt(ipPort[1]));
						System.out.println("The App Server is now down!");
					}
				} else if(ipPort[2].equals("1")) {
					
					if(ipPort[3].equals("0")) {
						fg.reset("LoadBalancer", ipPort[0], Integer.parseInt(ipPort[1]));
						System.out.println("The load balancer is now down!");
					}else if(ipPort[3].equals("1")) {
						fg.reset("WideBoxServer", ipPort[0], Integer.parseInt(ipPort[1]));
						System.out.println("The App Server is now down!");
					}else {
						fg.reset("WideBoxDBServer", ipPort[0], Integer.parseInt(ipPort[1]));
						System.out.println("The DB Server is now down!");
					}
					
				} else {
					System.err.println("Write 0,1,2,3!");
				}



			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sc.close();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sc.close();
			} catch (NotBoundException e) {
				sc.close();
			}
		}

	}

}
