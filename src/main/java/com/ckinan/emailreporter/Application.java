package com.ckinan.emailreporter;

import com.ckinan.emailreporter.core.ConfigMapper;
import com.ckinan.emailreporter.core.EmailReporter;
import com.ckinan.emailreporter.core.providers.google.GmailReader;
import com.ckinan.emailreporter.core.providers.google.GoogleSheetsDataSource;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Application {

	public static void main(String[] args) throws IOException, GeneralSecurityException {
		new EmailReporter(
				new ConfigMapper("/uber-gmail-config.json"),
				new GoogleSheetsDataSource(),
				new GmailReader()
		).run();
	}

}
