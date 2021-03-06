package uk.gov.justice.services.eventsourcing.repository.jdbc.eventlog;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidSequenceIdException;
import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryException;
import uk.gov.justice.services.test.utils.persistence.AbstractJdbcRepositoryIT;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

public class EventLogJdbcRepositoryIT extends AbstractJdbcRepositoryIT<EventLogJdbcRepository> {

    private static final UUID STREAM_ID = randomUUID();
    private static final Long SEQUENCE_ID = 5L;
    private static final String NAME = "Test Name";
    private static final String PAYLOAD_JSON = "{\"field\": \"Value\"}";
    private static final String METADATA_JSON = "{\"field\": \"Value\"}";
    private static final String LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML = "liquibase/event-store-db-changelog.xml";
    private final static ZonedDateTime TIMESTAMP = now();


    public EventLogJdbcRepositoryIT() {
        super(LIQUIBASE_EVENT_STORE_DB_CHANGELOG_XML);
    }

    @Before
    public void initializeDependencies() throws Exception {
        jdbcRepository = new EventLogJdbcRepository();
        registerDataSource();
    }

    @Test
    public void shouldStoreEventLogs() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventLogOf(SEQUENCE_ID, STREAM_ID));
        jdbcRepository.insert(eventLogOf(SEQUENCE_ID + 1, STREAM_ID));
        jdbcRepository.insert(eventLogOf(SEQUENCE_ID + 2, STREAM_ID));

        Stream<EventLog> eventLogs = jdbcRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);
        Stream<EventLog> eventLogs2 = jdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, SEQUENCE_ID + 1);
        Long latestSequenceId = jdbcRepository.getLatestSequenceIdForStream(STREAM_ID);

        assertThat(eventLogs.count(), equalTo(3L));
        assertThat(eventLogs2.count(), equalTo(2L));
        assertThat(latestSequenceId, equalTo(7L));
    }

    @Test
    public void shouldReturnEventsByStreamIdOrderedBySequenceId() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventLogOf(1, randomUUID()));
        jdbcRepository.insert(eventLogOf(7, STREAM_ID));
        jdbcRepository.insert(eventLogOf(4, STREAM_ID));
        jdbcRepository.insert(eventLogOf(2, STREAM_ID));

        Stream<EventLog> eventLogs = jdbcRepository.findByStreamIdOrderBySequenceIdAsc(STREAM_ID);

        final List<EventLog> eventLogList = eventLogs.collect(toList());
        assertThat(eventLogList, hasSize(3));
        assertThat(eventLogList.get(0).getSequenceId(), is(2l));
        assertThat(eventLogList.get(1).getSequenceId(), is(4l));
        assertThat(eventLogList.get(2).getSequenceId(), is(7l));

    }

    @Test
    public void shouldReturnEventsByStreamIdFromSequenceIdOrderBySequenceId() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventLogOf(5, randomUUID()));
        jdbcRepository.insert(eventLogOf(7, STREAM_ID));
        jdbcRepository.insert(eventLogOf(4, STREAM_ID));
        jdbcRepository.insert(eventLogOf(3, STREAM_ID));

        Stream<EventLog> eventLogs = jdbcRepository.findByStreamIdFromSequenceIdOrderBySequenceIdAsc(STREAM_ID, 4l);
        final List<EventLog> eventLogList = eventLogs.collect(toList());
        assertThat(eventLogList, hasSize(2));
        assertThat(eventLogList.get(0).getSequenceId(), is(4l));
        assertThat(eventLogList.get(1).getSequenceId(), is(7l));
    }

    @Test
    public void shouldReturnAllEventsOrderedBySequenceId() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventLogOf(1, randomUUID()));
        jdbcRepository.insert(eventLogOf(4, STREAM_ID));
        jdbcRepository.insert(eventLogOf(2, STREAM_ID));

        Stream<EventLog> eventLogs = jdbcRepository.findAll();

        final List<EventLog> eventLogList = eventLogs.collect(toList());
        assertThat(eventLogList, hasSize(3));
        assertThat(eventLogList.get(0).getSequenceId(), is(1l));
        assertThat(eventLogList.get(1).getSequenceId(), is(2l));
        assertThat(eventLogList.get(2).getSequenceId(), is(4l));

    }

    @Test
    public void shouldReturnStreamOfStreamIds() throws Exception {

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();
        jdbcRepository.insert(eventLogOf(1, streamId1));
        jdbcRepository.insert(eventLogOf(1, streamId2));
        jdbcRepository.insert(eventLogOf(1, streamId3));
        jdbcRepository.insert(eventLogOf(2, streamId1));

        Stream<UUID> streamIds = jdbcRepository.getStreamIds();

        final List<UUID> streamIdList = streamIds.collect(toList());

        assertThat(streamIdList, hasSize(3));
        assertThat(streamIdList, hasItem(streamId1));
        assertThat(streamIdList, hasItem(streamId2));
        assertThat(streamIdList, hasItem(streamId3));

    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateId() throws InvalidSequenceIdException {
        UUID id = randomUUID();
        jdbcRepository.insert(eventLogOf(id, SEQUENCE_ID));
        jdbcRepository.insert(eventLogOf(id, SEQUENCE_ID + 1));
    }

    @Test(expected = JdbcRepositoryException.class)
    public void shouldThrowExceptionOnDuplicateSequenceId() throws InvalidSequenceIdException {
        jdbcRepository.insert(eventLogOf(SEQUENCE_ID, STREAM_ID));
        jdbcRepository.insert(eventLogOf(SEQUENCE_ID, STREAM_ID));
    }

    private EventLog eventLogOf(final UUID id, final String name, final UUID streamId, final long sequenceId, final String payloadJSON, final String metadataJSON, final ZonedDateTime timestamp) {
        return new EventLog(id, streamId, sequenceId, name, metadataJSON, payloadJSON, timestamp);
    }

    private EventLog eventLogOf(final long sequenceId, final UUID streamId) {
        return eventLogOf(randomUUID(), NAME, streamId, sequenceId, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
    }

    private EventLog eventLogOf(UUID id, long sequenceId) {
        return eventLogOf(id, NAME, STREAM_ID, sequenceId, PAYLOAD_JSON, METADATA_JSON, TIMESTAMP);
    }

}