import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;
import config.ConfigMapper;
import google.GmailClient;
import google.GoogleOperations;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pojo.Field;
import pojo.GmailMessage;
import utils.DateUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GmailProvider extends AbstractEmailProvider implements IEmailProvider {

    private final static String QUERY_EXPRESSION = "<DATE_QUERY>";
    private ConfigMapper configMapper;

    public GmailProvider(String configFile) throws IOException {
        this.configMapper = new ConfigMapper(configFile);
    }

    @Override
    public Long getWatermark() throws IOException {
        Long watermark = null;
        List<Object> lastRow = GoogleOperations.getLastRow();

        if(lastRow != null) {
            watermark = Long.parseLong(lastRow.get(0).toString());
        }

        return watermark;
    }

    @Override
    public List<String> getPendingMessageIds(String query) throws IOException {
        List<String> messageIds = new ArrayList<>();
        // Search operators in Gmail: https://support.google.com/mail/answer/7190?hl=en
        List<Message> messages = GmailClient.CLIENT.users().messages().list("me").setQ(query).execute().getMessages();

        for(Message message: messages) {
            messageIds.add(message.getId());
        }

        return messageIds;
    }

    @Override
    public List<List<Object>> calculateNewValues(List<String> messageIds, Long watermark) throws IOException {
        List<List<Object>> newValues = new ArrayList<>();

        for (String messageId : messageIds) {
            Message fullMessage = GmailClient.CLIENT.users().messages().get("me", messageId).setFormat("full").execute();
            GmailMessage message = new GmailMessage();
            message.setWatermark(fullMessage.getInternalDate());
            message.setBody(new String(Base64.decodeBase64(fullMessage.getPayload().getBody().getData().getBytes())));

            if(watermark != null && message.getWatermark() > watermark){
                System.out.println("Processing message: " + messageId);
                newValues.add(this.calculateRowValues(message.getWatermark(), message.getBody()));
            } else {
                System.out.println("Skipping message: " + messageId);
            }
        }

        Collections.sort(newValues, Comparator.comparingLong(value -> (long) value.get(0)));
        return newValues;
    }

    @Override
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

    @Override
    public List<List<Object>> generateReport() throws IOException {
        Long watermark = this.getWatermark();
        String query = this.calculateQuery(watermark);
        List<String> messageIds = this.getPendingMessageIds(query);

        if (messageIds.isEmpty()) {
            System.out.println("No messages found.");
            return null;
        }

        // List of lists represents Rows and columns
        return this.calculateNewValues(messageIds, watermark);
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

}
