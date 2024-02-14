package documentation.generator;

import com.google.protobuf.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                    var subGraph = scenarioSubGraph(scenario, parameters);
                    return new Scenarios.Scenario(scenario, new Scenarios.Description(), subGraph);
                }).toList();

        return new Scenarios(list);
    }

    @NotNull
    private List<Call> scenarioSubGraph(Span scenarioRoot, PerspectiveParameters parameters) {
        var scenarioSubGraph = new ArrayList<Call>();
        new DepthFirstIterator<>(this.graph(), scenarioRoot.spanId()).forEachRemaining(spanId -> {
            Span span = this.span(spanId);
            if (span != null) {
                Span parent = this.span(span.parentSpanId());
                scenarioSubGraph.add(Call.of(parent, span, parameters.participantName(), parameters.callName()));
            }
        });
        return scenarioSubGraph;
    }
}
