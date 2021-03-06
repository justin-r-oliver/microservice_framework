package uk.gov.justice.services.common.util;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.now;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.json.JsonString;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DateTimeProviderTest {

    @InjectMocks
    private DateTimeProvider dateTimeProvider;

    @Test
    public void shouldGetANewZonedDateTimeWithCurrentTime() throws Exception {

        final ZonedDateTime zonedDateTime = dateTimeProvider.now();

        assertThat(zonedDateTime, is(notNullValue()));

        assertThat(zonedDateTime.isAfter(now().minusSeconds(2L)), is(true));
        assertThat(zonedDateTime.getZone(), is(UTC));
    }

    @Test
    public void shouldConvertStringToUtc() {
        final ZonedDateTime dateTime = dateTimeProvider.fromString("2016-01-21T23:42:03.522+07:00");
        assertThat(dateTime.getZone(), is(ZoneId.of("UTC").normalized()));
    }

    @Test
    public void shouldConvertJsonStringToUtc() {
        final ZonedDateTime dateTime = dateTimeProvider.fromJsonString(createJsonString("2016-01-21T23:42:03.522+07:00"));
        assertThat(dateTime.getZone(), is(ZoneId.of("UTC").normalized()));
    }

    @Test
    public void shouldKeepCorrectTimeWhenConvertingStringToUtc() {
        final ZonedDateTime dateTime = dateTimeProvider.fromJsonString(createJsonString("2016-01-21T23:42:03.522+07:00"));
        assertThat(dateTime.toInstant(), is(ZonedDateTime.parse("2016-01-21T16:42:03.522Z").toInstant()));
    }

    @Test
    public void shouldConvertNonUtcToUtcString() {
        final String dateTime = dateTimeProvider.toString(ZonedDateTime.parse("2016-01-21T23:42:03.522+07:00"));
        assertThat(dateTime, is("2016-01-21T16:42:03.522Z"));
    }

    @Test
    public void shouldConvertTimestampToZoneDateTime() {
        final Timestamp dateTime = dateTimeProvider.toSqlTimestamp(ZonedDateTime.of(2016, 12, 31, 23, 59, 59, 0, UTC));
        assertThat(dateTime.toString(), equalTo("2016-12-31 23:59:59.0"));
    }

    @Test
    public void shouldConertZoneDateTimeToTimestamp() {
        final ZonedDateTime dateTime = dateTimeProvider.fromSqlTimestamp(Timestamp.valueOf("2016-12-31 23:59:59.123"));
        assertThat(dateTime.toString(), equalTo("2016-12-31T23:59:59.123Z"));
    }

    private JsonString createJsonString(final String source) {
        return createObjectBuilder().add("tmp", source).build().getJsonString("tmp");
    }
}
