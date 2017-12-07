package client.controller.web.inputController.actions;

import java.io.IOException;
import java.rmi.ConnectException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.presentation.web.inputController.FrontController;
import client.presentation.web.model.QueryTheatresModel;
import loadBal.ILoadBalancer;
import server.IWideBox;
import server.Message;



public class QueryTheatreAction extends Action {

	public void process(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		QueryTheatresModel model = new QueryTheatresModel();
		request.setAttribute("model", model);
		ILoadBalancer lb = FrontController.getLoadBalancer();
		Message mens;
		
		try {
			
			try{
				mens = lb.requestSearch();
			} catch (ConnectException e){
				FrontController.removeLoadBalancer();
				lb = FrontController.getLoadBalancer();
				mens = lb.requestSearch();
			}
				if (mens !=null && mens.getStatus().equals(Message.THEATRES)) {
					model.setTheatres(mens.getTheatres());
					model.setHasTheatres(true);
					FrontController.setServer(mens.getServer());
					
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