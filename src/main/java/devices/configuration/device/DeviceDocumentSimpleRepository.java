package devices.configuration.device;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.Optional;

@Repository
@AllArgsConstructor
class DeviceDocumentSimpleRepository implements DeviceRepository {

    private final DocumentRepository documents;

    @Override
    public Optional<Device> get(String deviceId) {
        return documents.findById(deviceId)
                .map(DeviceDocumentEntity::getDevice);
    }

    @Override
    public void save(Device device) {
        documents.save(documents.findById(device.deviceId)
                .orElseGet(() -> new DeviceDocumentEntity(device.deviceId))
                .setDevice(device)
        );
    }

    @Repository
    interface DocumentRepository extends PagingAndSortingRepository<DeviceDocumentEntity, String> {
    }

    @Entity
    @Table(name = "device_document")
    @NoArgsConstructor
    static class DeviceDocumentEntity {
        @Id
        private String deviceId;
        @Version
        private long version;

        @Getter
        @Type(type = "jsonb")
        private Device device;

        public DeviceDocumentEntity setDevice(Device device) {
            this.device = device;
            return this;
        }

        DeviceDocumentEntity(String deviceId) {
            this.deviceId = deviceId;
        }
    }

}
