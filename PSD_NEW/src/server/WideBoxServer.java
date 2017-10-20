package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import db.WideBoxDB;

public class WideBoxServer {

	private static final int WIDEBOXCLIENT_PORT = 1616;
	private static final int WIDEBOXDB_PORT = 1717;

	public static void main(String[] args) throws Exception {

		new WideBoxServer(args);

	}

	public WideBoxServer(String[] args) {
		System.out.println("Starting the server...");

		WideBoxDB wideboxDBStub = null;
		WideBoxImpl widebox = null;
		String ipDB = "127.0.0.1";

		try {
			Registry registry = LocateRegistry.getRegistry(ipDB, WIDEBOXDB_PORT);
			wideboxDBStub = (WideBoxDB) registry.lookup("WideBoxDB");
			widebox = new WideBoxImpl(wideboxDBStub);
		} catch (RemoteException e) {
			/*
			 * abrir nova conexao de db, e criar nova implementacao para enviar conexoes
			 */
		} catch (Exception e) {
			System.err.println("Error in Impl");
		}

		try {
			Registry registry = LocateRegistry.createRegistry(WIDEBOXCLIENT_PORT);
			registry.rebind("WideBoxServer", widebox);
		} catch (Exception e) {
			System.err.println("Widebox - Error trying to start the server!");
		}
	}
}