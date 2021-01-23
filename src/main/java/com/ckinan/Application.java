package com.ckinan;

import com.ckinan.core.ConfigMapper;
import com.ckinan.core.EmailReporter;
import com.ckinan.core.GmailReader;
import com.ckinan.core.GoogleSheetsDataSource;

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
