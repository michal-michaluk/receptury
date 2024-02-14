package documentation.generator;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record Perspective(Scenarios scenarios,
                   List<Call> calls,
                   Participants participants) {

    List<Span> scenarioRoots() {
        return scenarios.roots();
    }

    record Participants(LinkedHashMap<String, LinkedHashSet<String>> participants) {

        Group get(String group) {
            return new Group(group, Objects.requireNonNullElseGet(participants.get(group), LinkedHashSet::new));
        }
    }

    Group participantGroup(String group) {
        return participants.get(group);
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
                                .map(parent -> Call.of(parent, call.child(), parameters.participantName(), parameters.callName()))
                                .stream();
                    } else {
                        return Stream.empty();
                    }
                })
                .toList();
    }

    @NotNull
    private static Participants collectParticipants(PerspectiveParameters parameters, List<Call> calls) {
        return new Participants(calls.stream()
                .filter(call -> call.parent() != null)
                .collect(Collectors.groupingBy(
                        call -> parameters.participantGroup().apply(call.child()),
                        LinkedHashMap::new,
                        Collectors.mapping(Call::childParticipant,
                                Collectors.toCollection(LinkedHashSet::new))
                )));
    }
}
