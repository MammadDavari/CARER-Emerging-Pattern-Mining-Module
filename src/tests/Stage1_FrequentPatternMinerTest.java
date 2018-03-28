package tests;

import ActivityRecognition.Configuration.Configuration;


public class Stage1_FrequentPatternMinerTest {
    public static void main(String[] args) {
        Configuration configuration = new Configuration("configuration.xml");

        ActivityRecognition.Miner.FrequentPatternMiner fpMiner = new ActivityRecognition.Miner.FrequentPatternMiner(
                configuration.DataSet.SimpleSeparateSPFFormatDadaistDir, configuration.Mining);
        fpMiner.mineAllFrequentPatterns();
    }
}
