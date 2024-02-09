package devices.configuration.installations;

import devices.configuration.device.DeviceFixture;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static devices.configuration.installations.InstallationProcessState.State.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InstallationController.class)
class InstallationControllerTest {

    @Autowired
    private MockMvc rest;
    @MockBean
    private InstallationService service;

    @Test
    void noBody() throws Exception {
        rest.perform(patch("/installations/{orderId}", "order-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void noCommand() throws Exception {
        rest.perform(patch("/installations/{orderId}", "order-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void assignDevice() throws Exception {
        expect(state("order-id", "device-id", DEVICE_ASSIGNED));

        when(
                """
                        {
                         "assignDevice": "device-id"
                        }
                        """)
                .andExpect(status().isOk())
                .andExpect(content().json(
                        """
                                {
                                  "orderId": "order-id",
                                  "deviceId": "device-id",
                                  "state": "DEVICE_ASSIGNED"
                                }
                                """));

        verify(service).assignDevice("order-id", "device-id");
    }

    @Test
    void assignLocation() throws Exception {
        expect(state("order-id", "device-id", DEVICE_ASSIGNED));

        when(
                """
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
                        }
                        """)
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "orderId": "order-id",
                          "deviceId": "device-id",
                          "state": "DEVICE_ASSIGNED"
                        }
                        """));

        verify(service).assignLocation("order-id", DeviceFixture.location());
    }

    @Test
    void confirmBootData() throws Exception {
        expect(state("order-id", "device-id", BOOTED));

        when(
                """
                        {
                          "confirmBoot": true
                        }
                        """)
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "orderId": "order-id",
                          "deviceId": "device-id",
                          "state": "BOOTED"
                        }
                        """));

        verify(service).confirmBootData("order-id");
    }

    @Test
    void wrongConfirmBootData() throws Exception {
        rest.perform(patch("/installations/{orderId}", "order-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "confirmBoot": false
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void complete() throws Exception {
        expect(state("order-id", "device-id", COMPLETED));

        when(
                """
                        {
                          "complete": true
                        }
                        """)
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "orderId": "order-id",
                          "deviceId": "device-id",
                          "state": "COMPLETED"
                        }
                        """
                ));

        verify(service).complete("order-id");
    }

    @Test
    void wrongComplete() throws Exception {
        rest.perform(patch("/installations/{orderId}", "order-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "complete": false
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    private void when(@Language("JSON") String request, @Language("JSON") String response) throws Exception {
        rest.perform(patch("/installations/{orderId}", "order-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().json(response));
    }

    private ResultActions when(@Language("JSON") String request) throws Exception {
        return rest.perform(patch("/installations/{orderId}", "order-id")
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request));
    }

    private OngoingStubbing<InstallationProcessState> expect(InstallationProcessState state) {
        return Mockito.when(service.getByOrderId(state.orderId())).thenReturn(state);
    }

    @NotNull
    private static InstallationProcessState state(String orderId, String deviceId, InstallationProcessState.State state) {
        return new InstallationProcessState(orderId, deviceId, state);
    }
}
