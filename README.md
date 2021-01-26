# email-reporter

A simple application that generates reports based on the content of email messages.

## Use case: Step by step

- Read the last row generated from the datasource (currently Google spreadsheets is the only datasource implemented).
- Read emails from Gmail inbox (currently it's the only email provider that has been implemented) and start the report
from the last email message processed based on what the datasource has (there is a unique identifier, aka "watermark").
- Parse the data based on the templates (*.json files per each report type), currently it only supports receipts from Uber trips. Use XPath to parse the information that is needed.
- Finally, store the report in the datasource (the spreadsheet).

## TODOs

- Create .env
- Create credentials.json
- OAuth2 flow

## Third parties

- Setup logging: [Tinylog](https://tinylog.org/v2/)
- Setup crontab: Quartz

## Run

```
mvn clean install
java -Duser.timezone=America/Lima -jar target/email-reporter.jar &
```

