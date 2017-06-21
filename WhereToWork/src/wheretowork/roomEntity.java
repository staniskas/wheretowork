package wheretowork;

import javax.persistence.Entity;

@Entity
public class roomEntity {
	
	public String name;
	public String nextStart ="";

    public roomEntity(String name, String nextStart) {
        this.name = name;
        this.nextStart = nextStart;
    }
}
