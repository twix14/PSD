package client.controller.web.inputController.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.presentation.web.inputController.FrontController;
import client.presentation.web.model.QueryTheatresModel;
import server.IWideBox;
import server.Message;



public class QueryTheatreAction extends Action {

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
					//model.setClientId(Integer.toString(mens.getSession().getId()));
				}
				else {
					model.addMessage("No available Theatres.");
					model.setHasTheatres(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				model.addMessage("Error trying to get the search result: " + e.getMessage());
			}
		
		request.getRequestDispatcher("/WEB-INF/TheatresResult.jsp").forward(request, response);
	}

}