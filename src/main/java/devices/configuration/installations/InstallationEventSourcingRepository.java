package devices.configuration.installations;

import devices.configuration.tools.EventTypes;
import devices.configuration.tools.LegacyDomainEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@AllArgsConstructor
class InstallationEventSourcingRepository implements InstallationRepository {

    private final EventRepository repository;
    private final ApplicationEventPublisher publisher;


    @Override
    public InstallationProcess getByOrderId(String orderId) {
        return recreateObject(repository.findByOrderId(orderId));
    }

    @Override
    public Optional<InstallationProcess> getByDeviceId(String deviceId) {
        List<InstallationEventEntity> history = repository.findByDeviceId(deviceId);
        if (history.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(recreateObject(history));
    }

    private InstallationProcess recreateObject(List<InstallationEventEntity> data) {
        Collections.reverse(data);
        List<DomainEvent> history = data.stream()
                .map(InstallationEventEntity::getEvent)
                .map(LegacyDomainEvent::normalise)
                .toList();
        if (history.isEmpty()) {
            throw new IllegalStateException("process never started");
        }
        return InstallationProcess.fromHistory(history);
    }

    @Override
    public void save(InstallationProcess process) {
        process.events.forEach(event -> {
            repository.save(new InstallationEventEntity(
                    process.orderId,
                    process.deviceId,
                    EventTypes.of(event),
                    event));
            publisher.publishEvent(event);
        });
        if (!process.events.isEmpty()) {
            publisher.publishEvent(process.asState());
        }
        process.events.clear();
    }

    @Repository
    interface EventRepository extends CrudRepository<InstallationEventEntity, UUID> {
        @Query(value = "select distinct on (type) *" +
                " from installation_events" +
                " where device_id = :deviceId" +
                " order by type, time desc", nativeQuery = true)
        List<InstallationEventEntity> findByDeviceId(String deviceId);

        @Query(value = "select distinct on (type) *" +
                " from installation_events" +
                " where order_id = :orderId" +
                " order by type, time desc", nativeQuery = true)
        List<InstallationEventEntity> findByOrderId(String orderId);
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
