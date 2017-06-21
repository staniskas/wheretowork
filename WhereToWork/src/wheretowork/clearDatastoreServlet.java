package wheretowork;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;


@SuppressWarnings("serial")
public class clearDatastoreServlet extends HttpServlet {
	
	public DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		clearDatastore();
		
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	}
	
	
	public void clearDatastore(){
		Query mydeleteq = new Query("Event");
    	PreparedQuery pq = datastore.prepare(mydeleteq);
    	for (Entity result : pq.asIterable()) {
    		datastore.delete(result.getKey());      
    	} 
    	
    	mydeleteq = new Query("Room");
    	pq = datastore.prepare(mydeleteq);
    	for (Entity result : pq.asIterable()) {
    		datastore.delete(result.getKey());      
    	} 
	}
}