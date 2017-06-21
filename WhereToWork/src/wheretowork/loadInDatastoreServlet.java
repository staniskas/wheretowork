package wheretowork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;


@SuppressWarnings("serial")
public class loadInDatastoreServlet extends HttpServlet {
	
	public DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	public List<String> roomList = new ArrayList<String>();
	public List<String> roomNameList = new ArrayList<String>();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		//vide la base
		//clearDatastore();
		//rempli la base avec les salles
		loadRoomsDatastore();
		
		initializeLists();
		ListIterator<String> it = roomList.listIterator();
		while(it.hasNext()){
			System.out.println("test");
			String room = it.next();
			try {
				//récupération des évènement de chaque salles
				insertDatasFromRoom(room);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	
	private void loadRoomsDatastore() throws IOException{
		
		String urls="https://edt.univ-nantes.fr/sciences/rindex.html";
		
		URL url = new URL(urls);
		URLConnection uc = url.openConnection();
		uc.setRequestProperty ("Authorization", "Basic ZTEyMzA2Mm46RGFsbGFzZHU0NCE=");
		InputStream inp = uc.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inp,"UTF-8"));
		
		String line = reader.readLine();
		
	    while (!line.contains("/select")){
	    	if(line.contains("option value")){
	    		String[] htmlValue = line.split("\"");
	    		htmlValue = htmlValue[1].split("\\.");

	    		String[] roomName = line.split(">");
	    		roomName = roomName[1].split("<");
	    		

	    		Entity event = new Entity("Room");
	    		event.setProperty("name", roomName[0]);
	    		event.setProperty("html", htmlValue[0]);
	    		event.setProperty("seats", 30);
	    		datastore.put(event);
	    	}
	    	
	    	line = reader.readLine();
	    }
	}
	
	private void initializeLists() {
		
		roomList.clear();
		roomNameList.clear();
		
		Query q = new Query("Room");
        PreparedQuery pq = datastore.prepare(q);  
        System.out.println("test");
	    for (Entity result : pq.asIterable()) {   
	    	roomNameList.add((String) result.getProperty("name"));
	    	roomList.add((String) result.getProperty("html"));
		}
	}
	
	private void insertDatasFromRoom(String roomName) throws ParseException{
		
		Boolean keepLine=false;
		String startDate ="";
		String endDate ="";
		
		try{
			
			String urls="https://edt.univ-nantes.fr/sciences/"+roomName+".ics";
		
			URL url = new URL(urls);
			URLConnection uc = url.openConnection();
			uc.setRequestProperty ("Authorization", "Basic ZTEyMzA2Mm46RGFsbGFzZHU0NCE=");
			InputStream inp = uc.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inp,"UTF-8"));
			
			String line = reader.readLine();
		
		    while ((line = reader.readLine()) != null)
		    {
		    	if(line.contains("DTSTART")){
		    		String fullDate[] = line.split(":");
		    		String stringDate[] = fullDate[1].split("T");
		    		
		    	    Date date = new Date();
		    	    String nowDate= new SimpleDateFormat("yyyyMMdd").format(date);
					int eventD = Integer.parseInt(stringDate[0]);
					int nowD = Integer.parseInt(nowDate);
					if(eventD >= nowD){
						keepLine = true;
						startDate = fullDate[1];
					}
					
		    	}else if(line.contains("DTEND") && keepLine){
		    		
		    		String fullDate[] = line.split(":");
					endDate = fullDate[1];
		    		
		    	}else if(line.contains("LOCATION") && keepLine){
		    		

		    		String fullLocation[] = line.split(":");
		    		
		    		if(fullLocation[1].length() < 50){
		    			
		    			com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter formatter = com.google.appengine.repackaged.org.joda.time.format.DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ");
		    			com.google.appengine.repackaged.org.joda.time.DateTime dtstart = formatter.parseDateTime(startDate);
		    			
		    			//il y a 2 heures de moins dans les fichiers ics
		    			//System.out.println(dtstart);
		    			//int realHour = dtstart.getHourOfDay() + 2;
		    			//dtstart = dtstart.hourOfDay().setCopy(realHour);
		    			//System.out.println(dtstart);
		    			
		    			com.google.appengine.repackaged.org.joda.time.DateTime dtend = formatter.parseDateTime(endDate);
		    			//realHour = dtend.getHourOfDay() + 2;
		    			//dtend = dtend.hourOfDay().setCopy(realHour);
		    			
						Entity event = new Entity("Event");
						event.setProperty("start", dtstart.getMillis());
						event.setProperty("end", dtend.getMillis());
						event.setProperty("location", fullLocation[1]);

						// Enregistre l'entité dans le Datastore
						datastore.put(event);
		    		}

		    		keepLine = false;
		    	}
		    }
	        
		    reader.close();
		  		
        } catch (IOException e) {
            e.printStackTrace();
        }	
		
	}

}