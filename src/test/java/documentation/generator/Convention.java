package documentation.generator;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class Convention {
    public static final String DOCUMENTING_SCENARIO = "documenting.scenario";
    public static final String DOCUMENTING_SCENARIO_STEP = "documenting.scenario.step";
    public static final String DOCUMENTING_SCENARIO_HIGHLIGHT = "documenting.scenario.highlight.";
    public static final String DOCUMENTING_SCENARIO_HIGHLIGHT_INFORMATION = "information";
    public static final String DOCUMENTING_SCENARIO_HIGHLIGHT_DATA = "data";

    @NotNull
    public static Scenarios.StepDescription createStepDescription(Span span) {
        Step step = span.attribute(DOCUMENTING_SCENARIO_STEP)
                .map(value -> Serialization.fromString(value, Step.class))
                .orElseThrow(() -> new IllegalStateException("given span " + span + " has not attribute " + DOCUMENTING_SCENARIO_STEP));

        return new Scenarios.StepDescription(span, step,
                List.copyOf(span.attributesWithPrefix(DOCUMENTING_SCENARIO_HIGHLIGHT)
                        .collect(Collectors.groupingBy(
                                entry -> entry.getKey().substring(DOCUMENTING_SCENARIO_HIGHLIGHT.length(), entry.getKey().lastIndexOf(".")),
                                Collectors.collectingAndThen(Collectors.toMap(
                                                entry -> entry.getKey().substring(entry.getKey().lastIndexOf(".") + 1),
                                                Map.Entry::getValue
                                        ), map -> new Scenarios.Highlighted(
                                                Objects.toString(map.get(Convention.DOCUMENTING_SCENARIO_HIGHLIGHT_INFORMATION)),
                                                Objects.toString(map.get(Convention.DOCUMENTING_SCENARIO_HIGHLIGHT_DATA)))
                                )
                        )).values())
        );
    }

    record HighlightAttributeKeys(String information, String data) {}

    static HighlightAttributeKeys highlight(long number) {
        return new HighlightAttributeKeys(
                Convention.DOCUMENTING_SCENARIO_HIGHLIGHT + number + "." + Convention.DOCUMENTING_SCENARIO_HIGHLIGHT_INFORMATION,
                Convention.DOCUMENTING_SCENARIO_HIGHLIGHT + number + "." + Convention.DOCUMENTING_SCENARIO_HIGHLIGHT_DATA
        );
    }
}
