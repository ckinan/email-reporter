import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;
import config.ConfigMapper;
import google.GmailClient;
import google.GoogleOperations;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pojo.Field;
import pojo.GmailMessage;
import us.codecraft.xsoup.Xsoup;
import utils.DateUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GmailProvider implements IEmailProvider {

    private final static String QUERY_EXPRESSION = "<DATE_QUERY>";

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
            GmailMessage message = this.getMessage(messageId);

            if(watermark != null && message.getInternalDate() > watermark){
                System.out.println("Processing message: " + messageId);
                newValues.add(this.calculateRowValues(message.getInternalDate(), message.getBody()));
            } else {
                System.out.println("Skipping message: " + messageId);
            }
        }

        Collections.sort(newValues, Comparator.comparingLong(value -> (long) value.get(0)));
        return newValues;
    }

    @Override
    public String calculateQuery(Long watermark) {
        final String query = ConfigMapper.CONFIG.getQuery();

        if (watermark != null) {
            return query.replaceAll(
                    QUERY_EXPRESSION,
                    " AND after:" + DateUtils.dateToString(new Date(watermark), "yyyy/MM/dd")
            );
        }

        return query.replaceAll(QUERY_EXPRESSION, "");
    }

    private List<Object> calculateRowValues(Long internalDate, String body) {
        Document document = Jsoup.parse(body);

        List<Object> cellValues = new ArrayList<>();
        cellValues.add(internalDate);

        for(Field field: ConfigMapper.CONFIG.getFields()) {
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
        // TODO : it should live somewhere else, perhaps its own util class?
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

    private GmailMessage getMessage(String messageId) throws IOException {
        Message fullMessage = GmailClient.CLIENT.users().messages().get("me", messageId).setFormat("full").execute();
        GmailMessage gmailMessage = new GmailMessage();
        gmailMessage.setInternalDate(fullMessage.getInternalDate());
        gmailMessage.setBody(new String(Base64.decodeBase64(fullMessage.getPayload().getBody().getData().getBytes())));
        return gmailMessage;
    }

}
