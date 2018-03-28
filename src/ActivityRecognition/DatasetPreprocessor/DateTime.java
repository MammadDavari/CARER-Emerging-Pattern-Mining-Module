/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ActivityRecognition.DatasetPreprocessor;

import java.util.Date;

/**
 *
 * @author aSingleBitOfUniverse
 */
public class DateTime extends Date {
    int milliseconds = 0;
    public void setMilliseconds(int m) {
        milliseconds = m;
    }
    public int getMilliseconds() {
        return milliseconds;
    }
    public String getDateTime() {
        return getYear() + "-" +
                getMonth() + "-" +
                getDay() + "   " +
                getHours() + ":" + 
                getMinutes() + ":" + 
                getSeconds() + "." +
                getMilliseconds();
    }
}
