package documentation.generator;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

record Scenarios(TelemetrySpans telemetry,
                 List<Scenario> scenarios) {


    record Description() {}

    record Scenario(Span root, Description description, List<Call> subGraph) {
        String name() {
            return root.name();
        }
    }

    @NotNull
    static Scenarios selectScenarios(TelemetrySpans telemetry, PerspectiveParameters parameters) {
        List<Scenario> list = telemetry
                .select(parameters.scenarioPredicate())
                .map(scenario -> {
                    var subGraph = scenarioSubGraph(telemetry, scenario, parameters);
                    return new Scenario(scenario, new Description(), subGraph);
                }).toList();

        return new Scenarios(telemetry, list);
    }

    @NotNull
    private static List<Call> scenarioSubGraph(TelemetrySpans telemetry, Span scenarioRoot, PerspectiveParameters parameters) {
        var scenarioSubGraph = new ArrayList<Call>();
        new DepthFirstIterator<>(telemetry.graph(), scenarioRoot.spanId()).forEachRemaining(spanId -> {
            Span span = telemetry.span(spanId);
            if (span != null) {
                Span parent = telemetry.span(span.parentSpanId());
                scenarioSubGraph.add(Call.of(parent, span, parameters.participantName(), parameters.callName()));
            }
        });
        return scenarioSubGraph;
    }

    List<Span> roots() {
        return scenarios.stream().map(Scenario::root).toList();
    }

    Scenarios subsetOfScenarios(Predicate<Span> scenarioPredicate) {
        return new Scenarios(telemetry, scenarios.stream()
                .filter(scenario -> scenarioPredicate.test(scenario.root()))
                .toList());
    }

    Stream<Call> subGraph() {
        return scenarios.stream()
                .flatMap(scenario -> scenario.subGraph().stream());
    }
}
