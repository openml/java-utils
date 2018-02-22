package org.openml.checkdatasets;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestValidator {

	Validator validator;
	
	@Before
	public void setUp() throws Exception {
		validator = new Validator();
	}

	@Ignore
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
	
	@Ignore
	@Test
	public void testValidateFeature() {
		File validArff = new File(this.getClass().getClassLoader().getResource("valid-arff.arff").getFile());
		File invalidArff = new File(this.getClass().getClassLoader().getResource("invalid-arff.arff").getFile());
		
	}

}
