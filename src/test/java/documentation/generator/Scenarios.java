package documentation.generator;

import java.util.List;
import java.util.stream.Stream;

record Scenarios(List<Scenario> scenarios) {

    record Description(documentation.generator.Scenario scenario, List<StepDescription> steps) {}

    record StepDescription(Span root, Step step, List<Highlighted> highlighted) {}

    record Highlighted(String information, String data) {}

    record Scenario(Span root, Description description, List<Call> subGraph) {
    }

    List<Span> roots() {
        return scenarios.stream().map(Scenario::root).toList();
    }

    Stream<Call> subGraph() {
        return scenarios.stream()
                .flatMap(scenario -> scenario.subGraph().stream());
    }
}
