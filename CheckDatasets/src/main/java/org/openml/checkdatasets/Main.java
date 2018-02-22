package org.openml.checkdatasets;

import org.openml.checkdatasets.Validator;

// Initializer class.
public class Main {

	public static void main(String[] args) throws Exception {
		
		new Validator().performAllChecks();
	}
}
