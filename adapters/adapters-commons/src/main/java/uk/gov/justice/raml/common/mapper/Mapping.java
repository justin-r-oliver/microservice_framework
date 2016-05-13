package uk.gov.justice.raml.common.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Mapping {
    private final Map<String, String> fields;

    private Mapping(Map<String, String> fields) {
        this.fields = fields;
    }

    public String get(final String fieldName) {
        return fields.get(fieldName);
    }

    public Set<String> getFieldNames() {
        return fields.keySet();
    }

    public static Builder buildMapping() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mapping)) return false;
        Mapping mapping = (Mapping) o;
        return Objects.equals(fields, mapping.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields);
    }

    public static class Builder {
        private final Map<String, String> fields;

        private Builder() {
            fields = new HashMap<>();
        }

        public Builder withField(final String fieldName, final String fieldValue) {
            fields.put(fieldName, fieldValue);
            return this;
        }

        public Mapping build() {
            return new Mapping(fields);
        }
    }

}
