package devices.configuration.device;

import devices.configuration.device.DomainEvent.DeviceStatuses;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
class ReadModelsProjection implements DeviceReads {

    private final JpaRepository repository;

    @EventListener
    public void handle(DeviceSnapshot details) {
        DeviceReadsEntity entity = repository.findById(details.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(details.deviceId()));

        entity
                .setOwnership(details.ownership())
                .setDetails(details)
                .setPin(DevicePin.ofNullable(details, entity.getStatuses()))
                .setSummary(DeviceSummary.ofNullable(details, entity.getStatuses()));

        repository.save(entity);
    }

    @EventListener
    public void handle(DeviceStatuses statuses) {
        DeviceReadsEntity entity = repository.findById(statuses.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(statuses.deviceId()));

        entity
                .setStatuses(statuses)
                .setPin(DevicePin.ofNullable(entity.getDetails(), statuses))
                .setSummary(DeviceSummary.ofNullable(entity.getDetails(), statuses));

        repository.save(entity);
    }

    @Override
    public Optional<DeviceSnapshot> findById(String deviceId) {
        return repository.findById(deviceId)
                .map(DeviceReadsEntity::getDetails);
    }

    @Override
    public List<DevicePin> findAllPins(String provider) {
        return repository.findAllByProvider(provider)
                .map(DeviceReadsEntity::getPin)
                .toList();
    }

    @Override
    public Page<DeviceSummary> findAllSummary(String provider, Pageable pageable) {
        return repository.findAllByProvider(provider, pageable)
                .map(DeviceReadsEntity::getSummary);
    }

    @Repository
    interface JpaRepository extends PagingAndSortingRepository<DeviceReadsEntity, String> {
        Stream<DeviceReadsEntity> findAllByProvider(String provider);

        Page<DeviceReadsEntity> findAllByProvider(String provider, Pageable pageable);
    }

    @Data
    @Accessors(chain = true)
    @Entity
    @DynamicUpdate
    @Table(name = "device_reads")
    @NoArgsConstructor
    static class DeviceReadsEntity {

        @Id
        private String deviceId;
        @Version
        private Long version;
        private String operator;
        private String provider;

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        private DevicePin pin;

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        private DeviceSummary summary;

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        private DeviceSnapshot details;

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        private DeviceStatuses statuses;

        public DeviceReadsEntity(String deviceId) {
            this.deviceId = deviceId;
        }

        public DeviceReadsEntity setOwnership(Ownership ownership) {
            operator = Optional.ofNullable(ownership).map(Ownership::operator).orElse(null);
            provider = Optional.ofNullable(ownership).map(Ownership::provider).orElse(null);
            return this;
        }
    }

}
