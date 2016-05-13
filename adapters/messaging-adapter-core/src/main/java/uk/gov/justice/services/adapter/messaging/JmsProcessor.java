package uk.gov.justice.services.adapter.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.adapter.messaging.exception.InvalildJmsMessageTypeException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.logging.JsonEnvelopeLoggerHelper;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import static uk.gov.justice.services.messaging.logging.JMSMessageLoggerHelper.toTraceString;

/**
 * In order to minimise the amount of generated code in the JMS Listener implementation classes,
 * this service encapsulates all the logic for validating a message and converting it to an
 * envelope, passing it to a consumer.  This allows testing of this logic independently from the
 * automated generation code.
 */
public class JmsProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsProcessor.class);

    @Inject
    EnvelopeConverter envelopeConverter;

    /**
     * Process an incoming JMS message by validating the message and then passing the envelope
     * converted from the message to the given consumer.
     *
     * @param consumer a consumer for the envelope
     * @param message  a message to be processed
     */
    public void process(final Consumer<JsonEnvelope> consumer, final Message message) {
        LOGGER.trace("Processing message  pre dispatching {}", toTraceString(message));
        if (!(message instanceof TextMessage)) {
            try {
                throw new InvalildJmsMessageTypeException(String.format("Message is not an instance of TextMessage %s", message.getJMSMessageID()));
            } catch (JMSException e) {
                throw new InvalildJmsMessageTypeException(String.format("Message is not an instance of TextMessage. Failed to retrieve messageId %s",
                        message), e);
            }
        }

        JsonEnvelope jsonEnvelope = envelopeConverter.fromMessage((TextMessage) message);
        LOGGER.trace("Message converted to JsonEnvelope pre dispatching {}", JsonEnvelopeLoggerHelper.toTraceString(jsonEnvelope));
        consumer.accept(jsonEnvelope);
        LOGGER.trace("JsonEnvelope accepted by {} pre dispatching {}", consumer.getClass().toString(), JsonEnvelopeLoggerHelper.toTraceString(jsonEnvelope));
    }


}
