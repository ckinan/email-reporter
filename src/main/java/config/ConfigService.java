package config;

import utils.DateUtils;

import java.util.Date;

public class ConfigService {

    private static final String dateQueryExpression = "<DATE_QUERY>";
    private static final String dateFormat = "yyyy/MM/dd";

    public static String calculateQuery(Long watermark) {
        final String query = ConfigMapper.CONFIG.getQuery();

        if (watermark != null) {
            // TODO : this query should NOT be tied to Gmail
            return query.replaceAll(
                    dateQueryExpression,
                    " AND after:" + DateUtils.dateToString(new Date(watermark), dateFormat)
            );
        }

        return query.replaceAll(dateQueryExpression, "");
    }

}
