/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ActivityRecognition.Miner;

/**
 *
 * @author Mammad Davari
 */
public class Sequence
{
    private int length;
    private String sequenceString;
    public String[] items;

    public Sequence(String line) {
        sequenceString = line;
        CountLength(sequenceString.split(" "));
        items = new String[length];
        SetItems(sequenceString.split(" "));
    }
    public int length(){
        return length;
    }
    private void CountLength(String[] item) {
        int count = 0;
        for (String item1 : item) {
            if ("-1".equals(item1)) {
                count++;
            } else if ("-2".equals(item1)) {
                break;
            }
        }
        length = count;
    }
    private void SetItems(String[] si) {
        int j = 0;
        for(int i = 0; i < si.length && j < length; i++) {
            if ( !"-1".equals(si[i]) && !"-2".equals(si[i]))
                items[j++] = si[i];
        }
    }
    public boolean IsSubSequence(Sequence s) {
        int j = 0;
        for (int i = 0; i < s.length() && j < length; i++) {
            if (s.items[i] == null ? items[j] == null : s.items[i].equals(items[j]))
                j++;
        }
        return j == length;
    }
    public String getSequenceString() {
        return sequenceString;
    }
}
