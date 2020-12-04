package com.ckinan.core;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;
import com.ckinan.config.ConfigMapper;
import com.ckinan.google.GmailClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.ckinan.pojo.Field;
import com.ckinan.pojo.EmailMessage;
import com.ckinan.utils.DateUtils;
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

    private final static String QUERY_EXPRESSION = "<DATE_QUERY>";
    private ConfigMapper configMapper;
    private IReportDataSource reportDataSource;

    public EmailReporter(String configFile, IReportDataSource reportDataSource) throws IOException {
        this.configMapper = new ConfigMapper(configFile);
        this.reportDataSource = reportDataSource;
    }

    public Long getWatermark() throws IOException {
        Long watermark = null;
        List<Object> lastRow = reportDataSource.findLast();

        if(lastRow != null) {
            watermark = Long.parseLong(lastRow.get(0).toString());
        }

        return watermark;
    }

    public List<String> getPendingMessageIds(String query) throws IOException {
        List<String> messageIds = new ArrayList<>();
        // Search operators in Gmail: https://support.google.com/mail/answer/7190?hl=en
        List<Message> messages = GmailClient.CLIENT.users().messages().list("me").setQ(query).execute().getMessages();

        for(Message message: messages) {
            messageIds.add(message.getId());
        }

        return messageIds;
    }

    public List<List<Object>> calculateNewValues(List<String> messageIds, Long watermark) throws IOException {
        List<List<Object>> newValues = new ArrayList<>();

        for (String messageId : messageIds) {
            Message fullMessage = GmailClient.CLIENT.users().messages().get("me", messageId).setFormat("full").execute();
            EmailMessage message = new EmailMessage();
            message.setWatermark(fullMessage.getInternalDate());
            message.setBody(new String(Base64.decodeBase64(fullMessage.getPayload().getBody().getData().getBytes())));

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

    public String calculateQuery(Long watermark) {
        final String query = this.configMapper.getConfig().getQuery();

        if (watermark != null) {
            return query.replaceAll(
                    QUERY_EXPRESSION,
                    " AND after:" + DateUtils.dateToString(new Date(watermark), "yyyy/MM/dd")
            );
        }

        return query.replaceAll(QUERY_EXPRESSION, "");
    }

    public void run() throws IOException {
        Long watermark = this.getWatermark();
        String query = this.calculateQuery(watermark);
        List<String> messageIds = this.getPendingMessageIds(query);

        if (messageIds.isEmpty()) {
            logger.info("No messages found.");
            return;
        }

        // List of lists represents Rows and columns
        reportDataSource.save(this.calculateNewValues(messageIds, watermark));
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
            logger.info("Rows were appended successfully.");
        } else {
            logger.info("No rows to append.");
        }
    }

}
