package ActivityRecognition.DatasetPreprocessor;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Event{
    public DateTime timestamp = new DateTime();
    public String dataSource = "";
    public String state = "";
    public String activityLabel = "";
    private String eventStringFormatRegex = null;

    public Event(){
    }
    
    public Event (String line){
        setParameters(line);
    }
    
    public Event(String line, String esf) {
        eventStringFormatRegex = esf;
        parseParameters(line);
    }
    
    public void setEventStringFormatRegex(String esf) {
        eventStringFormatRegex = esf;
    }
    
    public String getEventStringFormatRegex() {
        return eventStringFormatRegex;
    }
    
    private void setParameters(String line) {
        try {
            String[] chunks = line.split("\t");
            String[] dateChunks = chunks[0].split("-");
            String[] timeChunks = chunks[1].split(":");
            timestamp.setYear(Integer.valueOf(dateChunks[0]));
            timestamp.setMonth(Integer.valueOf(dateChunks[1]));
            timestamp.setDate(Integer.valueOf(dateChunks[2]));
            timestamp.setHours(Integer.valueOf(timeChunks[0]));
            timestamp.setMinutes(Integer.valueOf(timeChunks[1]));
            timestamp.setSeconds(Integer.valueOf(timeChunks[2].split("\\.")[0]));
            timestamp.setMilliseconds(Integer.valueOf(timeChunks[2].split("\\.")[1]));
            dataSource = chunks[2];
            if (chunks.length > 3)
                state = chunks[3];
            else
                return;
            if (chunks.length > 4)
                activityLabel = chunks[4];
        } catch(NumberFormatException e) {
            System.out.println("*** Exception: " + line);
        }
    }
    
    private void parseParameters(String line) {
        if (eventStringFormatRegex == null)
            System.err.println("Event String Format is null!");
        Pattern eventPattern = Pattern.compile(eventStringFormatRegex);
        Matcher m = eventPattern.matcher(line);
        if (m.find()) {
            timestamp.setYear(Integer.valueOf(m.group(1)));
            timestamp.setMonth(Integer.valueOf(m.group(2)));
            timestamp.setDate(Integer.valueOf(m.group(3)));
            timestamp.setHours(Integer.valueOf(m.group(4)));
            timestamp.setMinutes(Integer.valueOf(m.group(5)));
            timestamp.setSeconds(Integer.valueOf(m.group(6)));
            timestamp.setMilliseconds(Integer.valueOf(m.group(7)));
            dataSource = m.group(8).trim();
            if ( m.group(9) != null)
                state = m.group(9).trim();
            else
                return;
            if ( m.group(10) != null)
                activityLabel = m.group(10).trim();
        } else
            System.err.println("Pattern cannot be matched for " + line);
    }
    
    public void setActivityLabel(String al) {
        activityLabel = al;
    }
    
    public void printEvent() {
        System.out.println("***************** Printing Event...");
        System.out.println("Year: " + timestamp.getYear());
        System.out.println("Month: " + timestamp.getMonth());
        System.out.println("Day: " + timestamp.getDay());
        System.out.println("Hour: " + timestamp.getHours());
        System.out.println("Minutes: " + timestamp.getMinutes());
        System.out.println("Secends: " + timestamp.getSeconds());
        System.out.println("Millisecends: " + timestamp.getMilliseconds());
        System.out.println("Data Source: " + dataSource);
        System.out.println("State: "+ state);
        System.out.println("ActivityLabel: "+ activityLabel);
        System.out.println("************************************");
    }
    
    private int indexOf(String s1, char s2, int n) {
        int i = 0, pos = -1;
        do
        {
            pos = s1.indexOf(s2, pos+1);
            i++;
        } while (i < n && pos != -1 );
        if ( pos != -1 )
            return pos;
        return -1;
    }
    
    public String getLine(){
        String line = timestamp.getYear() + "-" +
                timestamp.getMonth() + "-" +
                timestamp.getDay() + "   " +
                timestamp.getHours() + ":" + 
                timestamp.getMinutes() + ":" + 
                timestamp.getSeconds() + "." +
                timestamp.getMilliseconds() + "\t" +
                dataSource + "\t" + state;
        return line;
    }
}
