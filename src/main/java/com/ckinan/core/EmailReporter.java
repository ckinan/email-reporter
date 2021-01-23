package com.ckinan.core;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.ckinan.pojo.Field;
import com.ckinan.pojo.EmailMessage;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailReporter {

    Logger logger = LoggerFactory.getLogger(EmailReporter.class);

    private ConfigMapper configMapper;
    private IReportDataSource reportDataSource;
    private IEmailReader emailReader;

    public EmailReporter(ConfigMapper configMapper, IReportDataSource reportDataSource, IEmailReader emailReader) throws IOException {
        this.configMapper = configMapper;
        this.reportDataSource = reportDataSource;
        this.emailReader = emailReader;
    }

    public void run() throws IOException {
        Long watermark = this.getWatermark();
        String query = emailReader.calculateQuery(this.configMapper.getConfig().getQuery(), watermark);
        List<String> messageIds = emailReader.getPendingMessageIds(query);

        if (messageIds.isEmpty()) {
            logger.info("No messages found.");
            return;
        }

        write(this.calculateNewValues(messageIds, watermark));
    }

    private Long getWatermark() throws IOException {
        Long watermark = null;
        List<Object> lastRow = reportDataSource.findLast();

        if(lastRow != null) {
            watermark = Long.parseLong(lastRow.get(0).toString());
        }

        return watermark;
    }

    private List<List<Object>> calculateNewValues(List<String> messageIds, Long watermark) throws IOException {
        List<List<Object>> newValues = new ArrayList<>();

        for (String messageId : messageIds) {
            EmailMessage message = emailReader.getEmailMessageById(messageId);

            if(watermark != null && message.getWatermark() > watermark){
                logger.info("Processing message: " + messageId);
                newValues.add(this.calculateRowValues(message.getWatermark(), message.getBody()));
            } else {
                logger.info("Skipping message: " + messageId);
            }
        }

        Collections.sort(newValues, Comparator.comparingLong(value -> (long) value.get(0)));
        return newValues;
    }

    private List<Object> calculateRowValues(Long internalDate, String body) {
        Document document = Jsoup.parse(body);

        List<Object> cellValues = new ArrayList<>();
        cellValues.add(internalDate);

        for(Field field: this.configMapper.getConfig().getFields()) {
            String value = this.readDocument(document, field.getXpath());
            if(field.getRegex() != null) {
                Pattern compile = Pattern.compile(field.getRegex());
                Matcher matcher = compile.matcher(value);
                if(matcher.find()) {
                    value = matcher.group("matcherGroup");
                }
            }
            cellValues.add(value);
        }

        return cellValues;
    }

    private String readDocument(Document doc, String xpath) {
        List<Element> elements = Xsoup.compile(xpath).evaluate(doc).getElements();
        for(Element e: elements) {
            if("img".equals(e.tagName()) && xpath.endsWith("@src")) {
                return e.attributes().get("src");
            } else {
                return e.text();
            }
        }
        return null;
    }

    private void write(List<List<Object>> rows) throws IOException {
        if(rows != null && rows.size() > 0) {
            reportDataSource.save(rows);
            logger.info(rows.size() + " rows were appended successfully.");
        } else {
            logger.info("No rows to append.");
        }
    }

}
