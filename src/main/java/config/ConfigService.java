package config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigService {

    private static final String dateQueryExpression = "<DATE_QUERY>";

    public static String calculateQuery(Long lastInternalDate, String query) {
        if (lastInternalDate != null) {
            query = query.replaceAll(dateQueryExpression, " AND after:" + ConfigService.dateToString(new Date(lastInternalDate)));
        } else {
            query = query.replaceAll(dateQueryExpression, "");
        }
        return query;
    }

    public static String dateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(date);
    }

}
