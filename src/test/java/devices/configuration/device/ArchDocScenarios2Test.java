package devices.configuration.device;

import org.junit.jupiter.api.Test;

import static devices.configuration.device.DeviceConfigurationAssert.assertThat;
import static devices.configuration.device.DeviceFixture.location;
import static devices.configuration.device.DeviceFixture.ownership;

public class ArchDocScenarios2Test {

    final DeviceRepositoryFake devices = new DeviceRepositoryFake();
    final DeviceService service = new DeviceService(devices);
//
//    @RegisterExtension
//    static final AgentInstrumentationExtension instrumentation = AgentInstrumentationExtension
//            .create();

    @Test
    void createNewDevice() {
//        OpenTelemetryFacade telemetry = OpenTelemetryFacade.create();
//        AgentTestingExporterAccess.reset();

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
//        Assertions.assertThat(AgentTestingExporterAccess.getExportedSpans())
//                .isNotEmpty();
//        telemetry.printTraces(System.out::println);
    }
}
