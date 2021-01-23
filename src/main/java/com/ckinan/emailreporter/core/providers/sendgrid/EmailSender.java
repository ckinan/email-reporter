package com.ckinan.emailreporter.core.providers.sendgrid;

import com.sendgrid.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.tinylog.Logger;

import java.io.IOException;

public class EmailSender {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String SENDGRID_API_KEY = dotenv.get("SENDGRID_API_KEY");
    private static final String EMAIL_ADDRESS = dotenv.get("EMAIL_ADDRESS");

    public static void send(String subject, String body) throws IOException {
        Email from = new Email(EMAIL_ADDRESS);
        Email to = new Email(EMAIL_ADDRESS);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            Logger.info(String.format("Email sent. Status code %s", response.getStatusCode()));
        } catch (IOException ex) {
            throw ex;
        }
    }

}
