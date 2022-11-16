package devices.configuration.device;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

interface DeviceReads {
    Optional<DeviceSnapshot> findById(String deviceId);

    List<DevicePin> findAllPins(String provider);

    Page<DeviceSummary> findAllSummary(String provider, Pageable pageable);
}
