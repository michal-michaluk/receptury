package devices.configuration.communication;

import devices.configuration.device.DeviceConfiguration;
import devices.configuration.installations.DomainEvent.DeviceAssigned;
import devices.configuration.installations.DomainEvent.InstallationCompleted;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.EnumType.STRING;

@Primary
@Component
@Transactional
@AllArgsConstructor
class KnownDevicesProjection implements KnownDevices {
    private final JpaRepository repository;

    @Override
    @WithSpan
    public State get(String deviceId) {
        return repository.findById(deviceId)
                .map(KnownDeviceEntity::state)
                .orElse(State.UNKNOWN);
    }

    @EventListener
    @WithSpan
    public void handleInstallationStart(DeviceAssigned event) {
        put(event.deviceId(), State.IN_INSTALLATION);
    }

    @EventListener
    @WithSpan
    public void handleInstallationFinish(InstallationCompleted event) {
        put(event.deviceId(), State.EXISTING);
    }

    @EventListener
    @WithSpan
    public void handleDeInstallation(DeviceConfiguration event) {
        if (event.ownership().isUnowned()) {
            put(event.deviceId(), State.UNKNOWN);
        }
    }

    private void put(String deviceId, State state) {
        repository.save(repository.findById(deviceId)
                .orElseGet(() -> new KnownDeviceEntity(deviceId))
                .state(state)
        );
    }

    interface JpaRepository extends CrudRepository<KnownDeviceEntity, String> {
    }

    @Entity
    @Table(name = "known_device")
    @NoArgsConstructor
    static class KnownDeviceEntity {
        @Id
        private String deviceId;
        @Enumerated(STRING)
        private State state;

        public KnownDeviceEntity(String deviceId) {
            this.deviceId = deviceId;
        }

        public State state() {
            return this.state;
        }

        public KnownDeviceEntity state(State state) {
            this.state = state;
            return this;
        }
    }
}
