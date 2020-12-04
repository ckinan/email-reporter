package com.ckinan;

import com.ckinan.core.EmailReporter;
import com.ckinan.core.GoogleSheetsDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Scheduled(cron="${cron.expression}", zone="${cron.zone}")
	public void runUberGmail() throws IOException {
		EmailReporter emailProvider = new EmailReporter("/uber-gmail-config.json", new GoogleSheetsDataSource());
		emailProvider.run();
	}

}
