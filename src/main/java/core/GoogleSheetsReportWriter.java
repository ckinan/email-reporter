package core;

import google.GoogleOperations;

import java.io.IOException;
import java.util.List;

public class GoogleSheetsReportWriter implements IReportWriter {

    @Override
    public void write(List<List<Object>> rows) throws IOException {
        if(rows != null && rows.size() > 0) {
            GoogleOperations.appendRowsToSheets(rows);
            System.out.println("Rows were appended successfully.");
        } else {
            System.out.println("No rows to append.");
        }
    }

}
