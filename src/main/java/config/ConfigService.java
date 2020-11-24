package config;

import utils.DateUtils;

import java.util.Date;

public class ConfigService {

    private static final String dateQueryExpression = "<DATE_QUERY>";
    private static final String dateFormat = "yyyy/MM/dd";

    public static String calculateQuery(Long lastInternalDate, String query) {
        if (lastInternalDate != null) {
            return query.replaceAll(
                    dateQueryExpression,
                    " AND after:" + DateUtils.dateToString(new Date(lastInternalDate), dateFormat)
            );
        }
        return query.replaceAll(dateQueryExpression, "");
    }

}
