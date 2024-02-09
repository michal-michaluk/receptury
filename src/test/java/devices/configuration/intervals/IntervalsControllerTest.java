package devices.configuration.intervals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static devices.configuration.tools.JsonConfiguration.json;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IntervalRulesController.class)
class IntervalRulesControllerTest {

    @Autowired
    private MockMvc rest;
    @MockBean
    private IntervalRulesDocumentRepository repository;

    @Test
    void getEmptyIntervalRules() throws Exception {
        Mockito.when(repository.get()).thenReturn(IntervalRules.defaultRules());

        rest.perform(get("/configs/IntervalRules")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "byIds": [],
                          "byModel": [],
                          "defSeconds": 1800
                        }
                        """, true));
    }

    @Test
    void getCurrentIntervalRules() throws Exception {
        Mockito.when(repository.get()).thenReturn(IntervalRulesFixture.currentRules());

        rest.perform(get("/configs/IntervalRules")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "byIds": [
                            {
                              "seconds": 600,
                              "devices": [
                                "ALF-2844179",
                                "ALF-9571445",
                                "CS_7155_CGC100",
                                "EVB-P4562137",
                                "EVB-P9287312"
                              ]
                            },
                            {
                              "seconds": 2700,
                              "devices": [
                                "EVB-P0984003",
                                "EVB-P15079256",
                                "EVB-P1515526",
                                "EVB-P1515640",
                                "t53_8264_019"
                              ]
                            }
                          ],
                          "byModel": [
                            {
                              "seconds": 60,
                              "vendor": "Alfen BV",
                              "model": "NG920-5250[6-9]",
                              "firmware": null
                            },
                            {
                              "seconds": 10,
                              "vendor": "ChargeStorm AB",
                              "model": "Chargestorm Connected",
                              "firmware": "1[.]2[.].*"
                            },
                            {
                              "seconds": 60,
                              "vendor": "ChargeStorm AB",
                              "model": "Chargestorm Connected",
                              "firmware": null
                            },
                            {
                              "seconds": 120,
                              "vendor": "EV-BOX",
                              "model": "G3-M5320E-F2.*",
                              "firmware": null
                            }
                          ],
                          "defSeconds": 1800
                        }
                        """, true));
    }

    @Test
    void putIntervalRules() throws Exception {
        rest.perform(put("/configs/IntervalRules")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(IntervalRules.defaultRules())))
                .andExpect(status().isOk());

        Mockito.verify(repository).save(IntervalRules.defaultRules());
    }
}
