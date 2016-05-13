package uk.gov.justice.services.messaging.logging;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.json.Json;


public class JMSMessageLoggerHelper {

    public static String toTraceString(final Message message) {

        String messageID = "";
        String jMSDestination = "";
        String jMSCorrelationId = "";

        try {
            messageID = message.getJMSMessageID();
            jMSDestination = message.getJMSDestination().toString();
            jMSCorrelationId = message.getJMSCorrelationID();
        } catch (JMSException e) {
            return Json.createObjectBuilder()
                    .add("ExceptionMessage", e.getMessage())
                    .build().toString();
        }

        return Json.createObjectBuilder()
                .add("messageID", messageID)
                .add("JMSDestination", jMSDestination)
                .add("JMSCorrelationID", jMSCorrelationId)
                .build().toString();
    }
}
