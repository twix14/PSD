package client.controller.web.inputController.actions;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import db.Status;
import server.Message;
import server.Session;
import server.WideBoxImpl.TimeoutThread;

public class testes {

   private static final String REGEX = "[A-Z][1-40]";
   private static final String INPUT = "3";

   public static void main( String args[] ) {
      //Pattern p = Pattern.compile(REGEX);
      //Matcher m = p.matcher(INPUT);   // get a matcher object

	   lock.lock();
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
      
         System.out.println(INPUT.matches(REGEX));
      
   }
}