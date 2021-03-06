package client.controller.web.inputController.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UnknownAction extends Action {

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		request.setAttribute("action", request.getPathInfo());
		request.getRequestDispatcher("/unknownAction.jsp").forward(request, response);
	}
}
