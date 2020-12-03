package com.ckinan;

import com.ckinan.core.GmailProvider;
import com.ckinan.core.GoogleSheetsReportWriter;
import com.ckinan.core.IEmailProvider;
import com.ckinan.core.IReportWriter;
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
	public void run() throws IOException {
		IEmailProvider emailProvider = new GmailProvider("/uber-gmail-config.json");
		List<List<Object>> rows = emailProvider.generateReport();
		IReportWriter reportWriter = new GoogleSheetsReportWriter();
		reportWriter.write(rows);
	}

}
