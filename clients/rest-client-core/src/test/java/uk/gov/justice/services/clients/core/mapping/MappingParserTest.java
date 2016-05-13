package uk.gov.justice.services.clients.core.mapping;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.clients.core.mapping.Mapping.buildMapping;
import static uk.gov.justice.services.clients.core.mapping.MappingParser.getMappingParser;
import static uk.gov.justice.services.clients.core.mapping.MappingParser.postMappingParser;

import java.util.List;

import org.junit.Test;

public class MappingParserTest {

    private static final String POST_MAPPING_PART =
            "        (mapping):\n" +
                    "            inputType: %s\n" +
                    "            type: %s\n" +
                    "            name: %s\n";

    private static final String[] POST_FIELD_NAMES = new String[]{"inputType", "type", "name"};

    private static final Mapping POST_MAPPING_1 = buildMapping()
            .withField("inputType", "mediaType1")
            .withField("type", "type1")
            .withField("name", "name1")
            .build();

    private static final Mapping POST_MAPPING_2 = buildMapping()
            .withField("inputType", "mediaType2")
            .withField("type", "type2")
            .withField("name", "name2")
            .build();

    private static final String GET_MAPPING_PART =
            "        (mapping):\n" +
                    "            outputType: %s\n" +
                    "            type: %s\n" +
                    "            name: %s\n";

    private static final String[] GET_FIELD_NAMES = new String[]{"outputType", "type", "name"};

    private static final Mapping GET_MAPPING_1 = buildMapping()
            .withField("outputType", "mediaType1")
            .withField("type", "type1")
            .withField("name", "name1")
            .build();

    private static final Mapping GET_MAPPING_2 = buildMapping()
            .withField("outputType", "mediaType2")
            .withField("type", "type2")
            .withField("name", "name2")
            .build();

    @Test
    public void shouldReturnNoMappingsForBlankDescription() throws Exception {
        List<Mapping> mappings = postMappingParser().parseFromDescription("");
        assertThat(mappings.size(), is(0));
    }

    @Test
    public void shouldReturnNoMappingsForNullDescription() throws Exception {
        List<Mapping> mappings = postMappingParser().parseFromDescription(null);
        assertThat(mappings.size(), is(0));
    }

    @Test
    public void shouldReturnNoMappingsForDescriptionWithNoMappings() throws Exception {
        List<Mapping> mappings = postMappingParser().parseFromDescription("Description\n with no mappings\n");
        assertThat(mappings.size(), is(0));
    }

    @Test
    public void shouldReturnNoMappingsForDescriptionWithEmptyMappingSection() throws Exception {
        List<Mapping> mappings = postMappingParser().parseFromDescription("...\n \n...\n");
        assertThat(mappings.size(), is(0));
    }

    @Test
    public void shouldReturnSingleMappingFromDescriptionWithSingleMappingDefined() throws Exception {
        String description = descriptionWithMappingsForPost(POST_MAPPING_1);

        List<Mapping> mappings = postMappingParser().parseFromDescription(description);

        assertThat(mappings, containsInAnyOrder(POST_MAPPING_1));
    }

    @Test
    public void shouldReturnMultipleMappingsFromDescriptionWithSingleMappingDefined() throws Exception {
        String description = descriptionWithMappingsForPost(POST_MAPPING_1, POST_MAPPING_2);

        List<Mapping> mappings = postMappingParser().parseFromDescription(description);

        assertThat(mappings, containsInAnyOrder(POST_MAPPING_1, POST_MAPPING_2));
    }

    @Test
    public void shouldReturnMultipleMappingsFromGetDescriptionWithSingleMappingDefined() throws Exception {
        String description = descriptionWithMappingsForGet(GET_MAPPING_1, GET_MAPPING_2);

        List<Mapping> mappings = getMappingParser().parseFromDescription(description);

        assertThat(mappings, containsInAnyOrder(GET_MAPPING_1, GET_MAPPING_2));
    }

    @Test
    public void shouldReturnFromMultipleMappingSectionsInDescription() throws Exception {
        String description = "Pre Text\n" +
                descriptionWithMappingsForPost(POST_MAPPING_1) +
                "middle text\n" +
                descriptionWithMappingsForPost(POST_MAPPING_2) +
                "post text.\n";

        List<Mapping> mappings = postMappingParser().parseFromDescription(description);

        assertThat(mappings, containsInAnyOrder(POST_MAPPING_1, POST_MAPPING_2));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfExpectedFieldsAreNotPresent() throws Exception {
        String description = "...\n" +
                "        (mapping):\n" +
                "            inputType: %s\n" +
                "            type: %s\n" +
                "...\n";

        postMappingParser().parseFromDescription(description);
    }

    private String descriptionWithMappingsForPost(final Mapping... mappings) {
        return descriptionWithMappings(POST_MAPPING_PART, POST_FIELD_NAMES, mappings);
    }

    private String descriptionWithMappingsForGet(final Mapping... mappings) {
        return descriptionWithMappings(GET_MAPPING_PART, GET_FIELD_NAMES, mappings);
    }

    private String descriptionWithMappings(final String mappingPart, final String[] fieldNames, final Mapping... mappings) {
        StringBuilder builder = new StringBuilder();
        builder.append("...\n");

        stream(mappings).forEach(mapping -> builder.append(
                format(mappingPart,
                        mapping.get(fieldNames[0]),
                        mapping.get(fieldNames[1]),
                        mapping.get(fieldNames[2]))));

        builder.append("...\n");
        return builder.toString();
    }
}
