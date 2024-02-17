package devices.configuration.installations;

import devices.configuration.IntegrationTest;
import devices.configuration.RequestsFixture;
import devices.configuration.auth.AuthFixture;
import devices.configuration.communication.CommunicationFixture;
import devices.configuration.device.DeviceFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static devices.configuration.TestTransaction.transactional;
import static devices.configuration.installations.InstallationFixture.givenWorkOrderFor;

@IntegrationTest(profiles = {"auth-test", "integration-test"})
class InstallationE2ETest {

    @Autowired
    AuthFixture auth;
    @Autowired
    RequestsFixture requests;
    @Autowired
    InstallationService service;
    @Autowired
    InstallationReadModelFixture installationReadModelFixture;

    final String deviceId = DeviceFixture.randomId();

    @BeforeEach
    void setUp() {
        requests.clearJwt();
        requests.withJwt(auth.tokenFor("john", "john"));
        installationReadModelFixture.truncate();
    }

    @Test
    void fullInstallation() {
        WorkOrder order = givenWorkOrderFor(DeviceFixture.ownership());

        // when
        transactional(() -> service.handleWorkOrder(order));
        // given
        requests.installations.get(0, 10000).isExactlyLike("""
                {"content":[{"orderId":"%s","deviceId":null,"state":"PENDING"}],"totalPages":1,"totalElements":1,"page":0,"size":1}""", order.orderId());
        requests.installations.get(order.orderId()).isExactlyLike("""
                {"orderId":"%s","deviceId":null,"state":"PENDING"} """, order.orderId());

        // when
        requests.installations.patch(order.orderId(), """
                        { "assignDevice": "%s" } """, deviceId)
                .isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"DEVICE_ASSIGNED"}""", order.orderId(), deviceId);

        // when
        requests.installations.patch(order.orderId(), """
                        {
                          "assignLocation": {
                            "street": "Rakietowa",
                            "houseNumber": "1A",
                            "city": "Wrocław",
                            "postalCode": "54-621",
                            "state": null,
                            "country": "POL",
                            "coordinates": {
                              "longitude": 51.09836221719513,
                              "latitude": 16.931752852309156
                            }
                          }
                        }""")
                .isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"DEVICE_ASSIGNED"}
                        """, order.orderId(), deviceId);

        transactional(() -> service.handleBootNotification(CommunicationFixture.boot(deviceId)));
        requests.installations.get(order.orderId())
                .isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"BOOTED"}
                        """, order.orderId(), deviceId);

        requests.installations.patch(order.orderId(), """
                        { "confirmBoot": true }""")
                .isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"BOOTED"}""", order.orderId(), deviceId);

        requests.installations.patch(order.orderId(), """
                        { "complete": true }""")
                .isExactlyLike("""
                        {"orderId":"%s","deviceId":"%s","state":"COMPLETED"} """, order.orderId(), deviceId);
    }
}
