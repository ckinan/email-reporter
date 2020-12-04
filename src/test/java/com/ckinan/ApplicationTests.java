package com.ckinan;

import com.ckinan.core.EmailReporter;
import com.ckinan.core.GoogleSheetsDataSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

// @SpringBootTest
class ApplicationTests {

	@Test
	void runUberGmailTest() throws IOException {
		EmailReporter emailProvider = new EmailReporter("/uber-gmail-config.json", new GoogleSheetsDataSource());
		emailProvider.run();
	}

}
