package client.controller.web.inputController.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.presentation.web.inputController.FrontController;
import client.presentation.web.model.QueryTheatresModel;
import server.IWideBox;
import server.Message;

public class ChooseSeatAction extends Action {
		
		public void process(HttpServletRequest request, HttpServletResponse response) 
				throws ServletException, IOException {

			QueryTheatresModel model = createHelper(request);
			request.setAttribute("model", model);
			IWideBox widebox = FrontController.getServer();
			Message mens;
			
			if (validInput(model)) {
				try {
					mens = widebox.seatsAvailable(FrontController.getId()
							, model.getTheatreId());
					if (mens.getStatus().equals(Message.AVAILABLE)) {
						model.setSeats(mens.getSeats());
						model.setSeat(mens.getSession().getSeat());
						model.setClientId(String.valueOf(mens.getSession().getId()));
					}
					else {
						model.addMessage("No available seats, session full.");
						request.getRequestDispatcher("/WEB-INF/Result.jsp").forward(request, response);
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					model.addMessage("Error trying to the available seats: " + e.getMessage());
				}
			} else
				model.addMessage("Error validating the theatre pick");
			
			request.getRequestDispatcher("/WEB-INF/AvailableSeats.jsp").forward(request, response);
		}

		
		private boolean validInput(QueryTheatresModel model) {
			
			return isFilled(model, model.getTheatreId(), "Theatre Id must be filled.");
			
		}


		private QueryTheatresModel createHelper(HttpServletRequest request){
			// Create the object model
			QueryTheatresModel model = new QueryTheatresModel();

			// fill it with data from the request
			model.setTheatreId(request.getParameter("theatreId"));
			model.setClientId(request.getParameter("clientId"));
			
			return model;
		}

}
