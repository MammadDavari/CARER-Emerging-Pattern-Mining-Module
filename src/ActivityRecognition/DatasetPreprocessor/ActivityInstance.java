/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ActivityRecognition.DatasetPreprocessor;



import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author Phantom
 */
public class ActivityInstance {
    protected List<Event> events;
    protected int length;
    protected String actor;
    
    public ActivityInstance(){
        
        events = new ArrayList<Event>();
        length = 0;
    }
    public List<Event> getEventsList(){
        return events;
    }
    public void setEventsList(List<Event> l){
        events = l;
        length = l.size();
    }
    public void addEvent(Event e){
        events.add(e);
        length++;
    }
    public Event getEvent(int index) throws Exception{
        if (index < events.size())
            return events.get(index);
        else
            throw new Exception("Index out of Range");
    }
    public Event getStartingEvent() throws Exception{
        if (!events.isEmpty())
            return events.get(0);
        else
            throw new Exception("There is no Event");
    }
    public Event getEndingEvent() throws Exception{
        if(!events.isEmpty())
            return events.get(length - 1);
        else
            throw new Exception("There is no Event");
    }
    public int getLength(){
        return length;
    }
    public DateTime getStartTime(){
        return events.get(0).timestamp;
    }
    public DateTime getEndTime(){
        return events.get(length-1).timestamp;
    }
    public String getActor(){
        return actor;
    }
    public void setActor(String a){
        actor = a;
    }
}
