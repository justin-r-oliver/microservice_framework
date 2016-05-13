package uk.gov.justice.services.messaging.logging;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.json.Json;

public class JMSMessageLoggerHelper {

    public static String toJMSTraceString(final Message message) {

        String messageID = "";
        String jMSDestination = "";
        String jMSCorrelationId = "";

        try {
            messageID = message.getJMSMessageID();;
            jMSDestination = message.getJMSDestination().toString();;
            jMSCorrelationId = message.getJMSCorrelationID();
        } catch (JMSException e) {
            //no action, instead return JsonObject string
        }

        if(messageID == null) { messageID = "Unable to retrieve messageID from message";}
        if(jMSDestination == null) { jMSDestination = "Unable to retrieve Destination from message";}
        if(jMSCorrelationId == null) { jMSCorrelationId = "Unable to retrieve jMSCorrelationId from message";}

        return Json.createObjectBuilder()
                .add("messageID", messageID)
                .add("JMSDestination", jMSDestination)
                .add("JMSCorrelationID", jMSCorrelationId)
                .build().toString();
    }
}
