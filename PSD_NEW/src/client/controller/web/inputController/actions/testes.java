package client.controller.web.inputController.actions;
import java.rmi.RemoteException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import server.Message;
import server.WideBoxImpl.TimeoutThread;
import utilities.Session;
import utilities.Status;

public class testes {

   private static final String REGEX = "[A-Z][1-40]";
   private static final String INPUT = "3";
 class TTThread extends Thread {

			
			public TTThread() {
			
			}
			
			public void run() {
				//timeout of 30 seconds
				
				try {
					Thread.sleep(15000);
					
					//Libertar lugar
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				return;
			}
}
   public static void main( String args[] ) {
	   TTThread t = new TTThread();
	 t.start();
	 t.interrupt();
	 t.interrupt();
	 
	 }}
      //Pattern p = Pattern.compile(REGEX);
      //Matcher m = p.matcher(INPUT);   // get a matcher object

	/*   lock.lock();
		Message response = null;
		try {
			String availableSeat = wideboxDBStub.get(theater);
			
			if(!availableSeat.equals("FULL")) {
				int id = getNextClientId();
				boolean result = wideboxDBStub.put(theater + "-" + availableSeat,
						Status.OCCUPIED, Status.FREE);
				
				if(result) {
					response = new Message(Message.AVAILABLE);
					Status[][] seats = wideboxDBStub.listSeats(theater);
					response.setSeats(seats);
					Session sess = new Session(id);
					sess.setSeat(availableSeat);
					sess.setTheatre(theater);
					response.setSession(sess);
					
					TimeoutThread tt = new TimeoutThread(theater+ "-" + 
							availableSeat, id);
					sessions.put(id, tt);
				} else {
					//VER O QUE DA ERROS
				}
				
							
			} else {
				response = new Message(Message.FULL);
			}
		} finally {
			lock.unlock();
		}
		return response;
      
         System.out.println(INPUT.matches(REGEX));*/
      
  
