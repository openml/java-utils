package org.openml.checkdatasets;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openml.apiconnector.xml.DataFeature;
import org.openml.apiconnector.xstream.XstreamXmlMapping;

import com.thoughtworks.xstream.XStreamException;

public class TestValidator {

	Validator validator;
	
	@Before
	public void setUp() throws Exception {
		
		validator = new Validator();
	}

	@Test
	public void testValidateCSV() throws FileNotFoundException {
		
		File validCsv = new File(this.getClass().getClassLoader().getResource("valid-csv.csv").getFile());
		assertTrue(validator.validateCSV(validCsv));
		
		
	}

	@Test
	public void testValidateARFF() throws IOException {
		
		File validArff = new File(this.getClass().getClassLoader().getResource("valid-arff.arff").getFile());
		File invalidArff = new File(this.getClass().getClassLoader().getResource("invalid-arff.arff").getFile());
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
		
		File validDataFeatures = new File(this.getClass().getClassLoader().getResource("valid_data_features.xml").getFile());
		File invalidDataFeatures = new File(this.getClass().getClassLoader().getResource("invalid_data_features.xml").getFile());
		DataFeature valid = (DataFeature) XstreamXmlMapping.getInstance().fromXML(validDataFeatures);
		DataFeature invalid = (DataFeature) XstreamXmlMapping.getInstance().fromXML(invalidDataFeatures);
		assertTrue(validator.validateFeature(valid));
		assertFalse(validator.validateFeature(invalid));
	}
}