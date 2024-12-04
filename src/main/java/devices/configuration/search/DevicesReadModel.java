package devices.configuration.search;

import devices.configuration.communication.BootNotification;
import devices.configuration.communication.DeviceStatuses;
import devices.configuration.device.DeviceConfiguration;
import devices.configuration.device.Ownership;
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
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Transactional
@AllArgsConstructor
class DevicesReadModel {

    private final DeviceReadsRepository repository;

    @EventListener
    public void projectionOf(DeviceConfiguration details) {
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
    public void projectionOf(BootNotification boot) {
        DeviceReadsEntity entity = repository.findById(boot.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(boot.deviceId()));

        entity.setBoot(boot);

        repository.save(entity);
    }

    @EventListener
    public void projectionOf(DeviceStatuses statuses) {
        DeviceReadsEntity entity = repository.findById(statuses.deviceId())
                .orElseGet(() -> new DeviceReadsEntity(statuses.deviceId()));

        entity
                .setStatuses(statuses)
                .setPin(DevicePin.ofNullable(entity.getDetails(), statuses))
                .setSummary(DeviceSummary.ofNullable(entity.getDetails(), statuses));

        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<DeviceDetails> queryDetails(String deviceId) {
        return repository.findById(deviceId)
                .map(entity -> new DeviceDetails(entity.details, entity.boot));
    }

    @Transactional(readOnly = true)
    public List<DevicePin> queryPins(String operator) {
        return repository.findAllByOperator(operator)
                .map(DeviceReadsEntity::getPin)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DeviceSummary> querySummary(String operator, Pageable pageable) {
        return repository.findAllByOperator(operator, pageable)
                .map(DeviceReadsEntity::getSummary);
    }

    @Repository
    interface DeviceReadsRepository extends PagingAndSortingRepository<DeviceReadsEntity, String> {
        Stream<DeviceReadsEntity> findAllByOperator(String operator);

        Page<DeviceReadsEntity> findAllByOperator(String operator, Pageable pageable);
    }

    @Data
    @Accessors(chain = true)
    @Entity
    @DynamicUpdate
    @Table(name = "search")
    @NoArgsConstructor
    static class DeviceReadsEntity {

        @Id
        private String deviceId;
        @Version
        private Long version;
        private String operator;
        private String provider;

        @Type(type = "jsonb")
        private DevicePin pin;

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        private DeviceSummary summary;

        @Type(type = "jsonb")
        private DeviceConfiguration details;

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        private DeviceStatuses statuses;
        @Type(type = "jsonb")
        private BootNotification boot;

        DeviceReadsEntity(String deviceId) {
            this.deviceId = deviceId;
        }

        DeviceReadsEntity setOwnership(Ownership ownership) {
            operator = ownership.operator();
            provider = ownership.provider();
            return this;
        }
    }
}
