package documentation.generator;

import lombok.Builder;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Mermaid {
    record SequenceDiagram(
            Perspective perspective,
            PerspectiveParameters parameters,
            SequenceDiagram.DiagramParameters diagramParameters
    ) implements Printable {

        SequenceDiagram(Perspective perspective, PerspectiveParameters parameters, DiagramParameters diagramParameters) {
            this.perspective = perspective.transform(
                    addReturningCallsAndResort,
                    participants -> participants
            );
            this.parameters = parameters;
            this.diagramParameters = diagramParameters;
        }

        static final UnaryOperator<Stream<Call>> addReturningCallsAndResort =
                stream -> stream
                        .flatMap(Call::includeReturning)
                        .sorted(Comparator.comparing(Call::start));

        @Builder
        record DiagramParameters(List<String> participantsGroupsOrder) {}

        @Override
        public void print(PrintWriter out) {
            out.println("sequenceDiagram");
            perspective.scenarioRoots().forEach(scenario ->
                    out.println("  participant " + encode(parameters.participantName(scenario)))
            );
            diagramParameters.participantsGroupsOrder()
                    .forEach(group -> {
                        var groupParticipants = perspective.participantGroup(group);
                        if (groupParticipants.hasSize(1)) {
                            String first = groupParticipants.getFirst();
                            if (group.equals(first)) {
                                out.println("  participant " + encode(first));
                                return;
                            }
                        }
                        out.println("  box " + group);
                        groupParticipants.stream()
                                .map(SequenceDiagram::encode)
                                .forEach(participant -> out.println("    participant " + participant));
                        out.println("  end");
                    });
            perspective.calls().stream()
                    .filter(call -> call.parent() != null)
                    .forEach(call -> {
                        switch (call.variant()) {
                            case INITIATING -> {
                                out.println("  " + encode(call.parentParticipant()) + " ->>+ " + encode(call.childParticipant()) + ": " + encode(call.callName()));
                                parameters.argumentsFilter(call.child())
                                        .forEach(arguments -> out.println("  Note left of " + encode(call.childParticipant()) + ": " + formatArgument(arguments.getValue())));
                            }
                            case RETURNING ->
                                    out.println("  " + encode(call.parentParticipant()) + " ->>- " + encode(call.childParticipant()) + ": " + encode(call.callName()));
                            case SELF_CALL ->
                                    out.println("  " + encode(call.parentParticipant()) + " ->> " + encode(call.childParticipant()) + ": " + (call.callName()));
                        }
                    });
        }

        private static String encode(String value) {
            return URLEncoder.encode(value, StandardCharsets.UTF_16).replace('+', ' ');
        }

        private static String formatArgument(Object argument) {
            return String.join("<br/> ", argument.toString().replaceAll("(?=[\\[=])|(?<==)", " ").split(",+|(?<=\\[)"));
        }
    }


    record Gantt(
            Perspective perspective,
            PerspectiveParameters parameters,
            Gantt.DiagramParameters diagramParameters
    ) implements Printable {

        Gantt(Perspective perspective, PerspectiveParameters parameters, DiagramParameters diagramParameters) {
            this.perspective = perspective.transform(
                    requiredPerspectiveModifier,
                    participants -> participants
            );
            this.parameters = parameters;
            this.diagramParameters = diagramParameters;
        }

        static final UnaryOperator<Stream<Call>> requiredPerspectiveModifier =
                stream -> stream
                        .sorted(Comparator.comparing(Call::start));

        @Builder
        record DiagramParameters(String title, String axisFormat) {

            public static DiagramParametersBuilder defaultParams() {
                return builder()
                        .title(null)
                        .axisFormat("%sms");
            }
        }

        @Override
        public void print(PrintWriter out) {
            out.println("gantt");
            if (diagramParameters.title() != null) {
                out.println("  title " + diagramParameters.title);
            }
            out.println("  dateFormat X");
            out.println("  axisFormat " + diagramParameters.axisFormat());
            perspective.scenarios().scenarios().forEach(scenario -> {
                Instant start = scenario.root().start();
                Instant max = Instant.ofEpochMilli(perspective.calls().stream()
                        .skip(1)
                        .mapToLong(call -> call.end().toEpochMilli() - call.start().toEpochMilli())
                        .max()
                        .orElse(start.toEpochMilli()));
                for (Call call : perspective.calls()) {
                    if (call.child().attribute("documenting.scenario").isPresent()) {
//                        start = call.start();
//                        out.println("    " + call.child().name() + ": "
//                                    + fromStart(start, call.start()) + ", " + fromStart(start, max));
                    } else if (call.child().attribute("documenting.scenario.step").isPresent()) {
                        start = call.start();
                        out.println("  section " + call.child().name());// + "\n" + highlights(call.child().attributes());
                    } else {
                        out.println("    " + call.childParticipant() + " " + call.callName() + ": "
                                    + fromStart(start, call.start()) + ", " + fromStart(start, call.end()));
                    }
                }
            });

        }

        private String highlights(Map<String, Object> attributes) {
            Pattern pattern = Pattern.compile("^documenting\\.scenario\\.highlight\\.(\\d)*\\..*");
            return attributes.keySet().stream()
                    .map(pattern::matcher)
                    .filter(Matcher::find)
                    .map(matcher -> matcher.toMatchResult().group(1))
                    .mapToLong(Long::parseLong)
                    .sorted()
                    .mapToObj(index -> attributes.get("documenting.scenario.highlight." + index + ".information") + "\n" +
                                       attributes.get("documenting.scenario.highlight." + index + ".data"))
                    .collect(Collectors.joining("\n"));
        }

        private long fromStart(Instant start, Instant call) {
            return call.toEpochMilli() - start.toEpochMilli();
        }
    }
}
