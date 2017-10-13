package server;

import java.io.Serializable;
import java.util.List;

public class Theatre implements Serializable {

	private static final long serialVersionUID = -9192350703750256384L;
	
	/**
	 * A DISCUTIR IMPLEMENTACAO DE TEATRO NA BASE DE DADOS
	 * KEY - CINEMA + NºBANCO
	 * VALUE - OCUPADO, RESERVADO OU DISPONIVEL
	 */
	
	private int id;
	
	private List<String> seats;
	
}
