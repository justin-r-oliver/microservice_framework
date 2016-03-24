package uk.gov.justice.services.example.cakeshop.event.listener;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(value = Component.EVENT_LISTENER)
public class CakeMadeEventListener {

    Logger logger = LoggerFactory.getLogger(CakeMadeEventListener.class);

    @Handles("cakeshop.events.cake-made")
    public void handle(final Envelope envelope) {

        logger.info("=============> Inside cake-made Event Listener");

    }
}
