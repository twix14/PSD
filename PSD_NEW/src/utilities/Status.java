package utilities;

public enum Status {
	FREE, RESERVED, OCCUPIED;
	
	

    public String getString() {
        return this.name();
    }
    
    public boolean getReserved() {
        return this == RESERVED;
    }

      public boolean getOccupied() {
        return this == OCCUPIED;
      }
}
  