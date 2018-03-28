# CARER-Emerging Pattern Mining Module
![alt text](https://github.com/MammadDavari/CARER-Emerging-Pattern-Mining-Module/blob/master/EmergingPatternsMiningModule.png)


1. Preliminaries
	* the SPMF toolset is used for the Frequent sequence mining stage. FMI: http://www.philippe-fournier-viger.com/spmf/
	* the program should reformat the Dataset in order to use it with the SPMF toolset
	* activities in the original dataset are separated into files for each activity with the name of activity on it
	* the dataset contains many sensor readings with no associated activity label, so they would be pruned 
		from the Dataset
	
2. How to config
	* There is a confif file called "configuration.xml"
	* In the Configuration file all the tags within "DataSet" node belongs to the configuration of the dataset
	* All activity labels should be under the "Activity_Label" node, otherwise the activity label would 
		be ?????? in the preprocessing phase
	* "Event_Format_Regex" is a regular expression that matches the event lines in the dataset and groups 
		them such that:
		   i. first group specifies the YEAR within which the event happened
		  ii. second group specifies the MONTH within which the event happened
		 iii. third group specifies the DAY within which the event happened
		  iv. forth group specifies the HOUR within which the event happened
		   v. fifth group specifies the MINUTE within which the event happened
		  vi. sixth group specifies the SECOND within which the event happened
		 vii. seventh group specifies the MILLISECOND within which the event happened
		viii. eighth group specifies the SENSOR NAME of the event
		  ix. ninth group specifies the VALUE that the sensor read
		   x. eighth group specifies the ACTIVITY LABEL if exists
		   
	* Set your desired directories if necessary
	* Set minimum support and minimum discriminative power of your choice
	
3. How to run
	* create a new project in your desired IDE and move the "src" folder content into the src folder of 
		your project. Add "data" folder and "configuration.xml" file to your project's root, as well.
	* there are 3 runnable .java files in the tests package that you can run
	* you should run stages in order specified by the "stage#" prefix. If you want to run a stage separately 
		make sure that there exists necessary files for that stage in advance
	
		  
4. Outputs
	* pruned dataset: a dataset such that all the sensor readings without activity label are removed
	* simple separate SPMF-format dataset files: a separate file for each activity with the SPMF-format
	* frequent patterns: the frequent patterns for each activity
	* emerging patterns: the emerging patterns for each activity
	* SPMFCodeMap file: SMPF toolset does accept as input only numbers as the elements of the sequences. 
		So the SENSOR+STATE of the events are mapped to an integer NUMBER. You can find the mappings between 
		sensor+state and integer values in this file
	
5. Notes
	* the frequent pattern mining may take several HOURS
	* long sequences in the "simple separate SPMF-format files" -generated after the dataset preprocessing 
		stage- causes the time cost of the frequent pattern miner algorithm to grow exponentially
	* if frequent pattern mining stage takes unexpectedly long times, I suggest you to remove some longer sequences 
		selectively in exchange to the overall accuracy
