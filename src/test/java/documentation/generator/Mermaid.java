package documentation.generator;

import lombok.Builder;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
                    addReturningCallsAndResort(perspective.scenarios()),
                    participants -> participants
            );
            this.parameters = parameters;
            this.diagramParameters = diagramParameters;
        }

        private static UnaryOperator<Stream<Call>> addReturningCallsAndResort(Scenarios scenarios) {
            return stream -> stream
                    .flatMap(replaceWithScenarioDescription(scenarios))
                    .flatMap(Call::includeReturning)
                    .sorted(Comparator.comparing(Call::start));
        }

        public static Function<Call, Stream<Call>> replaceWithScenarioDescription(Scenarios scenarios) {
            Set<String> attributes = Set.of(
                    Convention.DOCUMENTING_SCENARIO,
                    Convention.DOCUMENTING_SCENARIO_STEP);
            return (call) -> {
                if (call.parent() == null || call.child().anyOfAttributes(attributes).isPresent()) {
                    return Stream.of();
                }

                return scenarios.stepDescription(call.parent())
                        .map(Scenarios.StepDescription::actorName)
                        .filter(actorName -> !actorName.isEmpty())
                        .map(call::replaceParentParticipantName)
                        .orElseGet(() -> Stream.of(call));
            };
        }

        @Builder
        record DiagramParameters() {
            public static DiagramParametersBuilder defaultParams() {
                return builder();
            }
        }

        @Override
        public void print(PrintWriter out) {
            // todo:
            // weryfikacja markdown
            // highlighty
            // ignor stepu
            // praca bez opisu scenariusza i stepów

            out.println("sequenceDiagram");
            out.println("  box actors");
            perspective.scenarios().forEachActor(actor ->
                    out.println("  actor " + encode(actor.name()))
            );
            out.println("  end");

            perspective.participants().forEachGroup(group -> {
                        if (group.hasSize(1)) {
                            String first = group.getFirst();
                            if (group.name().equals(first)) {
                                out.println("  participant " + encode(first));
                                return;
                            }
                        }
                        out.println("  box " + group.name());
                        group.stream()
                                .map(SequenceDiagram::encode)
                                .forEach(participant -> out.println("    participant " + participant));
                        out.println("  end");
                    }
            );
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
            // todo:
            // reset czasu: EACH_SCENARIO, EACH_STEP, NEVER
            // przesunięcie negatywne czasu
            // aktor przy stepie
            // info o kolorach, per grupa / partycypant
            // obsługa markdown / usunięcie go
            // highlighty


            out.println("gantt");
            if (diagramParameters.title() != null) {
                out.println("  title " + diagramParameters.title);
            }
            out.println("  dateFormat X");
            out.println("  axisFormat " + diagramParameters.axisFormat());
            perspective.scenarios().forEachScenario(scenario -> {
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
