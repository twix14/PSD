package client.controller.web.inputController.actions;

import java.io.IOException;

import javax.ejb.EJB;
//import javax.ejb.Stateless;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import client.presentation.web.model.QueryTheatresModel;
//import fcul.css.desporges.facade.interfaces.ISportEventServicesRemote;

//@Stateless
public class SearchTheatresAction extends Action {
	
	//@EJB private ISportEventServicesRemote newRefToMatchHandler;

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		QueryTheatresModel model = new QueryTheatresModel();
		//model.setAddCustomerHandler(newRefToMatchHandler);
		request.setAttribute("model", model);
		request.getRequestDispatcher("SearchTheatres.jsp").forward(request, response);
	}

}