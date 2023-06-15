package devices.configuration.installations;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.OptionalAssert;
import org.springframework.test.util.ReflectionTestUtils;

public class InstallationProcessAssert {
    private final InstallationProcess actual;

    public static InstallationProcessAssert assertThat(InstallationProcess actual) {
        return new InstallationProcessAssert(actual);
    }

    private InstallationProcessAssert(InstallationProcess actual) {
        this.actual = actual;
    }

    @SafeVarargs
    public final InstallationProcessAssert emittedEvents(Class<? extends DomainEvent>... expected) {
        Assertions.assertThat(actual.events)
                .hasExactlyElementsOfTypes(expected);
        return this;
    }

    public InstallationProcessAssert lastEventIsEqualTo(DomainEvent expected) {
        Assertions.assertThat(actual.events)
                .last().isEqualTo(expected);
        return this;
    }

    public InstallationProcessAssert isInState(InstallationProcessState.State expected) {
        Assertions.assertThat(actual.asState().state())
                .isEqualTo(expected);
        return this;
    }

    public InstallationProcessAssert hasDeviceAssigned(String deviceId) {
        Assertions.assertThat(actual.asState().deviceId())
                .isEqualTo(deviceId);
        return this;
    }

    public InstallationProcessAssert bootIsNotConfirmed() {
        Assertions.assertThat(ReflectionTestUtils.getField(actual, "bootConfirmation")).isEqualTo(false);
        return this;
    }

    public InstallationProcessAssert bootIsConfirmed() {
        Assertions.assertThat(ReflectionTestUtils.getField(actual, "bootConfirmation")).isEqualTo(true);
        return this;
    }

    private <T extends DomainEvent> OptionalAssert<T> lastEmitted(Class<T> type) {
        return Assertions.assertThat(actual.events.stream()
                .filter(type::isInstance)
                .reduce((previous, next) -> next)
                .map(type::cast));
    }
}
