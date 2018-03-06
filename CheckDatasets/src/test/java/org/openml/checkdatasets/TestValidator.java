package org.openml.checkdatasets;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataFeature;
import org.openml.apiconnector.xml.DataQuality;
import org.openml.apiconnector.xstream.XstreamXmlMapping;

import com.thoughtworks.xstream.XStreamException;

/**
 * @author Arlind Kadra
 * 
 * Unit tests for the Validator class.
 */
public class TestValidator {

	private final static String XSDSCHEMA = "openml.data.upload";
	Validator validator;
	File xsdSchema;
	
	@Before
	public void setUp() throws Exception {
		
		validator = new Validator();
		OpenmlConnector connector = new OpenmlConnector("https://www.openml.org/", "0bdf33abdecb6631fbc77b6978258a2c");
		xsdSchema = connector.getXSD(XSDSCHEMA);
		
	}

	@Test
	public void testValidateCSV() throws FileNotFoundException {
		
		File validCsv = new File(this.getClass().getClassLoader().getResource("." + File.separator + "csv" + File.separator + "valid.csv").getFile());
		assertTrue(validator.validateCSV(validCsv));
		
		
	}

	@Test
	public void testValidateARFF() throws IOException {
		
		File validArff = new File(this.getClass().getClassLoader().getResource("." + File.separator + "arff"+ File.separator + "valid.arff").getFile());
		File invalidArff = new File(this.getClass().getClassLoader().getResource("." + File.separator + "arff"+ File.separator + "invalid.arff").getFile());
		validator.validateARFF(validArff);
		try {
			validator.validateARFF(invalidArff);
			fail("Should have thrown an exception");
		} catch(IOException e) {
			// Do nothing, it is working
		}
	}
	
	@Test
	public void testValidateFeature() throws XStreamException {
		
		File validDataFeatures = new File(this.getClass().getClassLoader().getResource("." + File.separator + "features"+ File.separator + "valid-features.xml").getFile());
		File invalidDataFeatures = new File(this.getClass().getClassLoader().getResource("." + File.separator + "features"+ File.separator + "invalid-features.xml").getFile());
		DataFeature valid = (DataFeature) XstreamXmlMapping.getInstance().fromXML(validDataFeatures);
		DataFeature invalid = (DataFeature) XstreamXmlMapping.getInstance().fromXML(invalidDataFeatures);
		assertTrue(validator.validateFeature(valid));
		assertFalse(validator.validateFeature(invalid));
	}
	
	@Test
	public void testValidateQualities() throws XStreamException {
		
		File validQualities = new File(this.getClass().getClassLoader().getResource("." + File.separator + "qualities"+ File.separator + "valid-qualities.xml").getFile());
		File invalidQualities = new File(this.getClass().getClassLoader().getResource("." + File.separator + "qualities"+ File.separator + "invalid-qualities.xml").getFile());
		DataQuality valid = (DataQuality) XstreamXmlMapping.getInstance().fromXML(validQualities);
		DataQuality invalid = (DataQuality) XstreamXmlMapping.getInstance().fromXML(invalidQualities);
		assertTrue(validator.validateQualities(valid));
		assertFalse(validator.validateQualities(invalid));
	}
	
	@Test
	public void testValidateDatasetsOnSchema() {
		
		File validDescription = new File(this.getClass().getClassLoader().getResource("." + File.separator + "description"+ File.separator + "valid-description.xml").getFile());
		File invalidDescription = new File(this.getClass().getClassLoader().getResource("." + File.separator + "description"+ File.separator + "invalid-description.xml").getFile());
		assertTrue(validator.validateDatasetOnSchema(validDescription, xsdSchema));
		assertFalse(validator.validateDatasetOnSchema(invalidDescription, xsdSchema));
	}
	
	@Test
	public void testGetNumberTasks() throws Exception {
		
		assertTrue(validator.getNumberTasks(2) > 0);
		assertFalse(validator.getNumberTasks(41022) > 0);
	}
	
	public void testValidateDataType() {
		
		String[] types = {"nominal", "string", "date", "numeric"};
		String outlier = "Bang";
		
		for(String value: types) {
			assertTrue(validator.validateDataType(value));
		}
		assertFalse(validator.validateDataType(outlier));
	}
}