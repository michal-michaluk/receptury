package devices.configuration.communication.protocols.iot16;

import devices.configuration.communication.CommunicationFixture;
import devices.configuration.communication.CommunicationService;
import devices.configuration.communication.KnownDevices;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IoT16Controller.class)
class IoT16ControllerTest {

    @Autowired
    private MockMvc rest;
    @MockBean
    private CommunicationService service;


    @Test
    void updateAll() throws Exception {
        Mockito.when(service.handleBoot(any()))
                .thenReturn(new CommunicationService.BootResponse(
                        Instant.parse("2023-06-28T06:15:30.00Z"),
                        Duration.ofSeconds(69),
                        KnownDevices.State.EXISTING
                ));

        rest.perform(post("/protocols/iot16/bootnotification/{deviceId}", "device-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "chargePointVendor": "Garo",
                                  "chargePointModel": "CPF25 Family",
                                  "chargePointSerialNumber": "891234A56711",
                                  "chargeBoxSerialNumber": "820394A93203",
                                  "firmwareVersion": "1.1",
                                  "iccid": "112233445566778899C1",
                                  "imsi": "082931213347973812",
                                  "meterType": "5051",
                                  "meterSerialNumber": "937462A48276"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                                {
                                  "currentTime": "2023-06-28T06:15:30Z",
                                  "interval": 69,
                                  "status": "Accepted"
                                }
                        """, true));

        Mockito.verify(service).handleBoot(CommunicationFixture.boot("device-id"));
    }
}
