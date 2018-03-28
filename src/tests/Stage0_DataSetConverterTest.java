/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import ActivityRecognition.Configuration.Configuration;
import ActivityRecognition.DatasetPreprocessor.DataSet;
import ActivityRecognition.DatasetPreprocessor.DiscretizedSensor;


public class Stage0_DataSetConverterTest {
    public static void main(String[] args) {
        Configuration configuration = new Configuration("configuration.xml");

        DataSet ds = new DataSet(configuration.DataSet);
        String[] ignore = {"BA"};
        ds.setWorkingDirectory(System.getProperty("user.dir") + "\\");
        ds.setIgnoredSensorsList(ignore);
        ds.addDiscritizedSensor((new DiscretizedSensor("LS", 10)));
        ds.addDiscritizedSensor((new DiscretizedSensor("T1", 10)));
        ds.loadDataSet();
        ds.printActivityInstances();
        ds.saveSimpleActivityPrunedDataset();
        ds.saveDataSet_SPMFFormat_Sourse();
    }
    
}
