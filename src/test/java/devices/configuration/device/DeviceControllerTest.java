package devices.configuration.device;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static devices.configuration.device.DeviceFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc rest;
    @MockBean
    private DeviceService service;

    @Test
    void updateAll() throws Exception {
        Mockito.when(service.updateDevice(eq("device-id"), any()))
                .thenReturn(Optional.of(new DeviceConfiguration(
                        "device-id",
                        ownership(),
                        location(),
                        closedAtWeekend(),
                        settingsForPublicDevice(),
                        Violations.builder().build(),
                        Visibility.basedOn(true, true)
                )));

        rest.perform(patch("/devices/{deviceId}", "device-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ownership": {
                                    "operator": "Devicex.nl",
                                    "provider":"public-devices"
                                  },
                                  "location": {
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
                                  },
                                  "openingHours": {
                                    "alwaysOpen": false,
                                    "opened": {
                                      "monday": {
                                        "option": "Opened24h"
                                      },
                                      "tuesday": {
                                        "option": "Opened24h"
                                      },
                                      "wednesday": {
                                        "option": "Opened24h"
                                      },
                                      "thursday": {
                                        "option": "Opened24h"
                                      },
                                      "friday": {
                                        "option": "OpenTime",
                                        "time": [
                                          {
                                            "open": "00:00:00",
                                            "close": "15:00:00"
                                          }
                                        ]
                                      },
                                      "saturday": {
                                        "option": "Closed24h"
                                      },
                                      "sunday": {
                                        "option": "Closed24h"
                                      }
                                    }
                                  },
                                  "settings": {
                                    "autoStart": false,
                                    "remoteControl": false,
                                    "billing": false,
                                    "reimbursement": false,
                                    "showOnMap": true,
                                    "publicAccess": true
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                                {
                                  "deviceId": "device-id",
                                  "location": {
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
                                  },
                                  "openingHours": {
                                    "alwaysOpen": false,
                                    "opened": {
                                      "monday": {
                                        "option": "Opened24h"
                                      },
                                      "tuesday": {
                                        "option": "Opened24h"
                                      },
                                      "wednesday": {
                                        "option": "Opened24h"
                                      },
                                      "thursday": {
                                        "option": "Opened24h"
                                      },
                                      "friday": {
                                        "option": "OpenTime",
                                        "time": [
                                          {
                                            "open": "00:00:00",
                                            "close": "15:00:00"
                                          }
                                        ]
                                      },
                                      "saturday": {
                                        "option": "Closed24h"
                                      },
                                      "sunday": {
                                        "option": "Closed24h"
                                      }
                                    }
                                  },
                                  "settings": {
                                    "autoStart": false,
                                    "remoteControl": false,
                                    "billing": false,
                                    "reimbursement": false,
                                    "showOnMap": true,
                                    "publicAccess": true
                                  },
                                  "ownership": {
                                    "operator": "Devicex.nl",
                                    "provider":"public-devices"
                                  },
                                  "violations": {
                                    "operatorNotAssigned":false,
                                    "providerNotAssigned":false,
                                    "locationMissing":false,
                                    "showOnMapButMissingLocation":false,
                                    "showOnMapButNoPublicAccess":false
                                  },
                                  "visibility": {
                                    "roamingEnabled":true,
                                    "forCustomer":"USABLE_AND_VISIBLE_ON_MAP"
                                  }
                                }
                        """, true));

        Mockito.verify(service).updateDevice("device-id", UpdateDevice.builder()
                .ownership(ownership())
                .location(location())
                .openingHours(closedAtWeekend())
                .settings(settingsForPublicDevice())
                .build()
        );
    }

    @Test
    void wrongDeviceId() throws Exception {
        Mockito.when(service.updateDevice(any(), any()))
                .thenReturn(Optional.empty());

        rest.perform(patch("/devices/{deviceId}", "not-existing-device-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void emptyBody() throws Exception {
        Mockito.when(service.updateDevice(any(), any()))
                .thenReturn(Optional.of(DeviceFixture.givenDeviceConfiguration("device-id")));

        rest.perform(patch("/devices/{deviceId}", "device-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        Mockito.verify(service).updateDevice("device-id", UpdateDevice.builder().build());
    }
}
