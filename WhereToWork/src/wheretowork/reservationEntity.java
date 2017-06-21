package wheretowork;

import javax.persistence.Entity;

@Entity
public class reservationEntity {
	
	public String name;
	public String start ="";
	public String end ="";

    public reservationEntity(String name, String start, String end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }
}
