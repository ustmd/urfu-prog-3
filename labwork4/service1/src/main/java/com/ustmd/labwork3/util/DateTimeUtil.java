package com.ustmd.labwork3.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeUtil {

    private DateTimeUtil() {
    }

    public static SimpleDateFormat getCustomFormat() {
        // SimpleDateFormat is mutable: each request must get its own instance.
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        format.setLenient(false);
        return format;
    }
}
