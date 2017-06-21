package wheretowork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;


@Api(
		name = "monapi",
		version = "v1"
)

public class whereToWorkEndpoint {

	public DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	public List<String> roomList = new ArrayList<String>();
	public List<String> roomNameList = new ArrayList<String>();
	
	@ApiMethod(
	        path = "rooms/liste",
	        httpMethod = HttpMethod.GET
	    )
	public List<Entity> listRoom() throws IOException {
		Query q = new Query("Room");
        PreparedQuery pq = datastore.prepare(q);  
        List<Entity> listRoom = pq.asList(FetchOptions.Builder.withDefaults());
		
		return listRoom;
	}
	
	@ApiMethod(
	        path = "rooms/eventsList/{date}",
	        httpMethod = HttpMethod.GET
	    )
	public List<Entity> listEvent(@Named("date") String dateSearch) throws IOException {
		
		initializeLists();
		List<Entity> EventList = getEntitiesOfTheDate(dateSearch,"Event");
		List<Entity> ReservationsList = getEntitiesOfTheDate(dateSearch,"Reservation");
		
		List<Entity> newList = new ArrayList<Entity>(EventList);
		newList.addAll(ReservationsList);
		
		return newList;
    }
	
	@ApiMethod(
	        path = "myResa/{userId}",
	        httpMethod = HttpMethod.GET
	    )
	public List<Entity> getMyResa(@Named("userId") String userId) throws IOException {
		Filter user = new FilterPredicate("userId", FilterOperator.EQUAL, userId);

    	// Use class Query to assemble a query
    	Query q = new Query("Reservation").setFilter(user);

    	// Use PreparedQuery interface to retrieve results
    	PreparedQuery pq = datastore.prepare(q);
    	List<Entity> reservationList = pq.asList(FetchOptions.Builder.withDefaults());
		
    	return reservationList;
    	
    }
	
	@ApiMethod(
	        path = "cancelResa/{id}",
	        httpMethod = HttpMethod.DELETE
	    )
	public void cancelResa(@Named("id") String id) throws IOException, EntityNotFoundException {
		
		Key key = KeyFactory.createKey("Reservation", Long.parseLong(id));
	    datastore.delete(key);
	    
    }
	
	
	//La méthode POST fonctionne très bien en local mais pas en déploiement... problème non réoslu
	@ApiMethod(
			path = "reserve",
			httpMethod = HttpMethod.GET
	    )
	public void reserve(@Named("bookingDate")String bookingDate, @Named("room")String room, @Named("when")String when, @Named("mails")String mails, @Named("userId")String userId, @Named("nbPers")String nbPers) throws IOException {
		
		//calcul du dt début et fin pour l'enregistrement dans le datastore
        com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter formatter = com.google.appengine.repackaged.org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");
		com.google.appengine.repackaged.org.joda.time.DateTime dtstart = formatter.parseDateTime(bookingDate);
		
		dtstart = prepareDateStart(dtstart,when);
		
		//problème d'heure au déploiement, ajoute 2 heures
		dtstart = dtstart.minusHours(2);
		
		Long dtstartMillis = dtstart.getMillis();
		
		dtstart = dtstart.plusMinutes(20);
		dtstart = dtstart.plusHours(1);
        
		Long dtendMillis = dtstart.getMillis();

		
        boolean alreadyReserved = searchIfAlreadyReservedUser(userId, dtstartMillis, dtendMillis);
        System.out.println(alreadyReserved);
        
        if(!alreadyReserved){
        	boolean tooMuchPeople = searchIfTooManyPeople(room,nbPers,dtstartMillis);
            if(!tooMuchPeople){
            	saveReservation(room,dtstartMillis,dtendMillis,userId, nbPers, mails);
            }
        }else{
        	
        }
        
    }
	
	
	private boolean searchIfTooManyPeople(String room, String nbPers, Long dtStart) {
		int totalPers = 0;
		Filter reservationFilter = new FilterPredicate("start", FilterOperator.EQUAL, dtStart);

    	// Use class Query to assemble a query
    	Query q = new Query("Reservation").setFilter(reservationFilter);

    	// Use PreparedQuery interface to retrieve results
    	PreparedQuery pq = datastore.prepare(q);
    	List<Entity> reservationsAtADate = pq.asList(FetchOptions.Builder.withDefaults());
		
    	
    	for (Iterator<Entity> iter = reservationsAtADate.iterator(); iter.hasNext(); ) {
    	    Entity element = iter.next();
    	    totalPers = totalPers + Integer.parseInt((String) element.getProperty("nbPers"));
    	}

 
    	System.out.println(30-totalPers);
    	if( (30-totalPers) >= Integer.parseInt(nbPers)){
    			
    		return false;
    	}
    	
    	
    	return true;
    	
    }
	
	private void saveReservation(String room, Long dtstartMillis, Long dtendMillis, String userId, String nbPers, String mails){
		
		Entity reservation = new Entity("Reservation");
		reservation.setProperty("location", room);
		reservation.setProperty("start", dtstartMillis);
		reservation.setProperty("end", dtendMillis);
		reservation.setProperty("userId", userId);
		reservation.setProperty("nbPers", nbPers);
		reservation.setProperty("mails", mails);
		datastore.put(reservation);

	}
	
	//recherche si l'utilisateur n'a pas déjà une réservation à ce moment là
	private boolean searchIfAlreadyReservedUser(String userId, Long dtstartMillis, Long dtendMillis){
	
		Filter user = new FilterPredicate("userId", FilterOperator.EQUAL, userId);

    	// Use class Query to assemble a query
    	Query q = new Query("Reservation").setFilter(user);

    	// Use PreparedQuery interface to retrieve results
    	PreparedQuery pq = datastore.prepare(q);
    	List<Entity> reservationList = pq.asList(FetchOptions.Builder.withDefaults());
	    
    	for (Entity result : reservationList) {
    		if( dtstartMillis >= (Long) result.getProperty("start") && dtstartMillis <= (Long) result.getProperty("end")){
    			return true;
    		}else if(dtendMillis >= (Long) result.getProperty("end") && dtendMillis <= (Long) result.getProperty("end")){
    			return true;
    		}
    	}
    	return false;
	}
	
	private com.google.appengine.repackaged.org.joda.time.DateTime prepareDateStart(com.google.appengine.repackaged.org.joda.time.DateTime dtstart, String when){
	
		switch(when){
			case "first":
				dtstart = dtstart.hourOfDay().setCopy(8);
				dtstart = dtstart.minuteOfHour().setCopy(0);
				break;
			case "second":
				dtstart = dtstart.hourOfDay().setCopy(9);
				dtstart = dtstart.minuteOfHour().setCopy(30);
				
				break;
			case "third":
				dtstart = dtstart.hourOfDay().setCopy(11);
				dtstart = dtstart.minuteOfHour().setCopy(0);
				
				break;
			case "fourth":
				dtstart = dtstart.hourOfDay().setCopy(14);
				dtstart = dtstart.minuteOfHour().setCopy(0);
				
				break;
			case "fifth":
				dtstart = dtstart.hourOfDay().setCopy(15);
				dtstart = dtstart.minuteOfHour().setCopy(30);
				
				break;
			case "sixth":
				dtstart = dtstart.hourOfDay().setCopy(17);
				dtstart = dtstart.minuteOfHour().setCopy(0);
				
				break;
		}
		
		return dtstart;
	}
	
	private List<Entity> getEntitiesOfTheDate(String dateSearch, String entityName){
		com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter formatter = com.google.appengine.repackaged.org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");
		com.google.appengine.repackaged.org.joda.time.DateTime dtSearch = formatter.parseDateTime(dateSearch);
		
		com.google.appengine.repackaged.org.joda.time.DateTime dtBegin = dtSearch;
		dtBegin = dtBegin.hourOfDay().setCopy(0);
		dtBegin = dtBegin.minuteOfHour().setCopy(0);
	    long beginingSearch = dtBegin.getMillis();
	    
	    com.google.appengine.repackaged.org.joda.time.DateTime dtEnd = dtSearch;
	    //à modifier
	    dtEnd = dtEnd.hourOfDay().setCopy(23);
	    dtEnd = dtEnd.minuteOfHour().setCopy(59);
	    long endingSearch = dtEnd.getMillis();
	    
	    Filter StartDateFilter =
	    	new FilterPredicate("start", FilterOperator.GREATER_THAN_OR_EQUAL, beginingSearch);

    	Filter EndDateFilter =
    	    new FilterPredicate("start", FilterOperator.LESS_THAN_OR_EQUAL, endingSearch);

    	// Use CompositeFilter to combine multiple filters
    	CompositeFilter DateRangeFilter = CompositeFilterOperator.and(StartDateFilter, EndDateFilter);

    	// Use class Query to assemble a query
    	Query q = new Query(entityName).setFilter(DateRangeFilter).addSort("start", SortDirection.ASCENDING);

    	// Use PreparedQuery interface to retrieve results
    	PreparedQuery pq = datastore.prepare(q);
    	List<Entity> EventList = pq.asList(FetchOptions.Builder.withDefaults());
    	
    	return EventList;
	}
	
	
	private void initializeLists() {
		
		roomList.clear();
		roomNameList.clear();
		
		Query q = new Query("Room");
        PreparedQuery pq = datastore.prepare(q);  
	    for (Entity result : pq.asIterable()) {   
	    	roomNameList.add((String) result.getProperty("name"));
	    	roomList.add((String) result.getProperty("html"));
		}
	}
	
}
