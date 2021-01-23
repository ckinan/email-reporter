package com.ckinan.emailreporter.core.providers.google;

import com.ckinan.emailreporter.core.IEmailReader;
import com.ckinan.emailreporter.core.providers.google.clients.GmailClient;
import com.ckinan.emailreporter.core.pojo.EmailMessage;
import com.ckinan.emailreporter.core.utils.DateUtils;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GmailReader implements IEmailReader {

    private final static String QUERY_EXPRESSION = "<DATE_QUERY>";
    private GmailClient gmailClient;

    public GmailReader() throws GeneralSecurityException, IOException {
        gmailClient = new GmailClient();
    }

    @Override
    public List<String> getPendingMessageIds(String query) throws IOException {
        List<String> messageIds = new ArrayList<>();
        // Search operators in Gmail: https://support.google.com/mail/answer/7190?hl=en
        List<Message> messages =
                gmailClient.getClient().users().messages().list("me").setQ(query).execute().getMessages();

        for(Message message: messages) {
            messageIds.add(message.getId());
        }

        return messageIds;
    }

    @Override
    public EmailMessage getEmailMessageById(String id) throws IOException {
        Message fullMessage = gmailClient.getClient().users().messages().get("me", id).setFormat("full").execute();
        EmailMessage message = new EmailMessage();
        message.setWatermark(fullMessage.getInternalDate());
        message.setBody(new String(Base64.decodeBase64(fullMessage.getPayload().getBody().getData().getBytes())));
        return message;
    }

    @Override
    public String calculateQuery(String template, Long watermark) {
        if (watermark != null) {
            return template.replaceAll(
                    QUERY_EXPRESSION,
                    " AND after:" + DateUtils.dateToString(new Date(watermark), "yyyy/MM/dd")
            );
        }
        return template.replaceAll(QUERY_EXPRESSION, "");
    }
}
