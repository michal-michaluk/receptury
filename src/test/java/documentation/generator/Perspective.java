package documentation.generator;

import documentation.generator.Scenarios.Description;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record Perspective(Scenarios scenarios,
                   List<Call> calls,
                   Participants participants) {

    record Participants(LinkedHashMap<String, LinkedHashSet<String>> participants) {

        Group get(String group) {
            return new Group(group, Objects.requireNonNullElseGet(participants.get(group), LinkedHashSet::new));
        }

        public void forEachGroup(Consumer<Group> function) {
            participants.forEach((group, participants) -> function.accept(new Group(group, participants)));
        }

        public void forEachParticipant(Consumer<String> function) {
            participants.values().stream()
                    .flatMap(Collection::stream)
                    .forEach(function);
        }
    }

    record Group(String name, LinkedHashSet<String> participants) {

        boolean hasSize(int size) {
            return participants.size() == size;
        }

        String getFirst() {
            return participants.iterator().next();
        }

        Stream<String> stream() {
            return participants.stream();
        }
    }

    static Perspective perspective(TelemetrySpans telemetry, Scenarios scenarios, PerspectiveParameters parameters) {
        var calls = filterAndSortCalls(telemetry, parameters, scenarios.subGraph());
        var participants = collectParticipants(parameters, calls);
        return new Perspective(scenarios, calls, participants);
    }

    Perspective transform(UnaryOperator<Stream<Call>> callsModifier,
                          UnaryOperator<Participants> participantsModifier) {
        return new Perspective(
                scenarios,
                callsModifier.apply(calls.stream()).toList(),
                participantsModifier.apply(participants)
        );
    }

    @NotNull
    private static List<Call> filterAndSortCalls(TelemetrySpans telemetry, PerspectiveParameters parameters, Stream<Call> subGraph) {
        return subGraph
                .sorted(Comparator.comparing(Call::start).reversed())
                .flatMap(call -> {
                    if (call.parent() == null) {
                        return Stream.of(call);
                    } else if (parameters.include().test(call.parent()) && !parameters.exclude().test(call.parent())
                               && parameters.include().test(call.child()) && !parameters.exclude().test(call.child())) {
                        return Stream.of(call);
                    } else if (parameters.include().test(call.child()) && !parameters.exclude().test(call.child())) {
                        //look up for better parent
                        return Stream.iterate(call.parent(), span -> span.parentSpanId() != null, span -> telemetry.span(span.parentSpanId()))
                                .filter(parent -> parameters.include().test(parent) && !parameters.exclude().test(parent))
                                .findFirst()
                                .map(parent -> Call.of(parent, call.child(), parameters))
                                .stream();
                    } else {
                        return Stream.empty();
                    }
                })
                .sorted(Comparator.comparing(Call::start))
                .toList();
    }

    @NotNull
    private static Participants collectParticipants(PerspectiveParameters parameters, List<Call> calls) {
        return new Participants(calls.stream()
                .collect(Collectors.groupingBy(
                        call -> parameters.participantGroup().apply(call.child()),
                        LinkedHashMap::new,
                        Collectors.mapping(Call::childParticipant,
                                Collectors.toCollection(LinkedHashSet::new))
                )));
    }

    interface Splitting {
        record Split<T>(int index, T scope, Perspective perspective) {}

        static Stream<Split<Scenario>> forEachScenario(TelemetrySpans telemetry, Scenarios scenarios, PerspectiveParameters parameters) {
            Stream.Builder<Split<Scenario>> stream = Stream.builder();
            var i = new AtomicInteger(0);
            scenarios.forEachScenario(scenario -> {
                var calls = filterAndSortCalls(telemetry, parameters, scenario.subGraph().stream());
                var participants = collectParticipants(parameters, calls);
                stream.add(new Split<>(
                        i.getAndIncrement(),
                        scenario.description().scenario(),
                        new Perspective(new Scenarios(List.of(scenario)), calls, participants)
                ));
            });
            return stream.build();
        }

        record StepScope(int index, Scenario scenario, Step step) {}

        static Stream<Split<StepScope>> forEachScenarioStep(TelemetrySpans telemetry, Scenarios scenarios, PerspectiveParameters parameters) {
            Stream.Builder<Split<StepScope>> stream = Stream.builder();
            var i = new AtomicInteger(0);
            scenarios.forEachScenario(scenario -> {
                        var j = new AtomicInteger(0);
                        scenario.forEachStep(step -> {
                            var calls = filterAndSortCalls(telemetry, parameters, telemetry.subGraph(step.root(), parameters).stream());
                            var participants = collectParticipants(parameters, calls);
                            Scenarios.Scenario filtered = new Scenarios.Scenario(
                                    step.root(),
                                    new Description(scenario.description().scenario(), List.of(step)),
                                    calls
                            );
                            stream.add(new Split<>(
                                    i.getAndIncrement(),
                                    new StepScope(j.getAndIncrement(), scenario.description().scenario(), step.step()),
                                    new Perspective(new Scenarios(List.of(filtered)), calls, participants)
                            ));
                        });
                    }
            );
            return stream.build();
        }
    }
}
