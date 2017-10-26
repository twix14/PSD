package client.controller.web.inputController.actions;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.presentation.web.inputController.FrontController;
import client.presentation.web.model.QueryTheatresModel;
import server.IWideBox;
import server.Message;
import server.Session;

public class SeatReplyAction extends Action{

			private static final String REGEX = "[A-Z][1-40]";

			IWideBox widebox = FrontController.getWideBoxServer();
			
			
			@Override
			public void process(HttpServletRequest request, HttpServletResponse response) 
					throws ServletException, IOException {

				QueryTheatresModel model = createHelper(request);
				request.setAttribute("model", model);
				Message mens;
				Session sess = null;
				
				if (validInput(model)) {
					try {
						sess = new Session(Integer.parseInt(model.getClientId()));
						sess.setSeat(model.getSeat());
						sess.setTheatre(model.getTheatreId());
					switch(model.getResult()) {
					
						case "YES":
							
							mens = widebox.acceptSeat(sess);
							if (mens.getStatus().equals(Message.ACCEPT_OK)) {
								model.addMessage("Your purchase is completed.");
							}
							else 
								model.addMessage("Your reservation has experired(ACCEPT_ERROR).");
								
							request.getRequestDispatcher("/WEB-INF/Result.jsp").forward(request, response);
							break;
							
						case "CAN":
							mens = widebox.cancelSeat(sess);
							if (mens.getStatus().equals(Message.CANCEL_OK)) {
								model.addMessage("Your reservation is canceled.");
							}
							else 
								model.addMessage("Your reservation has experied(CANCEL_ERROR).");
							
							request.getRequestDispatcher("/WEB-INF/Result.jsp").forward(request, response);	
							break;
							
						default:
							//if (model.getResult().matches(REGEX)) {
								
								mens = widebox.reserveNewSeat(sess, model.getResult());
								if (mens.getStatus().equals(Message.AVAILABLE)) {
									model.setSeats(mens.getSeats());
									model.setSeat(mens.getSession().getSeat());
									model.setClientId(String.valueOf(mens.getSession().getId()));
									model.addMessage("Your purchase is completed.");
									
									request.getRequestDispatcher("/WEB-INF/Result.jsp").forward(request, response);
								}
								else { 
									model.addMessage("Your reservation has experired.");
									request.getRequestDispatcher("/WEB-INF/Result.jsp").forward(request, response);
								}
								
								
							/*} 
							else {
								mens = widebox.seatsAvailable(model.getTheatreId());
								//if (mens.getStatus().equals(Message.))
								model.setSeats(mens.getSeats());
								//model.setSeat();
								request.getRequestDispatcher("/WEB-INF/AvailableSeats.jsp").forward(request, response);
							}*/
								
								
							break;
						}
					} catch (Exception e) {
							model.addMessage("Error trying to the available seats: " + e.getMessage());
							request.getRequestDispatcher("/WEB-INF/AvailableSeats.jsp").forward(request, response);
						}
				} else {
					model.addMessage("Please choose one option");
					request.getRequestDispatcher("/WEB-INF/AvailableSeats.jsp").forward(request, response);
				}
				
				
			}

			
			private boolean validInput(QueryTheatresModel model) {
				
				return isFilled(model, model.getResult(), "Your choice must be filled.");
				
			}


			private QueryTheatresModel createHelper(HttpServletRequest request){
				// Create the object model
				QueryTheatresModel model = new QueryTheatresModel();

				// fill it with data from the request
				model.setResult(request.getParameter("result"));
				model.setClientId(request.getParameter("clientId"));
				model.setSeat(request.getParameter("seat"));
				model.setTheatreId(request.getParameter("theatre"));
				return model;
			}
}
