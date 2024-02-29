package documentation.generator;

import com.google.protobuf.ByteString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Scenarios {
    private final List<Scenario> scenarios;
    private final Map<ByteString, StepDescription> steps;

    Scenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
        this.steps = scenarios.stream()
                .flatMap(scenario -> scenario.description().steps().stream())
                .collect(Collectors.toUnmodifiableMap(
                        step -> step.root().spanId(),
                        step -> step
                ));
    }

    public void forEachActor(Consumer<Actor> consumer) {
        scenarios.stream()
                .flatMap(scenario -> scenario.description().steps().stream())
                .flatMap(step -> step.step().actors().stream())
                .distinct()
                .forEach(consumer);
    }

    public void forEachScenario(Consumer<Scenario> consumer) {
        scenarios.forEach(consumer);
    }

    public Optional<StepDescription> stepDescription(Span span) {
        return Optional.ofNullable(steps.get(span.spanId()));
    }

    record Description(documentation.generator.Scenario scenario, List<StepDescription> steps) {}

    record StepDescription(Span root, Step step, List<Highlighted> highlighted) {
        public String actorName() {
            return step.actors().stream()
                    .map(Actor::name)
                    .collect(Collectors.joining("|"));
        }
    }

    record Highlighted(String information, String data) {}

    record Scenario(Span root, Description description, List<Call> subGraph) {
    }

    Stream<Call> subGraph() {
        return scenarios.stream()
                .flatMap(scenario -> scenario.subGraph().stream());
    }
}
