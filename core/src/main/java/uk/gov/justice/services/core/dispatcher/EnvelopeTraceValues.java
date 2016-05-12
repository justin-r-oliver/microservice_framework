package uk.gov.justice.services.core.dispatcher;

import java.util.List;
import java.util.UUID;

public class EnvelopeTraceValues {

    private UUID id;
    private String name;
    private List<UUID> causation;

    public EnvelopeTraceValues(UUID id, String name, List<UUID> causation) {
        this.id = id;
        this.name = name;
        this.causation = causation;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<UUID> getCausation() {
        return causation;
    }
}
