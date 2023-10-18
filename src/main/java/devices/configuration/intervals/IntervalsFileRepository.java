package devices.configuration.intervals;

public class IntervalsFileRepository implements IntervalsRepository {
    @Override
    public IntervalRules get() {
//        IntervalsFileRepository.class.getResourceAsStream("/interval-rules.json");
//        return JsonConfiguration.OBJECT_MAPPER.readValue(json, IntervalRules.class);
        return null;
    }
}
