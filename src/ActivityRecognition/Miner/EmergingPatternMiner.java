/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ActivityRecognition.Miner;

import ActivityRecognition.Configuration.MiningConfiguration;

import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Phantom
 */
public class EmergingPatternMiner {
    private List<String> frequentPatternsFiles = new ArrayList<>();;
    private List<String> emergingPatternsFiles = new ArrayList<>();
    private List<String> activityTransactionFileNames = new ArrayList<>();
    private int numberOfActivities;
    private MiningConfiguration miningConfiguration;

    public EmergingPatternMiner(List<String> ls, MiningConfiguration mc) {
        frequentPatternsFiles = ls;
        numberOfActivities = ls.size();
        miningConfiguration = mc;
        if (!new File(miningConfiguration.EmergingPatternsDir).exists())
            new File(miningConfiguration.EmergingPatternsDir).mkdirs();
        for (int i = 0; i < numberOfActivities; i++) {
            emergingPatternsFiles.add(miningConfiguration.EmergingPatternsDir + new File(ls.get(i)).getName());
        }
    }

    public EmergingPatternMiner(String[] ls, MiningConfiguration mc) {
        numberOfActivities = ls.length;
        miningConfiguration = mc;
        if (!new File(miningConfiguration.EmergingPatternsDir).exists())
            new File(miningConfiguration.EmergingPatternsDir).mkdirs();
        for (int i = 0; i < numberOfActivities; i++) {
            emergingPatternsFiles.add(miningConfiguration.EmergingPatternsDir + new File(ls[i]).getName());
        }
    }

    public EmergingPatternMiner(String dir, MiningConfiguration mc) {
        File folder = new File(dir);
        for(File f: Objects.requireNonNull(folder.listFiles())){
            if(f.isFile())
                frequentPatternsFiles.add(f.getAbsolutePath());
        }
        numberOfActivities = frequentPatternsFiles.size();
        miningConfiguration = mc;
        if (!new File(miningConfiguration.EmergingPatternsDir).exists())
            new File(miningConfiguration.EmergingPatternsDir).mkdirs();
        for (int i = 0; i < numberOfActivities; i++) {
            emergingPatternsFiles.add(miningConfiguration.EmergingPatternsDir + new File(frequentPatternsFiles.get(i)).getName());
        }
    }

    public void setFrequentPatternsFileNames(String[] fpfn) {
        frequentPatternsFiles = new ArrayList<>();
        Collections.addAll(frequentPatternsFiles, fpfn);
        numberOfActivities = fpfn.length;
    }

    public List<String> getFrequentPatternsFileNames() {
        return frequentPatternsFiles;
    }

    public void setEmergingPatternsFilesList(List<String> l) {
        emergingPatternsFiles = l;
    }

    public void setEmergingPatternFileNames(String[] epfn) {
        emergingPatternsFiles = new ArrayList<>();
        Collections.addAll(emergingPatternsFiles, epfn);
    }

    public List<String> getEmergingPatternFileNames() {
        return emergingPatternsFiles;
    }

    public int getNumberOfActivities() {
        return numberOfActivities;
    }

    public void setActvityTransactionFileNames(String atfnDir) {
        File folder = new File(atfnDir);
        for(File f: Objects.requireNonNull(folder.listFiles())){
            if(f.isFile())
                activityTransactionFileNames.add(f.getAbsolutePath());
        }
    }

    public void setActvityTransactionFileNames(String[] atfn) {
        Collections.addAll(activityTransactionFileNames, atfn);
    }

    public void mineEmergingPatterns() {
        mineEmergingPatterns(miningConfiguration.MinimumDiscriminativePower);
    }

    public void mineEmergingPatterns(double discriminativePowre) {
        if (discriminativePowre > 1) {
            Logger.getLogger(EmergingPatternMiner.class.getName()).info("dpower is not correct!");
            return;
        }
        int[] dbSize = ComputeDBSize(numberOfActivities);

        for (int i = 1; i <= numberOfActivities; i++) {
            try {
                PrintWriter output = new PrintWriter(emergingPatternsFiles.get(i - 1));
                Scanner input = new Scanner(new File(frequentPatternsFiles.get(i - 1)));
                while (input.hasNext()) {
                    String line = input.nextLine();
                    Sequence se = new Sequence(line);
                    int support_SC = Integer.valueOf(line.substring(line.indexOf("#SUP: ") + 6));
                    int support_S = 0;
                    for (int j = 1; j <= numberOfActivities; j++) {
                        if (j == i) {
                            continue;
                        }
                        support_S += ComputeOccurence(se, frequentPatternsFiles.get(j - 1));
                    }
                    support_S += support_SC;
                    double dpower = ((double) support_SC / support_S) * (((double) (dbSize[0] - support_S) -
                            (dbSize[i] - support_SC)) / (dbSize[0] - support_S));
                    if (dpower >= discriminativePowre) {
                        Logger.getLogger(EmergingPatternMiner.class.getName()).info(String.format(se.getSequenceString()
                                + " #DPOWER: %1.3f\n", dpower));
                        output.append(se.getSequenceString()).append(String.format(" #DPOWER: %.3f", dpower)).append("\n");
                    }
                }
                output.close();
            } catch (java.io.FileNotFoundException ex) {
                Logger.getLogger(EmergingPatternMiner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static int ComputeOccurence(Sequence s1, String fileName) {
        int count = 0;
        try {
            Scanner input = new Scanner(new File(fileName));
            while (input.hasNext()) {
                Sequence se = new Sequence(input.nextLine());
                if (s1.IsSubSequence(se)) {
                    count++;
                }
            }
        } catch (java.io.FileNotFoundException ex) {
            Logger.getLogger(EmergingPatternMiner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = FrequentPatternMiner.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
    }

    private int[] ComputeDBSize(int numberOfActivities) {
        int[] count = new int[numberOfActivities + 1];

        for (int i = 1; i <= numberOfActivities; i++) {
            try {
                File f = new File(activityTransactionFileNames.get(i - 1));
                Scanner input = new Scanner(f);
                while (input.hasNext()) {
                    input.nextLine();
                    count[i]++;
                }
            } catch (java.io.FileNotFoundException ex) {
                Logger.getLogger(EmergingPatternMiner.class.getName()).log(Level.SEVERE, null, ex);
            }
            count[0] += count[i];
        }
        return count;
    }
}