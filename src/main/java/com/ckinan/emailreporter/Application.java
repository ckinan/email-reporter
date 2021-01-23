package com.ckinan.emailreporter;

import com.ckinan.emailreporter.core.providers.google.clients.GoogleClientInitiator;
import com.ckinan.emailreporter.jobs.UberReportJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public class Application {

	public static void main(String[] args) throws SchedulerException, GeneralSecurityException, IOException {

		// TODO: These checks should not exist here.
		if(!Files.exists(Paths.get("src/main/resources/credentials.json"))) {
			Logger.error("Please provide credentials.json");
			return;
		}

		if(!Files.exists(Paths.get("tokens/StoredCredential"))) {
			Logger.info("File: tokens/StoredCredential doesn't exist. Proceeding to OAuth");
			new GoogleClientInitiator();
		}

		Logger.info("Starting application...");

		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.start();

		JobDetail job = JobBuilder.newJob(UberReportJob.class)
				.withIdentity("mainJob", "mainJobGroup")
				.build();

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("mainTrigger", "mainTriggerGroup")
				.withSchedule(
						// Ref: http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html
						CronScheduleBuilder.cronSchedule("0 0,15,30,45 8-23 ? * *")
						// CronScheduleBuilder.cronSchedule("0 * 8-23 ? * *") // For local tests
				)
				.build();

		StdSchedulerFactory.getDefaultScheduler().scheduleJob(job, trigger);

		Logger.info("Application started successfully!");

	}

}
