package uk.gov.justice.services.example.cakeshop.command.api;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;

import org.slf4j.Logger;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class MakeCakeCommandApi {

    private static final Logger LOGGER = getLogger(MakeCakeCommandApi.class);

    @Inject
    Sender sender;

    @Handles("cakeshop.commands.make-cake")
    public void handle(final Envelope envelope) {
        LOGGER.info("=============> Inside make-cake Command API");

        sender.send(envelope);
    }
}
