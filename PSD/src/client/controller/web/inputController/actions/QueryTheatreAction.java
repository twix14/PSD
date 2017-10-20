package client.controller.web.inputController.actions;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.presentation.web.inputController.FrontController;
import client.presentation.web.model.QueryTheatresModel;
import server.IWideBox;
import server.Message;
import server.Theatre;
import server.WideBoxServer;

public class QueryTheatreAction extends Action {
	
	@Override
	public void process(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		QueryTheatresModel model = new QueryTheatresModel();
		request.setAttribute("model", model);
		IWideBox widebox = FrontController.getWideBoxServer();
		Message mens;
		
		try {
			mens = widebox.search();
				if (mens !=null && mens.getStatus().equals(Message.THEATRES)) {
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
		
		request.getRequestDispatcher("/WEB-INF/TheatresResult.jsp").forward(request, response);
	}

}