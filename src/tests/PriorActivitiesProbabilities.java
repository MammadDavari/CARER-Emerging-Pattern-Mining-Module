package tests;

import ActivityRecognition.Configuration.Configuration;
import ActivityRecognition.DatasetPreprocessor.DataSet;
import ActivityRecognition.DatasetPreprocessor.DiscretizedSensor;


public class PriorActivitiesProbabilities {
    public static void main(String[] args) {
        Configuration configuration = new Configuration("configuration.xml");

        DataSet ds = new DataSet(configuration.DataSet);
        String[] ignore = {"BA"};
        ds.setWorkingDirectory(System.getProperty("user.dir") + "\\");
        ds.setIgnoredSensorsList(ignore);
        ds.addDiscritizedSensor((new DiscretizedSensor("LS", 10)));
        ds.addDiscritizedSensor((new DiscretizedSensor("T1", 10)));
        ds.loadDataSet();
        ds.getPriorActivityProbabilities();
    }
}
