package ActivityRecognition.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class Configuration {
    public DataSetConfiguration DataSet = new DataSetConfiguration();
    public MiningConfiguration Mining = new MiningConfiguration();

    public Configuration(String configFileName) {
        Logger.getLogger(ActivityRecognition.Configuration.Configuration.class.getName()).info("Loading configuration...");
        try {
            File fXmlFile = new File(configFileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            Element dataSetConfigurationElement = (Element) doc.getElementsByTagName("DataSet").item(0);
            DataSet.EventFormatRegex = dataSetConfigurationElement.getElementsByTagName("Event_Format_Regex").item(0).getTextContent();
            DataSet.DataSetFile = dataSetConfigurationElement.getElementsByTagName("DataSet_File").item(0).getTextContent();
            DataSet.SimplePrunedOutputDataSetFile = dataSetConfigurationElement.getElementsByTagName("Simple_Pruned_Output_DataSet_File").item(0).getTextContent();
            DataSet.SimpleSeparateSPFFormatDadaistDir = dataSetConfigurationElement.getElementsByTagName("Simple_Seperate_SPMFFormat_Dataset_Dir").item(0).getTextContent();
            DataSet.SPMFCodeMapsFile = dataSetConfigurationElement.getElementsByTagName("SPMF_Code_Map_File").item(0).getTextContent();
            NodeList activityLabelNodes = ((Element) dataSetConfigurationElement.getElementsByTagName("Activity_Labels").item(0)).getElementsByTagName("Label");
            List<String> activityLabels = new ArrayList<>();
            for (int i = 0; i < activityLabelNodes.getLength(); i++)
                activityLabels.add(activityLabelNodes.item(i).getTextContent());
            String[] al = new String[activityLabels.size()];
            al = activityLabels.toArray(al);
            DataSet.ActivityLabels = al;

            Element miningConfigurationElement = (Element) doc.getElementsByTagName("Mining").item(0);
            Mining.MinimumSupport = Double.valueOf(miningConfigurationElement.getElementsByTagName("Minimum_Support").item(0).getTextContent());
            Mining.FrequentPatternsDir = miningConfigurationElement.getElementsByTagName("Frequent_Patterns_Dir").item(0).getTextContent();
            Mining.KeepPatterns = Boolean.valueOf(miningConfigurationElement.getElementsByTagName("Keep_Patterns").item(0).getTextContent());
            Mining.Verbose = Boolean.valueOf(miningConfigurationElement.getElementsByTagName("Verbose").item(0).getTextContent());
            Mining.FindClosedPatterns = Boolean.valueOf(miningConfigurationElement.getElementsByTagName("Find_Closed_Patterns").item(0).getTextContent());
            Mining.ExecutePruningMethods = Boolean.valueOf(miningConfigurationElement.getElementsByTagName("Execute_Pruning_Methods").item(0).getTextContent());

            Mining.MinimumDiscriminativePower = Double.valueOf(miningConfigurationElement.getElementsByTagName("Minimum_Discriminative_Power").item(0).getTextContent());
            Mining.EmergingPatternsDir = miningConfigurationElement.getElementsByTagName("Emerging_Patterns_Dir").item(0).getTextContent();
        } catch (Exception ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
