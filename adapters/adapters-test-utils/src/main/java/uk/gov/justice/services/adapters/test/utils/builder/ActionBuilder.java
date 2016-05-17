package uk.gov.justice.services.adapters.test.utils.builder;

import static java.util.Arrays.stream;
import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static org.raml.model.ParamType.STRING;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.Response;
import org.raml.model.parameter.QueryParameter;

public class ActionBuilder {
    private static final String POST_MAPPING_ANNOTATION = "...\n" +
            "(mapping):\n" +
            "    inputType: application/vnd.structure.command.test-cmd+json\n" +
            "    type: command\n" +
            "    name: structure.test-cmd\n" +
            "...\n";

    private static final String GET_MAPPING_ANNOTATION = "...\n" +
            "(mapping):\n" +
            "    outputType: application/vnd.ctx.query.defquery+json\n" +
            "    type: query\n" +
            "    name: ctx.test-cmd\n" +
            "...\n";

    private final Map<String, MimeType> body = new HashMap<>();
    private final Map<String, QueryParameter> queryParameters = new HashMap<>();
    private ActionType actionType;
    private List<Response> responses = new LinkedList<>();
    private String description;

    public static ActionBuilder action() {
        return new ActionBuilder();
    }

    public static ActionBuilder defaultPostAction() {
        return action(POST, "application/vnd.structure.command.test-cmd+json")
                .withDescription(POST_MAPPING_ANNOTATION);
    }

    public static ActionBuilder defaultGetAction() {
        return action().withActionType(GET)
                .withDefaultResponseType()
                .withDescription(GET_MAPPING_ANNOTATION);
    }

    public static ActionBuilder action(final ActionType actionType, final String... mimeTypes) {
        ActionBuilder actionBuilder = new ActionBuilder()
                .withActionType(actionType);
        for (String mimeType : mimeTypes) {
            actionBuilder = actionBuilder.withMediaType(mimeType);
        }
        return actionBuilder;
    }

    public ActionBuilder withActionType(final ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public ActionBuilder withActionOfDefaultRequestType() {
        return withMediaType("application/vnd.ctx.command.defcmd+json");
    }

    public ActionBuilder withDefaultResponseType() {
        return withActionWithResponseTypes("application/vnd.ctx.query.defquery+json");
    }

    public ActionBuilder withActionWithResponseTypes(final String... responseTypes) {
        Response response = new Response();
        return withActionResponse(response, responseTypes);
    }

    public ActionBuilder withActionResponse(final Response response, final String... responseTypes) {
        Map<String, MimeType> respBody = new HashMap<>();
        for (String responseType : responseTypes) {
            respBody.put(responseType, new MimeType(responseType));
        }
        response.setBody(respBody);
        responses.add(response);
        return this;
    }

    public ActionBuilder withQueryParameters(final QueryParameter... queryParameters) {
        stream(queryParameters).forEach(queryParameter -> this.queryParameters.put(queryParameter.getDisplayName(), queryParameter));
        return this;
    }

    public ActionBuilder withQueryParameters(final String... paramNames) {
        stream(paramNames).forEach(paramName -> {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setDisplayName(paramName);
            queryParameter.setType(STRING);
            queryParameter.setRequired(true);
            this.queryParameters.put(paramName, queryParameter);
        });

        return this;
    }

    public ActionBuilder withOptionalQueryParameters(final String... paramNames) {
        stream(paramNames).forEach(paramName -> {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setDisplayName(paramName);
            queryParameter.setType(STRING);
            queryParameter.setRequired(false);
            this.queryParameters.put(paramName, queryParameter);
        });

        return this;
    }

    public ActionBuilder withMediaType(final MimeType mimeType) {
        body.put(mimeType.toString(), mimeType);
        return this;
    }

    public ActionBuilder withMediaType(final String stringMimeType) {
        return withMediaType(new MimeType(stringMimeType));
    }

    public ActionBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public Action build() {
        final Action action = new Action();
        action.setType(actionType);
        action.setDescription(description);
        action.setBody(body);

        HashMap<String, Response> responsesMap = new HashMap<>();
        this.responses.forEach(r -> responsesMap.put("200", r));
        action.setResponses(responsesMap);

        action.setQueryParameters(queryParameters);

        return action;
    }
}
