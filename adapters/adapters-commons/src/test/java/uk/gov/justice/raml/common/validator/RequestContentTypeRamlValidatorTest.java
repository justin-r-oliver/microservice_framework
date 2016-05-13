package uk.gov.justice.raml.common.validator;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.HEAD;
import static org.raml.model.ActionType.OPTIONS;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ActionType.PUT;
import static uk.gov.justice.services.adapters.test.utils.builder.HttpActionBuilder.httpAction;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.services.adapters.test.utils.builder.HttpActionBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RequestContentTypeRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RamlValidator validator = new RequestContentTypeRamlValidator();

    @Test
    public void shouldPassIfMediaTypeContainsAValidCommand() throws Exception {

        validator.validate(
                raml()
                        .with(resource()
                                .with(HttpActionBuilder.httpAction(POST, "application/vnd.people.command.command1+json")))
                        .build());

    }

    @Test
    public void shouldIgnoreInvalidMediaTypesInNonPOSTActions() throws Exception {

        validator.validate(
                raml()
                        .with(resource()
                                .with(HttpActionBuilder.httpAction(GET, "application/vnd.structure.dummy.command1+json"))
                                .with(HttpActionBuilder.httpAction(POST, "application/vnd.structure.command.command2+json"))
                                .with(HttpActionBuilder.httpAction(HEAD, "application/vnd.structure.dummy.command3+json"))
                                .with(HttpActionBuilder.httpAction(PUT, "application/vnd.structure.dummy.command4+json"))
                                .with(HttpActionBuilder.httpAction(OPTIONS, "application/vnd.structure.dummy.command5+json"))
                        )
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Request type not set");

        validator.validate(raml()
                .with(resource()
                        .with(httpAction().withHttpActionType(POST)))
                .build());

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainAValidCommand() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.people.invalid.command1+json");

        validator.validate(
                raml()
                        .with(resource()
                                .with(HttpActionBuilder.httpAction(POST, "application/vnd.people.invalid.command1+json")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfNotvalidMediaType() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: nd.people.command.command1+json");

        validator.validate(
                raml()
                        .with(resource()
                                .with(HttpActionBuilder.httpAction(POST, "nd.people.command.command1+json")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfNotvalidMediaType2() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: nd.people.unknown.command1+nonjson");

        validator.validate(
                raml()
                        .with(resource()
                                .with(HttpActionBuilder.httpAction(POST, "nd.people.unknown.command1+nonjson")))
                        .build());

    }

    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainContext() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid request type: application/vnd.handlers.command1+json");

        validator.validate(
                raml()
                        .with(resource()
                                .with(HttpActionBuilder.httpAction(POST, "application/vnd.handlers.command1+json")))
                        .build());

    }

}
