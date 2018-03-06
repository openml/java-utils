package org.openml.checkdatasets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openml.apiconnector.algorithms.Conversion;
import org.openml.apiconnector.io.ApiException;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.settings.Settings;
import org.openml.apiconnector.xml.Data.DataSet;
import org.openml.apiconnector.xml.DataFeature;
import org.openml.apiconnector.xml.DataFeature.Feature;
import org.openml.apiconnector.xml.DataQuality;
import org.openml.apiconnector.xml.DataSetDescription;
import org.openml.apiconnector.xml.Tasks;
import org.openml.apiconnector.xstream.XstreamXmlMapping;
import org.xml.sax.SAXException;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * @author Arlind Kadra
 * 
 * The validator class will check if the datasets fullfill certain requirements.
 */

public class Validator {
	
	private final static String XSDSCHEMA = "openml.data.upload";
	private OpenmlConnector connector;
	private Logger logger;
	// data type values.
	private enum Type {nominal, string, date, numeric};
	
	public Validator() {
		
		connector = new OpenmlConnector("https://www.openml.org/", "0bdf33abdecb6631fbc77b6978258a2c");
		Settings.CACHE_ALLOWED = false;
		logger = Logger.getInstance();
	}
	
	/** Validates the datasets on all requirements.
	 * 
	 * @throws Exception
	 */
	public void performAllChecks() throws Exception {
		
		
		ArrayList<Integer> ids = getListUniqueDatasetIds();
		this.checkARFF(ids);
		this.checkCSV(ids);
		this.validateDatasetsOnSchema(ids);
		this.checkFeatures(ids);
		this.checkQualities(ids);
		this.checktasks(ids);
	}
	

	/** 
	 * Gets all the datasets from the server and returns only the dataset ids.
	 * 
	 * @return - a list of dataset ids.
	 * @throws Exception - when a problem occurs while getting the list of datasets from the server
	 */
	private ArrayList<Integer> getListDatasetIds() throws Exception {
		
		DataSet[] datasets = connector.dataList(null).getData();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(int i=0; i < datasets.length; i++) {
			ids.add(datasets[i].getDid());
		}
		return ids;
	}
	
	/**
	 * Uses the dataset ids to download DataQuality objects for each dataset. 
	 * Based on the DataQuality objects only keep unique dataset ids and flag duplicate ones. 
	 * The later ones are tagged in case of duplicates. 
	 * 
	 * @return - an ArrayList of unique dataset ids.
	 * @throws Exception
	 */
	public ArrayList<Integer> getListUniqueDatasetIds() throws Exception {
		
		StringBuilder builder = new StringBuilder();
		builder.append("Info - Duplicate datasets: ");
		ArrayList<Integer> ids = getListDatasetIds();
		HashSet<DataQuality> dataQualities = new HashSet<DataQuality>();
		ArrayList<Integer> uniqueIds = new ArrayList<Integer>();
		for(Integer id: ids) {
			if(dataQualities.add(connector.dataQualities(id))) {
				uniqueIds.add(id);
			} else {
				builder.append(id + ", ");
			}
		}
		builder.append(System.lineSeparator());
		logger.logToFile(builder.toString());
		return uniqueIds;
	}
	
	private File getARFF(int datasetId) throws Exception {
		
		return connector.datasetGet(connector.dataGet(datasetId));
	}
	
	private File getCSV(int datasetId) throws Exception  {
		
		return connector.datasetGetCsv(connector.dataGet(datasetId));
	}
	
	/**
	 * Get each dataset CSV file and see if it can be parsed.
	 * If a failure occurs, mark the dataset and continue.
	 * 
	 * @param ids - dataset ids.
	 * @throws Exception
	 */
	public void checkCSV(ArrayList<Integer> ids) throws Exception {
		
		StringBuilder builder = new StringBuilder();
		builder.append("Info - CSV file cannot be parsed: ");
		for(Integer id: ids) {
			if(!(validateCSV(getCSV(id)))) {
				builder.append(id + ", ");
			}
		}
		builder.append(System.lineSeparator());
		logger.logToFile(builder.toString());
	}
	
	/**
	 * Function which checks if a csv file can be parsed.
	 * 
	 * @param csv - The csv file.
	 * @return - true if the csv file can be parsed, false if it cannot.
	 * @throws FileNotFoundException
	 */
	protected boolean validateCSV(File csv) throws FileNotFoundException {
		
		boolean flag = true;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(csv));
			CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT);
			// Go through the records one by one as not to increase strain on memory.
			Iterator<CSVRecord> iterator = parser.iterator();
			while(iterator.hasNext()) {
				// for the moment do nothing as we do not need the values
				iterator.next();
			}
			parser.close();
			reader.close();
		} catch (FileNotFoundException e1) {
			throw e1;
		} catch (IOException e2) {
			flag = false;
			e2.printStackTrace();
		}
		return flag;
	}
	
	/**
	 * For each dataset id, download the ARFF file.
	 * Check if the ARFF file can be parsed. 
	 * If it cannot be parsed, mark the dataset and continue to the next one.
	 * 
	 * @param ids - dataset ids.
	 * @throws Exception
	 */
	public void checkARFF(ArrayList<Integer> ids) throws Exception {
		
		StringBuilder builder = new StringBuilder();
		builder.append("Info - ARFF file cannot be parsed: ");
		for(Integer id: ids) {
			System.out.println(id);
			try {
				validateARFF(getARFF(id));
			} catch(IOException e) {
				builder.append(id + ", ");
			}
		}
		builder.append(System.lineSeparator());
		logger.logToFile(builder.toString());
	}
	
	/**
	 * Checks if an ARFF file can be parsed by using weka.
	 * 
	 * @param arff - ARFF file.
	 * @throws IOException - when the ARFF file cannot be parsed.
	 */
	protected void validateARFF(File arff) throws IOException {
		
		ArffLoader arffLoader = new ArffLoader();
		arffLoader.setFile(arff);
		Instances structure = arffLoader.getStructure();
		// Going through the instances one by one as to not strain the memory.
		while(arffLoader.getNextInstance(structure) != null) {
			// No need to check the values, just pass
		}
	}
	
	/**
	 * Gets the DatasetDescription for each dataset id and validate against the xsd schema.
	 * 
	 * @param ids - dataset ids.
	 * @throws Exception
	 */
	public void validateDatasetsOnSchema(ArrayList<Integer> ids) throws Exception {
		
		StringBuilder builder = new StringBuilder();
		builder.append("Info - Could not be validated on schema: ");
		File xsdSchema = connector.getXSD(XSDSCHEMA);
		if(xsdSchema != null) {
			for(Integer id: ids) {
				DataSetDescription datasetDescription = connector.dataGet(id);
				File xml = null;
				try {
					// Create temp file.
					xml = File.createTempFile("datadescrp" + id, ".xml");
					xml.deleteOnExit();
					// Write to temp file
					BufferedWriter out = new BufferedWriter(new FileWriter(xml));
					out.write(XstreamXmlMapping.getInstance().toXML(datasetDescription));
					out.close();
				} catch(IOException e) {
					logger.logToFile("Could not create xml file " + e.toString());
					throw e;
				}
				if(xml != null) {
					if(!(validateDatasetOnSchema(xml, xsdSchema))) {
						builder.append(id + ", ");
					}
				}
			}
		}
		builder.append(System.lineSeparator());
		logger.logToFile(builder.toString());
	}
	
	/**
	 * Validate the xml file against the xsd schema on OpenML.
	 * 
	 * @param xml - xml file.
	 * @return - true if validation succeeds, false if it does not.
	 */
	protected boolean validateDatasetOnSchema(File xml, File xsd) {
		
		try {
			return Conversion.validateXML(xml, xsd);
		} catch (IOException | SAXException e1) {
			// Do nothing, validation failed on the xml file. False will be returned.
			return false;
		}
	}
	
	/**
	 * Gets the DataQuality for each dataset id and check whether there are problems with the qualities.
	 * Mark the datasets which have problems with the qualities.
	 * 
	 * @param ids - dataset ids.
	 * @throws Exception
	 */
	public void checkQualities(ArrayList<Integer> ids) throws Exception {
		
		StringBuilder builder = new StringBuilder();
		builder.append("Info - Problem with the qualities: ");
		for(Integer id: ids) {
			if(!validateQualities(connector.dataQualities(id))) {
				builder.append(id + ", ");
			}
		}
		builder.append(System.lineSeparator());
		logger.logToFile(builder.toString());
	}
	
	/**
	 * Checks whether the DataQuality object has no missing values and different data types for the parameters.
	 * Mark the datasets which fail the check.
	 * 
	 * @param qualities - DataQuality object.
	 * @return - true if the validation is successfull, false otherwise.
	 */
	protected boolean validateQualities(DataQuality qualities) {
		
		for(Map.Entry<String, Double> entrySet : qualities.getQualitiesMap().entrySet()) {
			if(entrySet.getKey() != null && entrySet.getKey() instanceof String) {
				if(entrySet.getValue() != null && entrySet.getValue() instanceof Double) {
					continue;
				}
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Get the DataFeature object for each dataset id and check each feature.
	 * Mark datasets which fail the requirements.
	 * 
	 * @param ids - dataset ids.
	 * @throws Exception
	 */
	public void checkFeatures(ArrayList<Integer> ids) throws Exception {
		
		StringBuilder builder = new StringBuilder();
		builder.append("Info - Problem with the features: ");
		for(Integer id: ids) {
			DataFeature dataFeatures = connector.dataFeatures(id);
			if(!(validateFeature(dataFeatures))) {
				builder.append(id + ", ");
			}
		}
		builder.append(System.lineSeparator());
		logger.logToFile(builder.toString());
	}
	
	/**
	 * Validate the different features. Check whether the parameters have different value types or if they have missing values.
	 * 
	 * @param - dataFeatures - DataFeature object.
	 * @return - true if all features pass the checks, false otherwise.
	 */
	protected boolean validateFeature(DataFeature dataFeatures) {
		
		for(Feature feature : dataFeatures.getFeatures()) {
			if(feature.getIndex() != null && feature.getIndex() instanceof Integer) {
				if(feature.getName() != null && feature.getName() instanceof String) {
					if(feature.getDataType() != null && feature.getDataType() instanceof String && validateDataType(feature.getDataType())) {
						if(feature.getIs_target() != null && feature.getIs_target() instanceof Boolean) {
							continue;
						}
					}
				}
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Check whether the data type belongs to one of several categories.
	 * 
	 * @param - dataType - String of the data type description.
	 * @return - true if it belongs to one of the hardcoded categories, false otherwise.
	 */
	protected boolean validateDataType(String dataType) {
		
		return Arrays.stream(Type.values()).anyMatch((t) -> t.name().equals(dataType.toLowerCase()));
	}
	
	/**
	 * For each dataset id, check whether there are one or more tasks that are made upon it.
	 * 
	 * @param ids - dataset ids.
	 * @throws Exception
	 */
	
	public void checktasks(ArrayList<Integer> ids) throws Exception {
		
		StringBuilder builder = new StringBuilder();
		builder.append("Info - DataSets that have no tasks built upon: ");
		for(Integer id: ids) {
			if(getNumberTasks(id) == 0) {
				builder.append(id + ", ");
			}
		}
		builder.append(System.lineSeparator());
		logger.logToFile(builder.toString());
	}
	
	/**
	 * Get the tasks associated with the dataset id. 
	 * Check whether there are 1 ore more tasks.
	 * 
	 * @param id - dataset id.
	 * @return -  number of tasks if there are tasks associated with the dataset, otherwise return 0.
	 * @throws Exception
	 */
	protected int getNumberTasks(int id) throws Exception {
		
		Map<String, String> filters = new HashMap<String, String>();
		filters.put("data_id", "" + id);
		try {
			Tasks tasks = connector.taskList(filters);
			return tasks.getTask().length;
		} catch(ApiException e) {
			if(e.getCode() == 482) {
				return 0;
			} else {
				throw e;
			}
		}
	}
}