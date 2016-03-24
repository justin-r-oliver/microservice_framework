package uk.gov.justice.services.example.cakeshop.command.api;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

/**
 *
 */
@ServiceComponent(COMMAND_API)
public class AddRecipeCommandApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddRecipeCommandApi.class);
    private static final String FIELD_RECIPE_ID = "recipeId";

    @Inject
    Sender sender;

    @Handles("cakeshop.commands.add-recipe")
    public void addRecipe(final Envelope command) {
        LOGGER.info("=============> Inside add-recipe Command API. RecipeId: " + command.payload().getString(FIELD_RECIPE_ID));

        sender.send(command);
    }

}
