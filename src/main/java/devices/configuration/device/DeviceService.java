package devices.configuration.device;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository repository;

    @WithSpan
    @Transactional(readOnly = true)
    public Optional<DeviceConfiguration> getDevice(@SpanAttribute String deviceId) {
        return repository.get(deviceId)
                .map(Device::toDeviceConfiguration);
    }

    @WithSpan
    public DeviceConfiguration createNewDevice(@SpanAttribute String deviceId, @SpanAttribute UpdateDevice update) {
        Device device = Device.newDevice(deviceId);
        update.apply(device);
        repository.save(device);
        return device.toDeviceConfiguration();
    }

    @WithSpan
    public Optional<DeviceConfiguration> updateDevice(@SpanAttribute String deviceId, UpdateDevice update) {
        return repository.get(deviceId)
                .map(device -> {
                    update.apply(device);
                    repository.save(device);
                    return device.toDeviceConfiguration();
                });
    }
}
