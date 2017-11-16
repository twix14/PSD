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
		Scanner sc = new Scanner(System.in);
		while(true) {
			try {
				System.out.println("0 to crash db, 1 to crash app server, "
						+ "2 to recover db, 3 to recover app server");
				String s = sc.nextLine();
				if(s.equals("0") || s.equals("2")) {
					registry = LocateRegistry.getRegistry(SERVER_IP, SERVER_PORT);
					IWideBox wb = (IWideBox) registry.lookup("WideBoxDBServer");

					try {
						if(s.equals("0")) wb.crash(); else wb.reset();
					} catch (IOException e) {
						System.out.println("DB is now down...");
					}

				} else if(s.equals("1") || s.equals("3")) {
					registry = LocateRegistry.getRegistry(DB_IP, DB_PORT);
					IWideBoxDB wb = (IWideBoxDB) registry.lookup("WideBoxServer");
					
					try {	
						if(s.equals("1")) wb.crash(); else wb.reset();
					} catch (IOException e) {
						System.out.println("AppServer is now down...");
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
