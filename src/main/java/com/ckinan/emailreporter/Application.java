package com.ckinan.emailreporter;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.tinylog.Logger;

public class Application {

	public static void main(String[] args) throws SchedulerException {

		Logger.info("Starting application...");

		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.start();

		JobDetail job = JobBuilder.newJob(EmailReporterJob.class)
				.withIdentity("mainJob", "mainJobGroup")
				.build();

		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("mainTrigger", "mainTriggerGroup")
				.withSchedule(
						// Ref: http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html
						CronScheduleBuilder.cronSchedule("0 0,15,30,45 8-23 ? * *")
				)
				.build();

		StdSchedulerFactory.getDefaultScheduler().scheduleJob(job, trigger);

		Logger.info("Application started successfully!");

	}

}
