package fail;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import db.IWideBoxDB;
import server.IWideBox;

public class FailureGenerator {
	
	private static final String SERVER_IP = "10.101.148.86";
	private static final int SERVER_PORT = 5000; 
	
	private static final String DB_IP = "10.101.148.86";
	private static final int DB_PORT = 5001; 
	
	public static void main(String[] args) {
		Registry registry;
		try {
			Scanner sc = new Scanner(System.in);
			System.out.println("0 to crash db, 1 to crash app server");
			String s = sc.nextLine();
			if(s.equals("0")) {
				registry = LocateRegistry.getRegistry(SERVER_IP, SERVER_PORT);
				IWideBox wb = (IWideBox) registry.lookup("WideBoxServer");
				
				try {	
					wb.crash();
				} catch (IOException e) {
					System.out.println("DB is now down...");
				}
			
			} else if(s.equals("1")) {
				registry = LocateRegistry.getRegistry(DB_IP, DB_PORT);
				IWideBoxDB wb = (IWideBoxDB) registry.lookup("WideBoxDBServer");
				try {	
					wb.crash();
				} catch (IOException e) {
					System.out.println("AppServer is now down...");
				}
				
			} else {
				System.err.println("Write 0 or 1!");
			}
			
			sc.close();
				
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			  
		}
		
	}

}
