/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ActivityRecognition.DatasetPreprocessor;

import java.util.List;

/**
 *
 * @author Phantom
 */
public class SimpleActivityInstance extends ActivityInstance{
    private String label;
    FeatureVector featureVector;
    public SimpleActivityInstance(){
        super();
        featureVector = new FeatureVector();
        label = "";
    }
    public void setLabel(String l){
        label = l;
    }
    public String getLabel(){
        return label;
    }
    public FeatureVector getFeatureVector(){
        return featureVector;
    }
    public void buildFeatureVector(List<String> sensors){
        featureVector.numberOfSensors = length;
        featureVector.hour = getStartTime().getHours();
        featureVector.Label = label;
        try{
        featureVector.begingSensor = getStartingEvent().dataSource;
        featureVector.endingSensor = getEndingEvent().dataSource;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        featureVector.duration = getDuration();
        featureVector.bagOfSensors = new int[sensors.size()];
        for(int i = 0; i < sensors.size(); i++){
            featureVector.bagOfSensors[i] = countSensors(sensors.get(i));
        }
    }
    public int countSensors(String s){
        int count = 0;
        for(int i = 0; i < events.size(); i++){
            String temp = events.get(i).dataSource + events.get(i).state;
            if ( temp.contains(s))
                count++;
        }
        return count;
    }
    public int getDuration(){
        DateTime dt0 = getStartTime();
        DateTime dtn = getEndTime();
        return (((((dtn.getYear() - dt0.getYear()) * 12 +
        (dtn.getMonth() - dt0.getMonth())) * 30 + 
        (dtn.getDay() - dt0.getDay())) * 24 +
        (dtn.getHours() - dt0.getHours())) * 60 +
        (dtn.getMinutes() - dt0.getMinutes())) * 60 +
        (int) (dtn.getSeconds()/1000 - dt0.getSeconds()/1000);
    }
    public String getFeatureVectorString(){
        String feature = "";
        feature += featureVector.numberOfSensors
                + ", " + featureVector.duration
                + ", " + featureVector.hour
                + ", " + featureVector.begingSensor 
                + ", " + featureVector.endingSensor;
        for (int i = 0; i < featureVector.bagOfSensors.length; i++)
            feature +=  ", " +featureVector.bagOfSensors[i];
        feature +=  ", " + featureVector.Label;
        return feature;
    }
}
