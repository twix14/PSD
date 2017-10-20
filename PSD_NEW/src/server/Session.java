package server;

import java.util.UUID;

public class Session {
	
	private UUID id;
	private String seat;
	
	/**
	 * creates a session for the user with the id 
	 * @param id for the user
	 */
	public Session(UUID id) {
		this.id = id;
	}
	
	public void addSeat(String seat) {
		this.seat = seat;
	}

}
