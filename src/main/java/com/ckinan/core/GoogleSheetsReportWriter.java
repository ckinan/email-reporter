package com.ckinan.core;

import com.ckinan.google.GoogleOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class GoogleSheetsReportWriter implements IReportWriter {

    Logger logger = LoggerFactory.getLogger(GoogleSheetsReportWriter.class);

    @Override
    public void write(List<List<Object>> rows) throws IOException {
        if(rows != null && rows.size() > 0) {
            GoogleOperations.appendRowsToSheets(rows);
            logger.info("Rows were appended successfully.");
        } else {
            logger.info("No rows to append.");
        }
    }

}
