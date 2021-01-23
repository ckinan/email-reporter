package com.ckinan.emailreporter.experimental;

import com.sendgrid.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;

public class EmailSender {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String SENDGRID_API_KEY = dotenv.get("SENDGRID_API_KEY");
    private static final String EMAIL_ADDRESS = dotenv.get("EMAIL_ADDRESS");

    public static void main(String[] args) throws IOException {
        Email from = new Email(EMAIL_ADDRESS);
        String subject = "Sending with Twilio SendGrid is Fun";
        Email to = new Email(EMAIL_ADDRESS);
        Content content = new Content("text/plain", "and easy to do anywhere, even with Java");
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException ex) {
            throw ex;
        }
    }

}
