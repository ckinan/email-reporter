package com.ckinan.emailreporter.jobs;

import com.ckinan.emailreporter.core.ConfigMapper;
import com.ckinan.emailreporter.core.EmailReporter;
import com.ckinan.emailreporter.core.providers.google.GmailReader;
import com.ckinan.emailreporter.core.providers.google.GoogleSheetsDataSource;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.tinylog.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class UberReportJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            new EmailReporter(
                    new ConfigMapper("/uber-gmail-config.json"),
                    new GoogleSheetsDataSource(),
                    new GmailReader()
            ).run();
        } catch (IOException | GeneralSecurityException e) {
            Logger.error(e);
        }
    }

}
