package documentation.generator;

import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

record TelemetrySpans(Map<ByteString, Span> spans,
                      Graph<Object, DefaultEdge> graph) {

    Span span(Object spanId) {
        return spans.get(spanId);
    }

    Stream<Span> select(Predicate<Span> predicate) {
        return spans.values().stream().filter(predicate);
    }

    public Scenarios selectScenarios(PerspectiveParameters parameters) {
        List<Scenarios.Scenario> list = this
                .select(parameters.scenarioPredicate())
                .map(scenario -> {
                    var subGraph = subGraph(scenario, parameters);
                    return new Scenarios.Scenario(scenario, description(scenario), subGraph);
                }).toList();

        return new Scenarios(list);
    }

    @NotNull
    List<Call> subGraph(Span root, PerspectiveParameters parameters) {
        var subGraph = new ArrayList<Call>();
        new DepthFirstIterator<>(this.graph(), root.spanId()).forEachRemaining(spanId -> {
            Span span = this.span(spanId);
            if (span != null) {
                Span parent = this.span(span.parentSpanId());
                subGraph.add(Call.of(parent, span, parameters));
            }
        });
        return subGraph;
    }

    private Scenarios.Description description(Span root) {
        documentation.generator.Scenario scenario = root.attribute(Convention.DOCUMENTING_SCENARIO)
                .map(value -> Serialization.fromString(value, documentation.generator.Scenario.class))
                .orElseGet(() -> Scenario.title(root.name()));
        List<Scenarios.StepDescription> steps = graph.outgoingEdgesOf(root.spanId()).stream()
                .map(graph::getEdgeTarget)
                .map(this::span)
                .filter(Objects::nonNull)
                .filter(span -> span.attribute(Convention.DOCUMENTING_SCENARIO_STEP).isPresent())
                .map(Convention::createStepDescription)
                .toList();
        return new Scenarios.Description(scenario, steps);
    }
}
