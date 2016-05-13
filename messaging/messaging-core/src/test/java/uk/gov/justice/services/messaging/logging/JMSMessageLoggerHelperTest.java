package uk.gov.justice.services.messaging.logging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JMSMessageLoggerHelperTest {

    private static final String CORRELATION_ID = "1234-13snnkad-adda";
    private static final String JMS_DESTINATION = "some.where.over.the.rainbow";
    private static final String MESSAGE_ID = "asd2-asdef-2232n";
    private static final String ERROR_MSG = "the bells the bells!";

    @Mock
    Destination destination;

    @Mock
    Message message;

    @Mock
    Message messageThrowsException;

    @Mock
    JMSException jmsException;

    @Before
    public void setUp() throws Exception {
        when(message.getJMSCorrelationID()).thenReturn(CORRELATION_ID);
        when(message.getJMSDestination()).thenReturn(destination);
        when(message.getJMSMessageID()).thenReturn(MESSAGE_ID);
        when(destination.toString()).thenReturn(JMS_DESTINATION);
        when(messageThrowsException.getJMSMessageID()).thenThrow(jmsException);
        when(jmsException.getMessage()).thenReturn(ERROR_MSG);
    }

    @Test
    public void shouldHaveCorrectFieldValues() {
        String result = JMSMessageLoggerHelper.toTraceString(message);
        assertThat(result, containsString(CORRELATION_ID));
        assertThat(result, containsString(JMS_DESTINATION));
        assertThat(result, containsString(MESSAGE_ID));
    }

    @Test
    public void shouldPrintJMSExceptionAsJson() {
        String result = JMSMessageLoggerHelper.toTraceString(messageThrowsException);
        assertThat(result, containsString(ERROR_MSG));
    }
}