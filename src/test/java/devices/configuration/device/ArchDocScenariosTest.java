package devices.configuration.device;

import io.opentelemetry.javaagent.testing.common.AgentTestingExporterAccess;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static devices.configuration.device.DeviceConfigurationAssert.assertThat;
import static devices.configuration.device.DeviceFixture.location;
import static devices.configuration.device.DeviceFixture.ownership;

public class ArchDocScenariosTest {

    final DeviceRepositoryFake devices = new DeviceRepositoryFake();
    final DeviceService service = new DeviceService(devices);

//    OpenTelemetryFacade ot = OpenTelemetryFacade.create();

    @Test
    void createNewDevice() {
        AgentTestingExporterAccess.reset();

        DeviceConfiguration configuration = service.createNewDevice(
                "new-device-id",
                UpdateDevice.use(ownership(), location())
        );

        assertThat(configuration)
                .hasOwnership(ownership())
                .hasLocation(location())
                .hasOpeningHours(OpeningHours.alwaysOpened())
                .hasSettings(Settings.defaultSettings())
                .hasNoViolations()
                .isNotVisible();
        Assertions.assertThat(AgentTestingExporterAccess.getExportedSpans())
                .isNotEmpty();
    }
}
