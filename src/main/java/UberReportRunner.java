import google.GoogleOperations;

import java.io.*;
import java.util.*;

public class UberReportRunner {

    public static void main(String... args) throws IOException {
        IEmailProvider emailProvider = new GmailProvider("/uber-gmail-config.json");
        List<List<Object>> rows = emailProvider.generateReport();
        IReportWriter reportWriter = new GoogleSheetsReportWriter();
        reportWriter.write(rows);
    }

}