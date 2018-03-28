package ActivityRecognition.DatasetPreprocessor;

import java.io.*;
import java.io.File;
import java.util.Scanner;
import java.util.HashMap;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import ActivityRecognition.Configuration.DataSetConfiguration;


/**
 * <h1>Dataset</h1>
 * Processes a dataset of ADLs with the CASAS Format.
 * <p>
 * CASAS Event Format:
 * "timeStampDate\ttimeStampeTimeOfDay\tSourceSensor\tSensorStatus\t[Activity Label\tBegin|End]"
 * </p>
 *
 * @author Mohammed Davari
 * @version 1.0
 * @since 2015-11-15
 */
public class DataSet {
    private boolean fineActivities = true;
    private boolean coarseActivities = false;
    private String dataSources = "";
    private String statues = "";
    private String relationName = "";
    private String header = "";
    private String data = "";
    private String workingDirectory;
    private int numberOfInstances = 0;
    private List<String> sensors = new ArrayList<>();
    private HashMap<String, Integer> sensorsHashMap = new HashMap<>();
    private HashMap<String, Integer> sensorStateHashMap = new HashMap<>();
    private int sensorID = 0;
    private int sensorStateID = 0;
    private String[] activityLabels;
    private List<SimpleActivityInstance> simpleActivityList = new ArrayList<>();
    private List<ComplexActivityInstance> complexActivityList = new ArrayList<>();
    private List<String> ignoredSensors = new ArrayList<>();
    private List<DiscretizedSensor> discreteSensors = new ArrayList<>();
    private String[] activityFiles_SPMF;
    private List<String> allActivities = new ArrayList<>();
    private int[] numberOfActivityInstances;
    private int[][] conditionalProbabilities;
    private DataSetConfiguration dataSetConfiguration = new DataSetConfiguration();

    public DataSet() {
    }

    public DataSet(DataSetConfiguration dsc) {
        dataSetConfiguration = dsc;
        setActivityLabels(dataSetConfiguration.ActivityLabels);
    }

    public void setDataSetConfiguration(DataSetConfiguration dsc) {
        dataSetConfiguration = dsc;
    }

    public DataSetConfiguration getDataSetConfiguration() {
        return dataSetConfiguration;
    }

    /**
     * This method is used to set all the Activity Labels in Dataset.
     *
     * @param al String array of labels.
     */
    public void setActivityLabels(String[] al) {
        activityLabels = al;
        numberOfActivityInstances = new int[al.length];
        conditionalProbabilities = new int[al.length][al.length];
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        if (!(new File(workingDirectory)).exists()) {
            new File(workingDirectory).mkdirs();
            System.out.println("Building working directory at " + workingDirectory);
        }
    }

    public void onlyConsiderCoarseActivities() {
        fineActivities = false;
        coarseActivities = true;
    }

    public void loadDataSet() {
        loadDataSet(dataSetConfiguration.DataSetFile);
    }

    public void loadDataSet(String fn) {
        Scanner input;
        int previousActivityIndex = -1;
        try {
            input = new Scanner(new File(workingDirectory + fn));  //Opening Input File
            //Initializing data structures
            List<Event> currentActivityTrace = new ArrayList<>();
            boolean begined = false;
            List<String> beginLabels = new ArrayList<>();
            List<String> endLabels = new ArrayList<>();
            List<String> actors = new ArrayList<>();

            //processing the file
            while (input.hasNext()) {
                String line = input.nextLine();
                Event currentEvent = new Event(line, dataSetConfiguration.EventFormatRegex);

                if (isSuspected(line))
                    new DateTime();
                //chacking if new activity begins
                if (isBegingabel(line)) {
                    begined = true;
                    beginLabels.add(approveLabel(currentEvent.activityLabel));
                    if (hasActor(line))
                        actors.add(getActor(line));
                }
                if (begined) {
                    if (isIgnored(currentEvent.dataSource))
                        continue;
                    //discretizes sensor event if needed
                    discretizeSensor(currentEvent);
                    currentActivityTrace.add(currentEvent);
                    //Tracking a list of all sensors and all sensor&states
                    if (!sensors.contains(currentEvent.dataSource))
                        sensors.add(currentEvent.dataSource);
                    if (!sensorsHashMap.containsKey(currentEvent.dataSource))
                        sensorsHashMap.put(currentEvent.dataSource, ++sensorID);
                    if (!sensorStateHashMap.containsKey(currentEvent.dataSource + currentEvent.state))
                        sensorStateHashMap.put(currentEvent.dataSource + currentEvent.state, ++sensorStateID);
                }
                if (isEndingLabel(line)) {
                    endLabels.add(approveLabel(currentEvent.activityLabel));
                    if (beginLabels.size() == 1 && isMatchingEndLabel(line, beginLabels.get(0))) {
                        SimpleActivityInstance sai = new SimpleActivityInstance();
                        sai.setEventsList(currentActivityTrace);
                        String thisLabel = beginLabels.get(0);
                        int thisIndex = getIndex(thisLabel);
                        sai.setLabel(thisLabel);
                        if (!actors.isEmpty())
                            sai.setActor(actors.get(0));
                        simpleActivityList.add(sai);
                        numberOfInstances++;
                        allActivities.add(thisLabel);
                        numberOfActivityInstances[thisIndex]++;
                        if (previousActivityIndex >= 0)
                            conditionalProbabilities[previousActivityIndex][thisIndex]++;
                        previousActivityIndex = thisIndex;
                        //reseting variables
                        currentActivityTrace = new ArrayList<>();
                        begined = false;
                        beginLabels = new ArrayList<>();
                        endLabels = new ArrayList<>();
                        actors = new ArrayList<>();
                    } else if (isBeginsAndEndsMatch(beginLabels, endLabels)) {
                        ComplexActivityInstance cai = new ComplexActivityInstance();
                        cai.setEventsList(currentActivityTrace);
                        cai.setLabels(beginLabels);
                        complexActivityList.add(cai);

                        //reseting variables
                        currentActivityTrace = new ArrayList<>();
                        begined = false;
                        beginLabels = new ArrayList<>();
                        endLabels = new ArrayList<>();
                        actors = new ArrayList<>();
                    }
                }
            }
            input.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean isSuspected(String line) {
        return line.contains("Cook_Breakfast");
    }

    /**
     * This method prints all activity instances to the standard output
     */
    public void printActivityInstances() {
        for (int i = 0; i < simpleActivityList.size(); i++)
            System.out.println(
                    String.format("Simple Instance #%2d:\t %-30s \t %-25s ~ %-25s  contains %d events.",
                            i, simpleActivityList.get(i).getLabel(),
                            simpleActivityList.get(i).getStartTime().getDateTime(),
                            simpleActivityList.get(i).getEndTime().getDateTime(),
                            simpleActivityList.get(i).getLength()));
        for (int i = 0; i < complexActivityList.size(); i++) {
            StringBuilder activities = new StringBuilder();
            for (int j = 0; j < complexActivityList.get(i).getLabels().length; j++)
                activities.append(complexActivityList.get(i).getLabels()[j]).append(", ");
            System.out.println(
                    String.format("Complex Instance #%d: %s\t%s ~ %s  contains %d events.",
                            i, activities.toString(), complexActivityList.get(i).getStartTime().getDateTime(),
                            complexActivityList.get(i).getEndTime().getDateTime(),
                            complexActivityList.get(i).getLength()));
        }
    }

    private boolean isBegingabel(String line) {
        return line.contains("begin");
    }

    private boolean isMatchingEndLabel(String line, String l) {
        return line.contains(l);
    }

    private boolean hasMatchingEndLabel(List<String> list, String l) {
        for (String aList : list) {
            if (aList.contains(l))
                return true;
        }
        return false;
    }

    private boolean isEndingLabel(String line) {
        return line.contains("end");
    }

    private boolean isBeginsAndEndsMatch(List<String> bl, List<String> el) {
        if (bl.size() != el.size())
            return false;
        else {
            for (String aBl : bl)
                if (!el.contains(aBl))
                    return false;
            return true;
        }
    }

    /**
     * This method approves the activity label of an event
     *
     * @param label label of the event"
     * @return string: activity label associated with the line
     */
    private String approveLabel(String label) {
        for (String al : activityLabels) {
            if (fineActivities && label.equals(al))
                return al;
            else if (coarseActivities && label.contains(al))
                return al;
        }
        return "??????????";
    }

    /**
     * This method saves all simple activities with label activityLabel to the file fileName
     *
     * @param fileName      file path to which simple activities with label activityLabel will be saved
     * @param activityLabel interested activity label
     */
    public void saveToFileSimpleActivity(String fileName, String activityLabel) {
        StringBuilder contents = new StringBuilder();
        for (SimpleActivityInstance aSimpleActivityList : simpleActivityList) {
            if (aSimpleActivityList.getLabel().equals(activityLabel)) {
                List<Event> tempList = aSimpleActivityList.getEventsList();
                for (Event event : tempList) {
                    contents.append(event.dataSource).append(", ");
                }
                contents.append("\n");
            }
        }
        try {
            PrintWriter output = new PrintWriter(new File(fileName + "." + activityLabel));
            output.write(contents.toString());
            output.close();
        } catch (Exception ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean hasActor(String line) {
        return line.contains("R1") || line.contains("R2");
    }

    private String getActor(String line) {
        if (line.contains("R1"))
            return "R1";
        else if (line.contains("R2"))
            return "R2";
        else
            return null;
    }

    /**
     * This method builds the header of the .arff Weka input file
     */
    private String buildHeader() {
        header = "@relation " + relationName + "\n" +
                "\n" +
                "@attribute seq-id {";
        String seqNumbers = "";
        header += seqNumbers;
        header += "seq_10000}\n" +
                "@attribute Sequence relational\n" +
                "\t@attribute dataSource {" + dataSources + "nothing}\n" +
                "\t@attribute state {" + statues + "nothin}\n" +
                "@end Sequence\n" +
                "@attribute class {";
        int i = 1;
        for (; i < dataSetConfiguration.ActivityLabels.length; i++) {
            header += i + ",";
        }
        header += String.valueOf(i);
        header += "}\n";
        return header;
    }

    /**
     * This method returns the header of the .arff file
     *
     * @return string: weka file header
     */
    public String getArffFileHeader() {
        return header;
    }

    /**
     * This method returns the data section of the .arff file
     *
     * @return string: weka file body
     */
    public String getArffFileData() {
        return "\n@data\n" + data;
    }

    private void clearData() {
        data = "";
    }

    /**
     * This method returns number of sequences (labeled activities) in the dataset
     *
     * @return int: number of sequences
     */
    public int getNumberOfSequences() {
        return 0;
    }

    private int getNumberOfSimpleInstances() {
        return numberOfInstances;
    }

    /**
     * This method set the name in the relation tag of the .arff Weka file
     *
     * @param r relation name
     */
    public void setRelationName(String r) {
        relationName = r;
    }

    /**
     * This method returns the name in the relation tag of the .arff file
     *
     * @return string: weka file header
     */
    public String getRelationName() {
        return relationName;
    }

    public String getDataSources() {
        return dataSources;
    }

    public void setDataSources(String ds) {
        dataSources = ds;
    }

    public String getStatuses() {
        return statues;
    }

    public void setStatuses(String s) {
        statues = s;
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
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            assert input != null;
            input.close();
        }
        return i;
    }

    private String getActivityLabel(String fn) {
        return fn.substring(fn.indexOf(".t") + 2);
    }

    public int getMaxLabel(int[] activityLabels) {
        int max = 0;
        int index = 0;
        for (int i = 0; i <= dataSetConfiguration.ActivityLabels.length; i++) {
            if (activityLabels[i] > max) {
                max = activityLabels[i];
                index = i;
            }
        }
        return index;
    }

    private String buildBodyString() {
        String body = "";
        for (SimpleActivityInstance sai : simpleActivityList) {
            sai.buildFeatureVector(sensors);
            body += sai.getFeatureVectorString() + "\n";
        }
        return body;
    }

    /**
     * This method builds the header of the .arff Weka input file
     */
    private String buildHeaderString() {
        String header = "@RELATION Tulum2010\n";

        header += "@ATTRIBUTE numberOfSensors INTEGER\n"
                + "@ATTRIBUTE duration INTEGER\n"
                + "@ATTRIBUTE hour INTEGER\n"
                + "@ATTRIBUTE BeginSensor {";
        for (String sensor2 : sensors) {
            header += sensor2 + ", ";
        }
        header += "NOTHING}\n"
                + "@ATTRIBUTE endSensor {";
        for (String sensor1 : sensors) {
            header += sensor1 + ", ";
        }
        header += "NOTHING}\n";
        for (String sensor : sensors) {
            header += "@ATTRIBUTE " + sensor + " INTEGER\n";
        }
        header += "@ATTRIBUTE class {";

        for (int i = 0; i < activityLabels.length - 1; i++) {
            header += activityLabels[i] + ", ";
        }
        header += activityLabels[activityLabels.length - 1] + "}\n\n@data\n";
        return header;
    }

    /**
     * This method saves .arff file used as input dataset to the Weka toolkit
     * <b>Note:</b> Only extracts labeled activities.
     *
     * @param fileName file name of the .arff file (file extension should be .arff)
     */
    public void saveWekaDataset(String fileName) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(fileName));
            pw.write(buildHeaderString());
            pw.append(buildBodyString());
        } catch (Exception ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveSimpleActivityPrunedDataset() {
        saveSimpleActivityPrunedDataset(dataSetConfiguration.SimplePrunedOutputDataSetFile);
    }

    /**
     * This method saves all simple activities in the dataset to a text file
     * <b>Note:</b> Only extracts labeled activities.
     *
     * @param fileName file name
     */
    public void saveSimpleActivityPrunedDataset(String fileName) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(workingDirectory + fileName));
            int activityListSize = simpleActivityList.size();
            for (int i = 0; i < activityListSize; i++) {
                SimpleActivityInstance sai = simpleActivityList.get(i);
                int traceSize = sai.length;
                pw.write(sai.getEvent(0).toString() + "\t" + sai.getLabel() + "\tbegin\n");
                for (int j = 1; j < traceSize - 1; j++) {
                    pw.write(sai.getEvent(j).toString() + "\n");
                }
                pw.write(sai.getEvent(traceSize - 1).toString() + "\t" + sai.getLabel() + "\tend\n");
            }
        } catch (Exception ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method saves all activity instances (simple and complex) into a file
     * <b>Note:</b> Only extracts labeled activities.
     *
     * @param fileName file name to which activity instances will be saved
     */
    public void saveActivityDataSet(String fileName, String activityLabel) {
        @SuppressWarnings("UnusedAssignment")
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(workingDirectory + fileName));
            int activityListSize = simpleActivityList.size();
            for (int i = 0; i < activityListSize; i++) {
                SimpleActivityInstance sai = simpleActivityList.get(i);
                int traceSize = sai.length;
                if (!sai.getLabel().equals(activityLabel))
                    continue;
                pw.write(sai.getEvent(0).toString() + "\t" + sai.getLabel() + "\tbegin\n");
                for (int j = 1; j < traceSize - 1; j++) {
                    pw.write(sai.getEvent(j).toString() + "\n");
                }
                pw.write(sai.getEvent(traceSize - 1).toString() + "\t" + sai.getLabel() + "\tend\n");
            }
        } catch (Exception ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method saves .txt file used as input dataset to the SPMF toolkit.
     * Each Sequence element will be from the set Cartesian product of the sensors and sensorStates
     * <b>Note:</b> Only extracts labeled activities.
     *
     * @param fileName      file name of the .txt file
     * @param activityLabel label of the activity to extract to the file
     */
    public void saveActivityDataSet_SPMFFormat_SourseState(String fileName, String activityLabel) {
        @SuppressWarnings("UnusedAssignment")
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(workingDirectory + fileName));
            int activityListSize = simpleActivityList.size();
            for (int i = 0; i < activityListSize; i++) {
                SimpleActivityInstance sai = simpleActivityList.get(i);
                int traceSize = sai.length;
                if (!sai.getLabel().equals(activityLabel))
                    continue;
                for (int j = 0; j < traceSize; j++) {
                    pw.write(sai.getEvent(j).dataSource + sai.getEvent(j).state + " -1 ");
                }
                pw.write(" -2\n");
            }
        } catch (Exception ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method saves .txt file used as input dataset to the SPMF toolkit.
     * Each Sequence element will be the source sensor
     * <b>Note:</b> Only extracts labeled activities.
     *
     * @param fileName      file name of the .txt file
     * @param activityLabel label of the activity to extract to the file
     */
    public void saveActivityDataSet_SPMFFormat_Sourse(String fileName, String activityLabel) {
        @SuppressWarnings("UnusedAssignment")
        String data = "";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(workingDirectory + fileName));
            int activityListSize = simpleActivityList.size();
            for (int i = 0; i < activityListSize; i++) {
                SimpleActivityInstance sai = simpleActivityList.get(i);
                int traceSize = sai.length;
                if (!sai.getLabel().equals(activityLabel))
                    continue;
                for (int j = 0; j < traceSize; j++) {
                    data += sai.getEvent(j).dataSource + " -1 ";
                }
                data += " -2\n";
            }
            pw.write(data);
        } catch (Exception ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void saveDataSet_SPMFFormat_Sourse() {
        saveDataSet_SPMFFormat_Sourse(dataSetConfiguration.SimpleSeparateSPFFormatDadaistDir);
    }

    /**
     * This method saves .txt file used as input dataset to the SPMF toolkit.
     * Each Sequence element will be the source sensor
     * <b>Note:</b> Only extracts labeled activities.
     * <p>
     * there will be a seperate file prefixed with the activity label.
     */
    public void saveDataSet_SPMFFormat_Sourse(String outputDir) {
        Logger.getLogger(ActivityRecognition.Configuration.Configuration.class.getName()).info("Preparing SPMF Files...");
        if (!(new File(outputDir)).exists())
            (new File(outputDir)).mkdirs();
        activityFiles_SPMF = new String[activityLabels.length];
        PrintWriter pw[] = new PrintWriter[activityLabels.length];
        String activityLabel = "";
        try {
            for (int i = 0; i < activityLabels.length; i++) {
                //preparing acttivity file name
                String fn = workingDirectory + outputDir + activityLabels[i] + ".txt";
                activityFiles_SPMF[i] = fn;
                pw[i] = new PrintWriter(new File(fn));
            }
            int activityListSize = simpleActivityList.size();
            String data[] = new String[activityLabels.length];
            for (int i = 0; i < activityListSize; i++) {
                Logger.getLogger(ActivityRecognition.Configuration.Configuration.class.getName()).log(Level.FINE, "Processing instance #" + i + " of " + getNumberOfSimpleInstances());
                SimpleActivityInstance sai = simpleActivityList.get(i);
                activityLabel = sai.getLabel();
                int traceSize = sai.length;
                int index = getIndex(activityLabel);
                for (int j = 0; j < traceSize; j++) {
                    String temp = sensorsHashMap.get(sai.getEvent(j).dataSource) + " -1 ";
                    pw[index].write(temp);
                }
                pw[index].write("-2\n");
            }
            for (int i = 0; i < activityLabels.length; i++) {
                pw[i].flush();
                pw[i].close();
            }
        } catch (Exception ex) {
            Logger.getLogger(ActivityRecognition.DatasetPreprocessor.DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        saveCodes(dataSetConfiguration.SPMFCodeMapsFile);
    }

    public void saveDataSet_SPMFFormat_SourseState() {
        saveDataSet_SPMFFormat_SourseState("");
    }

    /**
     * This method saves .txt file used as input dataset to the SPMF toolkit.
     * Each Sequence element will be the (source sensor + sensor state)
     * <b>Note:</b> Only extracts labeled activities.
     */
    public void saveDataSet_SPMFFormat_SourseState(String namePrefix) {

        String path = workingDirectory + "simple_state" + "\\";
        if (!(new File(path)).exists())
            (new File(path)).mkdirs();

        activityFiles_SPMF = new String[activityLabels.length];
        PrintWriter pw[] = new PrintWriter[activityLabels.length];
        System.out.println("Preparing SPMF Files...\n");
        String activityLabel = "";
        try {
            for (int i = 0; i < activityLabels.length; i++) {
                //preparing activity file name
                String fn = path + namePrefix + activityLabels[i] + ".txt";
                activityFiles_SPMF[i] = fn;
                pw[i] = new PrintWriter(new File(fn));
            }
            int activityListSize = simpleActivityList.size();
            for (int i = 0; i < activityListSize; i++) {
                System.out.println("Processing instance #" + i + " of " + getNumberOfSimpleInstances());
                SimpleActivityInstance sai = simpleActivityList.get(i);
                activityLabel = sai.getLabel();
                int traceSize = sai.length;
                int index = getIndex(activityLabel);
                for (int j = 0; j < traceSize; j++) {
                    String temp = sensorStateHashMap.get(sai.getEvent(j).dataSource + sai.getEvent(j).state) + " -1 ";
                    pw[index].write(temp);
                }
                pw[index].write("-2\n");
            }
            for (int i = 0; i < activityLabels.length; i++) {
                pw[i].flush();
                pw[i].close();
            }
        } catch (Exception ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Activity Label: " + activityLabel);
        }
        saveCodes(dataSetConfiguration.SPMFCodeMapsFile);
    }

    /**
     * This method saves codes used to map sensors to integers as desired by the
     * SPMF to a text file named codes.txt
     *
     * @param fileName file name of the .txt file. For each activity
     */
    public void saveCodes(String fileName) {
        @SuppressWarnings("UnusedAssignment")
        String data = "";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(workingDirectory + fileName));
            for (String key : sensorsHashMap.keySet())
                data += key + ":" + sensorsHashMap.get(key) + "\n";
            pw.write(data);
            pw.flush();
            pw.close();
        } catch (Exception ex) {
            Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getIndex(String activityLabel) {
        int index = -1;
        for (int i = 0; i < activityLabels.length; i++) {
            if (activityLabels[i].equals(activityLabel)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void setIgnoredSensorsList(String[] ignored) {
        ignoredSensors = Arrays.asList(ignored);
    }

    private boolean isIgnored(String sensor) {
        sensor = sensor.substring(0, 2);
        return ignoredSensors.contains(sensor);
    }

    /**
     * This method adds Discritized sensor. a discritized sensor is a
     * sensor whose state should be discretized.
     *
     * @param ds DiscretizedSensor
     * @see DiscretizedSensor
     */
    public void addDiscritizedSensor(DiscretizedSensor ds) {
        discreteSensors.add(ds);
    }

    /**
     * This method returns the list of all discretized sensors already set for
     * the dataset.
     *
     * @return Lis of all discretized sensors
     * @see DiscretizedSensor
     */
    public List<DiscretizedSensor> getDiscretizedSensorses() {
        return discreteSensors;
    }

    /**
     * This methods checks if the sensor state should be discretized or not.
     * If it is the case then discretizes the sensor state with the bin which is
     * already set
     *
     * @param e is the sensor that its state should be discretizes
     */
    public void discretizeSensor(Event e) {
        for (DiscretizedSensor ds : discreteSensors)
            if (e.dataSource.contains(ds.sensor))
                try {
                    e.state = String.valueOf(Double.valueOf(e.state) / ds.bin);
                } catch (Exception ex) {
                    Logger.getLogger(DataSet.class.getName()).log(Level.SEVERE, null, ex);
                }

    }

    public String[] getActivityFiles_SPMF() {
        return activityFiles_SPMF;
    }

    public void getProbabilities() {
        double conditionalProbabilitiesD[][] = new double[numberOfActivityInstances.length][numberOfActivityInstances.length];
        for (int i = 0; i < numberOfActivityInstances.length; i++) {
            //System.out.print(activityLabels[i]);
            for (int j = 0; j < numberOfActivityInstances.length; j++) {
                conditionalProbabilitiesD[i][j] = ((double) conditionalProbabilities[i][j]) / numberOfActivityInstances[i];
                if (conditionalProbabilitiesD[i][j] == Double.NaN)
                    conditionalProbabilitiesD[i][j] = 0;
                System.out.print(String.format("%.2f", conditionalProbabilitiesD[i][j]) + "\t");
            }
            System.out.println();
        }


    }
}