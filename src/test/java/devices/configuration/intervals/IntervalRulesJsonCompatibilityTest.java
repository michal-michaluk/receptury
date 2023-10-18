package devices.configuration.intervals;

import com.fasterxml.jackson.core.JsonProcessingException;
import devices.configuration.JsonConfiguration;
import org.intellij.lang.annotations.Language;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class IntervalRulesJsonCompatibilityTest {

    @Test
    void intervalRulesIsCompatibleWithGivenJson() throws JsonProcessingException, JSONException {
        @Language("JSON") String json = """
                {
                  "deviceIdRules": [
                    { "interval": 600, "deviceIds": [ "EVB-P4562137", "ALF-9571445" ] },
                    { "interval": 2700, "deviceIds": [ "t53_8264_019", "EVB-P15079256" ] }
                  ],
                  "modelRules": [
                    { "interval": 120, "vendor": "ChargeStorm AB", "model": "Chargestorm Connected" },
                    { "interval": 60, "vendor": "Alfen BV", "model": "NG920-5250[6-9]" }
                  ],
                  "defaultInterval": 1800
                }
                """;
        IntervalRules object = JsonConfiguration.OBJECT_MAPPER.readValue(json, IntervalRules.class);

        String actual = JsonConfiguration.OBJECT_MAPPER.writeValueAsString(object);

        JSONAssert.assertEquals(json, actual, true);
    }
}
