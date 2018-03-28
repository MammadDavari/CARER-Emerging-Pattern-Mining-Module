package ActivityRecognition.DatasetPreprocessor;









/**
 * Created with IntelliJ IDEA.
 * <h1>Discretized Sensor</h1>
 * DiscretizedSensor is a sensor whose state should be discretized.
 * It contains the common name of all instances of the sensor in
 * the smart home. e.g. all instanses of Light sensor in a smart home
 * have the prefix of LS## like LS01.
 * There is also a bin vale which indicates the bin length
 * @author Mohammed Davari
 * @since 2015-11-15
 */

public class DiscretizedSensor {
    public String sensor;
    public double bin;
    public DiscretizedSensor(String s, double b){
        sensor = s;
        bin = b;
    }
}
