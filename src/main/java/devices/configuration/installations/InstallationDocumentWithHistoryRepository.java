package devices.configuration.installations;

import devices.configuration.tools.EventTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
class InstallationDocumentWithHistoryRepository implements InstallationRepository {

    private final DocumentRepository documents;
    private final EventRepository events;
    private final ApplicationEventPublisher publisher;

    @Override
    public InstallationProcess getByOrderId(String orderId) {
        return documents.findByOrderId(orderId)
                .map(InstallationEntity::getProcess)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public Optional<InstallationProcess> getByDeviceId(String deviceId) {
        return documents.findByDeviceId(deviceId)
                .map(InstallationEntity::getProcess);
    }

    @Override
    public void save(InstallationProcess process) {
        List<DomainEvent> emitted = eventsEmittedFrom(process);
        InstallationProcessState state = process.asState();
        documents.save(documents.findById(process.orderId)
                .orElseGet(() -> new InstallationEntity(process.orderId))
                .setProcess(process)
                .setState(state)
        );
        emitted.forEach(event -> {
            events.save(new InstallationEventEntity(
                    process.orderId,
                    process.deviceId,
                    EventTypes.of(event),
                    event
            ));
            publisher.publishEvent(event);
        });
        if (!emitted.isEmpty()) {
            publisher.publishEvent(state);
        }
    }

    @Override
    public Page<InstallationProcessState> findAllMatching(boolean anyStatus, List<InstallationProcessState.State> states, Pageable pageable) {
        return documents.findAllMatching(anyStatus, states, pageable)
                .map(InstallationEntity::getState);
    }

    private static List<DomainEvent> eventsEmittedFrom(InstallationProcess installation) {
        List<DomainEvent> emitted = List.copyOf(installation.events);
        installation.events.clear();
        return emitted;
    }

    @Repository
    interface DocumentRepository extends CrudRepository<InstallationEntity, String> {
        Optional<InstallationEntity> findByDeviceId(String deviceId);

        Optional<InstallationEntity> findByOrderId(String orderId);

        @Query(
                value = """
                        select * from installation_document
                        where (:anyStatus or (state ->> 'state') in (:states))
                        """,
                countQuery = """
                        select count(*) from installation_document
                        where (:anyStatus or (state ->> 'state') in (:states))
                        """,
                nativeQuery = true)
        Page<InstallationEntity> findAllMatching(
                @Param("anyStatus") boolean anyStatus, @Param("states") List<InstallationProcessState.State> states,
                Pageable pageable
        );
    }

    @Repository
    interface EventRepository extends CrudRepository<InstallationEventEntity, UUID> {
    }

    @Entity
    @Table(name = "installation_document")
    @NoArgsConstructor
    static class InstallationEntity {
        @Id
        private String orderId;
        private String deviceId;
        @Version
        private long version;

        @Getter
        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        private InstallationProcess process;
        @Type(type = "jsonb")
        @Getter
        @Column(columnDefinition = "jsonb")
        private InstallationProcessState state;

        public InstallationEntity(String orderId) {
            this.orderId = orderId;
        }

        public InstallationEntity setProcess(InstallationProcess process) {
            this.process = process;
            this.deviceId = process.deviceId;
            return this;
        }

        public InstallationEntity setState(InstallationProcessState state) {
            this.state = state;
            return this;
        }
    }

    @Data
    @Entity
    @Table(name = "installation_events")
    @NoArgsConstructor
    static class InstallationEventEntity {
        @Id
        private UUID id;
        private String orderId;
        private String deviceId;
        private String type;
        private Instant time;
        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        private DomainEvent event;

        InstallationEventEntity(String orderId, String deviceId, EventTypes.Type type, DomainEvent event) {
            this.id = UUID.randomUUID();
            this.orderId = orderId;
            this.deviceId = deviceId;
            this.type = type.type();
            this.time = Instant.now();
            this.event = event;
        }
    }
}
