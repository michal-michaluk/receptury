package documentation.generator;

import java.util.List;
import java.util.stream.Stream;

record Scenarios(List<Scenario> scenarios) {

    record Description() {}

    record Scenario(Span root, Description description, List<Call> subGraph) {
        String name() {
            return root.name();
        }
    }

    List<Span> roots() {
        return scenarios.stream().map(Scenario::root).toList();
    }

    Stream<Call> subGraph() {
        return scenarios.stream()
                .flatMap(scenario -> scenario.subGraph().stream());
    }
}
