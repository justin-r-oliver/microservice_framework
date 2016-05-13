package uk.gov.justice.services.messaging.logging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.StringReader;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.logging.JMSMessageLoggerHelper.toJMSTraceString;

@RunWith(MockitoJUnitRunner.class)
public class JMSMessageLoggerHelperTest {

    private static final String CORRELATION_ID = "1234-13snnkad-adda";
    private static final String JMS_DESTINATION = "some.where.over.the.rainbow";
    private static final String MESSAGE_ID = "asd2-asdef-2232n";

    @Mock
    Destination destination;

    @Mock
    Message message;

    @Mock
    Message messageThrowsException;



    @Before
    public void setUp() throws Exception {
        when(message.getJMSCorrelationID()).thenReturn(CORRELATION_ID);
        when(message.getJMSDestination()).thenReturn(destination);
        when(message.getJMSMessageID()).thenReturn(MESSAGE_ID);
        when(destination.toString()).thenReturn(JMS_DESTINATION);
        when(messageThrowsException.getJMSMessageID()).thenThrow(JMSException.class);
    }

    @Test
    public void shouldHaveCorrectFieldValues() {
        String result = toJMSTraceString(message);
        assertThat(result, containsString(CORRELATION_ID));
        assertThat(result, containsString(JMS_DESTINATION));
        assertThat(result, containsString(MESSAGE_ID));
    }

    @Test
    public void shouldPrintJMSExceptionAsJson() {

        StringReader sw = new StringReader(toJMSTraceString(messageThrowsException));
        JsonReader jsonReader = Json.createReader(sw);
        JsonObject result = jsonReader.readObject();

        assertThat(result.getString("messageID"), is(""));
        assertThat(result.getString("JMSDestination"), is(""));
        assertThat(result.getString("JMSCorrelationID"), is(""));
    }
}