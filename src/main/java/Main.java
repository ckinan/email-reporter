import google.GoogleOperations;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String... args) throws IOException {
        IEmailProvider emailProvider = new GmailProvider("/uber-gmail-config.json");

        // List of lists represents Rows and columns
        List<List<Object>> newValues = emailProvider.generateReport();

        if(newValues != null && newValues.size() > 0) {
            GoogleOperations.appendRowsToSheets(newValues);
        } else {
            System.out.println("No rows to update.");
        }

        System.out.println("Done.");
    }

}