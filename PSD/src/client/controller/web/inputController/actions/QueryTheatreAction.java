package client.controller.web.inputController.actions;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.presentation.web.model.QueryTheatresModel;
import server.IWideBox;
import server.Message;
import server.Theatre;
import server.WideBoxServer;

public class QueryTheatreAction extends Action {

	//@EJB private ISportEventServicesRemote addRefToMatchHandler;
	IWideBox widebox;
	private static final int WIDEBOX_PORT = 1616;

	protected IWideBox getWideBoxServer() {
		try {
			Registry registry = LocateRegistry.getRegistry(getRegistryHost(), WIDEBOX_PORT);
			return (IWideBox) registry.lookup("WideBoxServer");
		} catch (Exception e) {
			System.err.println("Error in Servlet");
			return null;
		}
	}
	
	
	@Override
	public void process(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		QueryTheatresModel model = createHelper(request);
		request.setAttribute("model", model);
		Message mens;
		
		if (validInput(model)) {
			try {
				if (mens = widebox.search(model.getMovie()) && mens.getStatus().equals(Message.THEATRES)) {
					model.setTheatres(mens.getTheatres());
					model.setHasTheatres(true);
					
					//Tem de vir o client id
					model.setClientId(Integer.toString(mens.getClientId()));
				}
				else {
					model.addMessage("No available Theatres.");
					model.setHasTheatres(false);
				}
			} catch (Exception e) {
				model.addMessage("Error trying to get the search result: " + e.getMessage());
			}
		} else
			model.addMessage("Error validating the movie search");
		
		request.getRequestDispatcher("TheatresResult.jsp").forward(request, response);
	}

	
	private boolean validInput(QueryTheatresModel model) {
		
		// check if designation is filled
		return isFilled(model, model.getMovie(), "Movie name must be filled.");
		
	}


	private QueryTheatresModel createHelper(HttpServletRequest request){
		// Create the object model
		QueryTheatresModel model = new QueryTheatresModel();

		// fill it with data from the request
		model.setMovie(request.getParameter("movie"));
		
		return model;
	}	
}