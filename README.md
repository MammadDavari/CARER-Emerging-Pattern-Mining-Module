# CARER-Emerging Pattern Mining Module
Complex Activity Recognition using Emerging patterns and Random forest

1. Preliminaries
	a. the SPMF toolset is used for the Frequent sequence mining stage. FMI: http://www.philippe-fournier-viger.com/spmf/
	b. the program should reformat the Dataset in order to use it with the SPMF toolset
	c. activities in the original dataset are separated into files for each activity with the name of activity on it
	d. the dataset contains many sensor readings with no associated activity label, so they would be pruned 
		from the Dataset
	
2. How to config
	a. There is a confgi file called "configuration.xml"
	b. In the Configuration file all the tags within "DataSet" node belongs to the configuration of the dataset
	c. All activity labels should be under the "Activity_Label" node, otherwise the activity label would 
		be ?????? in the preprocessing phase
	d. "Event_Format_Regex" is a regular expression that matches the event lines in the dataset and groups 
		them such that:
		   i. first group specifies the year within which the event happened
		  ii. second group specifies the month within which the event happened
		 iii. third group specifies the day within which the event happened
		  iv. forth group specifies the hour within which the event happened
		   v. fifth group specifies the minute within which the event happened
		  vi. sixth group specifies the second within which the event happened
		 vii. seventh group specifies the millisecond within which the event happened
		viii. eighth group specifies the sensor name of the event
		  ix. ninth group specifies the value that the sensor read
		   x. eighth group specifies the Activity Label if exists
		   
	e. Set your desired directories if necessary
	f. Set minimum support and minimum discriminative power of your choice
	
3. How to run
	a. create a new project in your desired IDE and move the "src" folder containments to the src folder of 
		your project. Add "data" folder and "configuration.xml" file to your project's root, too.
	b. there are 3 runnable .java files in the tests package
	c. you should run stages in order specified by the "stage#" prefix. If you want to run a stage separately 
		make sure that there exists necessary files for that stage in advance
	
		  
4. Outputs
	a. pruned dataset: a dataset such that all the sensor readings without activity label are removed
	b. simple separate SPMF-format dataset files: a separate file for each activity with the SPMF-format
	c. frequent patterns: the frequent patterns for each activity
	d. emerging patterns: the emerging patterns for each activity
	e. SPMFCodeMap file: SMPF toolset does accept as input only numbers as the elements of the sequences. 
		So the sensor+state of the events are mapped to an integer number. You can find the mappings between 
		sensor+state and integer values in this file
	
5. Notes
	a. the frequent pattern mining may take several HOURS
	b. long sequences in the "simple separate SPMF-format files" causes the time cost of the frequent pattern miner 
		algorithm to grow exponentially
	c. if frequent pattern mining stage takes unexpectedly long times, i suggest you to remove some longer sequences 
		selectively in exchange to the overall accuracy