/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ActivityRecognition.DatasetPreprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Phantom
 */
public class ComplexActivityInstance extends ActivityInstance {
    private List<String> labels;
    
    public ComplexActivityInstance(){
        super();
        labels = new ArrayList<>();
    }    
    public void addLabel(String l){
        labels.add(l);
    }
    public String[] getLabels(){
        return labels.toArray(new String[labels.size()]);
    }
    public int getNumberOfLabels(){
        return labels.size();
    }
    public void setLabels(List<String> l){
        labels = l;
    }
}
