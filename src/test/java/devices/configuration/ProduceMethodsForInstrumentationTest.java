package devices.configuration;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import documentation.generator.OtelInstrumentationMethodsInclude;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.tngtech.archunit.base.DescribedPredicate.or;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.core.domain.JavaCodeUnit.Predicates.method;
import static com.tngtech.archunit.core.domain.JavaMember.Predicates.declaredIn;

public class ProduceMethodsForInstrumentationTest {
    JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("devices.configuration");

    @Test
    public void architecture() throws IOException {
        DescribedPredicate<JavaCodeUnit> methods = method().and(declaredIn(
                resideInAPackage("devices.configuration..").and(or(
                        simpleNameEndingWith("Service"),
                        simpleNameEndingWith("ReadModel")))
        ));
        var printer = new OtelInstrumentationMethodsInclude(importedClasses, methods);
        String include = printer.includeString();

        System.setProperty("otel.instrumentation.methods.include", include);
        var expected = "devices.configuration.communication.KnownDevicesReadModel[projectionOfDeviceInstallation,projectionOfInstallationCompleted,put,projectionOfDeInstallation,queryDevice];" +
                       "devices.configuration.installations.InstallationService[handleWorkOrder,getByOrderId,complete,confirmBootData,assignLocation,handleBootNotification,getByDeviceId,assignDevice];" +
                       "devices.configuration.search.DevicesReadModel[projectionOf,queryPins,querySummary,queryDetails,projectionOf,projectionOf];" +
                       "devices.configuration.installations.InstallationReadModel[query,queryByOrderId,projectionOf,put];" +
                       "devices.configuration.intervals.IntervalsService[calculateInterval];" +
                       "devices.configuration.communication.CommunicationService[handleBoot];" +
                       "devices.configuration.device.DeviceService[getDevice,createNewDevice,updateDevice];";
        Assertions.assertThat(include).isEqualTo(expected);
    }

}
