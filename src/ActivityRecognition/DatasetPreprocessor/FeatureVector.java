/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ActivityRecognition.DatasetPreprocessor;

/**
 *
 * @author Phantom
 */
public class FeatureVector {
    public int numberOfSensors;
    public int duration;
    public String begingSensor;
    public String endingSensor;
    public int hour;
    public int[] bagOfSensors;
    public String Label;
}
