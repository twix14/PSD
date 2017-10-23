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

public class SeatReplyAction extends Action{

	//@EJB private ISportEventServicesRemote addRefToMatchHandler;
			
			private static final int WIDEBOX_PORT = 1616;
			private static final String REGEX = "[A-Z][1-40]";

			IWideBox widebox = FrontController.getWideBoxServer();
			
			
			@Override
			public void process(HttpServletRequest request, HttpServletResponse response) 
					throws ServletException, IOException {

				QueryTheatresModel model = createHelper(request);
				request.setAttribute("model", model);
				Message mens;
				
				if (validInput(model)) {
					try {
					switch(model.getResult()) {
					
						case "YES":
							mens = widebox.acceptSeat(model.getSession());
							if (mens.getStatus().equals(Message.ACCEPT_OK)) {
								model.addMessage("Your purchase is completed.");
							}
							else 
								model.addMessage("Your reservation has experired(ACCEPT_ERROR).");
								
							request.getRequestDispatcher("Result.jsp").forward(request, response);
							break;
							
						case "CAN":
							mens = widebox.cancelSeat(model.getClientId());
							if (mens.getStatus().equals(Message.CANCEL_OK)) {
								model.addMessage("Your reservation is canceled.");
							}
							else 
								model.addMessage("Your reservation has experied(CANCEL_ERROR).");
							
							request.getRequestDispatcher("Result.jsp").forward(request, response);	
							break;
							
						default:
							if (model.getResult().matches(REGEX)) {
								
								mens = widebox.reserveNewSeat(Integer.parseInt(model.getClientId()), model.getResult());
								if (mens.getStatus().equals(Message.AVAILABLE)) {
									model.setSeats(mens.getSeats());
									model.setReservedSeat(mens.getReservedSeat());
								}
								else 
									model.addMessage("No available seats, session full.");
							}
								
							break;
						}
					} catch (Exception e) {
							model.addMessage("Error trying to the available seats: " + e.getMessage());
						}
				} else
					model.addMessage("Please choose one option");
				
				request.getRequestDispatcher("AvailableSeats.jsp").forward(request, response);
			}

			
			private boolean validInput(QueryTheatresModel model) {
				
				return isFilled(model, model.getResult(), "Your choice must be filled.");
				
			}


			private QueryTheatresModel createHelper(HttpServletRequest request){
				// Create the object model
				QueryTheatresModel model = new QueryTheatresModel();

				// fill it with data from the request
				model.setResult(request.getParameter("result"));
				model.setSession(session);(request.getParameter("clientId"));
				
				return model;
			}
}
