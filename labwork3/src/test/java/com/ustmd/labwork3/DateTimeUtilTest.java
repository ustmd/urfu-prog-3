package com.ustmd.labwork3;

import com.ustmd.labwork3.util.DateTimeUtil;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeUtilTest {

    @Test
    void formatterUsesUtcAndExactlyThreeFractionalDigits() {
        SimpleDateFormat format = DateTimeUtil.getCustomFormat();

        assertThat(format.format(new Date(123L))).isEqualTo("1970-01-01T00:00:00.123Z");
        assertThat(format.getTimeZone().getRawOffset()).isZero();
        assertThat(format.getTimeZone().useDaylightTime()).isFalse();
    }

    @Test
    void formatterInstancesAreIndependentSoCallerChangesCannotAffectOtherRequests() {
        SimpleDateFormat first = DateTimeUtil.getCustomFormat();
        first.setTimeZone(TimeZone.getTimeZone("GMT+10:00"));
        first.applyPattern("yyyy");

        SimpleDateFormat second = DateTimeUtil.getCustomFormat();

        assertThat(second).isNotSameAs(first);
        assertThat(second.format(new Date(0L))).isEqualTo("1970-01-01T00:00:00.000Z");
    }

    @Test
    void separateRequestFormattersRemainCorrectWhenUsedConcurrently() throws Exception {
        var executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int index = 0; index < 200; index++) {
                long timestamp = index * 86400000L + index;
                tasks.add(() -> DateTimeUtil.getCustomFormat().format(new Date(timestamp)));
            }
            List<Future<String>> results = executor.invokeAll(tasks, 10, TimeUnit.SECONDS);

            for (int index = 0; index < results.size(); index++) {
                long timestamp = index * 86400000L + index;
                String expected = DateTimeUtil.getCustomFormat().format(new Date(timestamp));
                assertThat(results.get(index).get()).isEqualTo(expected);
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
