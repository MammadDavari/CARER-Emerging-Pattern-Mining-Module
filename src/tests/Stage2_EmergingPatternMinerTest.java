package tests;

import ActivityRecognition.Configuration.Configuration;
import ActivityRecognition.Miner.EmergingPatternMiner;


public class Stage2_EmergingPatternMinerTest {
    public static void main(String[] args) {
        Configuration configuration = new Configuration("configuration.xml");

        EmergingPatternMiner epMiner = new EmergingPatternMiner(configuration.Mining.FrequentPatternsDir,
                configuration.Mining);
        epMiner.setActvityTransactionFileNames(configuration.DataSet.SimpleSeparateSPFFormatDadaistDir);
        epMiner.mineEmergingPatterns();
    }
}