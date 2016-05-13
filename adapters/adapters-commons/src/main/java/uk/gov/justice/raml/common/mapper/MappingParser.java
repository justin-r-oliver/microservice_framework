package uk.gov.justice.raml.common.mapper;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MappingParser {

    public static final String OUTPUT_TYPE_FIELD = "responseType";
    public static final String INPUT_TYPE_FIELD = "requestType";
    public static final String TYPE_FIELD = "type";
    public static final String NAME_FIELD = "name";

    private static final Pattern MAPPING_SECTION_PATTERN = Pattern
            .compile("(?!\\.\\.\\.)(.*)(?!\\.\\.\\.)", Pattern.DOTALL);
    private static final String FIELD_PATTERN = "%s: (.*)";
    private static final int FIELD_VALUE_GROUP = 1;

    private final List<String> fieldNames;
    private final Map<String, Pattern> patterns;

    public static MappingParser getMappingParser() {
        return new MappingParser(OUTPUT_TYPE_FIELD, TYPE_FIELD, NAME_FIELD);
    }

    public static MappingParser postMappingParser() {
        return new MappingParser(INPUT_TYPE_FIELD, TYPE_FIELD, NAME_FIELD);
    }

    private MappingParser(final String... fieldNames) {
        this.fieldNames = asList(fieldNames);
        this.patterns = new HashMap<>();
        this.fieldNames.stream()
                .forEach(fieldName -> patterns.put(fieldName, patternForField(fieldName)));
    }

    public List<Mapping> parseFromDescription(final String description) {
        if (description == null) {
            return Collections.emptyList();
        }

        final Matcher matcher = MAPPING_SECTION_PATTERN.matcher(description);

        matcher.find();
        return range(0, matcher.groupCount())
                .mapToObj(index -> parseMappingSection(matcher.group(index)))
                .flatMap(mappingStream -> mappingStream)
                .collect(Collectors.toList());
    }

    private Stream<Mapping> parseMappingSection(final String mappingSection) {
        return stream(mappingSection.split("\\(mapping\\):"))
                .filter(messagePart -> messagePart.contains(fieldNames.get(0)))
                .map(this::parseMapping);
    }

    private Mapping parseMapping(String mappingPart) {
        final Mapping.Builder builder = Mapping.buildMapping();

        fieldNames.stream().forEach(fieldName -> {
            final String fieldValue = parseFieldFrom(fieldName, mappingPart)
                    .orElseThrow(() -> new IllegalStateException(format("No %s: field set in description", fieldName)));
            builder.withField(fieldName, fieldValue);
        });

        return builder.build();
    }

    private Optional<String> parseFieldFrom(final String fieldName, final String mappingPart) {
        final Matcher matcher = patterns.get(fieldName).matcher(mappingPart);

        if (matcher.find()) {
            return Optional.of(matcher.group(FIELD_VALUE_GROUP));
        }

        return Optional.empty();
    }

    private Pattern patternForField(final String fieldName) {
        return Pattern.compile(format(FIELD_PATTERN, fieldName));
    }
}
