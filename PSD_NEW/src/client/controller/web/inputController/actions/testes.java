package client.controller.web.inputController.actions;

public class testes {

   public static void main( String args[] ) {
	   int processors = Runtime.getRuntime().availableProcessors();
	   System.out.println(processors);
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
      
  
