/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ActivityRecognition.Miner;


import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import ActivityRecognition.Configuration.MiningConfiguration;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.AlgoCloSpan;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.SequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.clospan_AGP.items.creators.AbstractionCreator_Qualitative;


/**
 * @author Phantom
 */
public class FrequentPatternMiner {
    private List<String> activityFilesList;
    private List<String> frequentPatternsOfActivitiesFilesList = new ArrayList<>();
    private int numberOfActivities;
    private MiningConfiguration miningConfiguration;

    public FrequentPatternMiner(List<String> fl, MiningConfiguration mc) {
        numberOfActivities = fl.size();
        activityFilesList = fl;
        miningConfiguration = mc;
        if (!(new File(miningConfiguration.FrequentPatternsDir)).exists())
            (new File(miningConfiguration.FrequentPatternsDir)).mkdirs();
        for (int i = 0; i < numberOfActivities; i++) {
            String temp = activityFilesList.get(i);
            frequentPatternsOfActivitiesFilesList.add(i,
                    miningConfiguration.FrequentPatternsDir + new File(temp).getName());
        }
    }

    public FrequentPatternMiner(String[] fl, MiningConfiguration mc) {
        numberOfActivities = fl.length;
        activityFilesList = Arrays.asList(fl);
        miningConfiguration = mc;
        if (!(new File(miningConfiguration.FrequentPatternsDir)).exists())
            (new File(miningConfiguration.FrequentPatternsDir)).mkdirs();
        for (int i = 0; i < numberOfActivities; i++) {
            String temp = activityFilesList.get(i);
            frequentPatternsOfActivitiesFilesList.add(i,
                    miningConfiguration.FrequentPatternsDir + new File(temp).getName());
        }
    }

    public FrequentPatternMiner(String seperatedFilesDir, MiningConfiguration mc) {
        File folder = new File(seperatedFilesDir);
        for(File f: Objects.requireNonNull(folder.listFiles())){
            if(f.isFile())
                activityFilesList.add(f.getAbsolutePath());
        }
        numberOfActivities = activityFilesList.size();
        miningConfiguration = mc;
        if (!(new File(miningConfiguration.FrequentPatternsDir)).exists())
            (new File(miningConfiguration.FrequentPatternsDir)).mkdirs();
        for (int i = 0; i < numberOfActivities; i++) {
            String temp = activityFilesList.get(i);
            frequentPatternsOfActivitiesFilesList.add(i,
                    miningConfiguration.FrequentPatternsDir + new File(temp).getName());
        }
    }

    public void mineAllFrequentPatterns() {
        mineAllFrequentPatterns(miningConfiguration.MinimumSupport);
    }

    public void mineAllFrequentPatterns(double minimumSupport) {
        Logger.getLogger(FrequentPatternMiner.class.getName()).info("Starting the MINE PROCESS...");
        ExecutorService es = null;
        for (int i = 0; i < numberOfActivities; i++) {
            Miner m = new Miner(activityFilesList.get(i), frequentPatternsOfActivitiesFilesList.get(i), minimumSupport);
            es = Executors.newCachedThreadPool();
            es.execute(m);
        }
        Objects.requireNonNull(es).shutdown();
    }

    private class Miner implements Runnable {
        String inputPath;
        String outputPath;
        double minsup = 1.0;

        public Miner(String ifn, String ofn, double ms) {
            inputPath = ifn;
            outputPath = ofn;
            minsup = ms;
        }

        public void run() {
            int nfp;
            nfp = mineFrequentPatterns();
            while (nfp < 10 && nfp >= 0 && minsup > 0) {
                nfp = mineFrequentPatterns();
                minsup -= 0.1;
                Logger.getLogger(FrequentPatternMiner.class.getName()).info("Mining file " + inputPath +
                        " has no frequent patterns. Trying with minsup = " + minsup);
            }
            if (nfp < 0) {
                Logger.getLogger(FrequentPatternMiner.class.getName()).info("Mining file " + inputPath +
                        " failed...");
                return;
            }
            Logger.getLogger(FrequentPatternMiner.class.getName()).info("Mining file " + inputPath +
                    " finished. Final number of FPs: " + nfp);
        }

        private int mineFrequentPatterns() {
            Logger.getLogger(FrequentPatternMiner.class.getName()).info("Mining file " + inputPath +
                    " with support = " + minsup + "...");
            try {
                AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
                SequenceDatabase sequenceDatabase = new SequenceDatabase();
                sequenceDatabase.loadFile(inputPath, minsup);

                AlgoCloSpan algorithm;
                algorithm = new AlgoCloSpan(minsup, abstractionCreator, miningConfiguration.FindClosedPatterns,
                        miningConfiguration.ExecutePruningMethods);

                algorithm.runAlgorithm(sequenceDatabase, miningConfiguration.KeepPatterns, miningConfiguration.Verbose,
                        outputPath);
                System.out.println(inputPath + ": " + algorithm.getNumberOfFrequentPatterns() + " pattern found.");
                return algorithm.getNumberOfFrequentPatterns();

            } catch (IOException ex) {
                Logger.getLogger(FrequentPatternMiner.class.getName()).log(Level.SEVERE, null, ex);
            }
            return -1;
        }
    }

    public String fileToPath(String filename) throws UnsupportedEncodingException {

        URL url = FrequentPatternMiner.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
    }

    private int countLines(String fn) {
        int i = 0;
        Scanner input = null;
        try {
            input = new Scanner(new File(fn));
            while (input.hasNextLine()) {
                i++;
                input.nextLine();
            }
        } catch (java.io.FileNotFoundException ex) {
            Logger.getLogger(FrequentPatternMiner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            input.close();
        }
        return i;
    }

    public List<String> getFrequentPatternFilesList() {
        return frequentPatternsOfActivitiesFilesList;
    }

    public void setFrequentPatternsFilesList(List<String> l) {
        frequentPatternsOfActivitiesFilesList = l;
    }
}
