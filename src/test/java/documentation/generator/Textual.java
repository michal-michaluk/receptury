package documentation.generator;

import java.io.PrintWriter;

class Textual {
    static class VerboseScenario implements Printable {
        private final Perspective perspective;
        private final PerspectiveParameters parameters;

        VerboseScenario(Perspective perspective, PerspectiveParameters parameters) {
            this.perspective = perspective;
            this.parameters = parameters;
        }

        @Override
        public void print(PrintWriter out) {
            perspective.scenarios().forEachScenario(scenario -> {
                out.println("scenario: ");
                out.println(scenario.description().scenario());
                scenario.description().steps().forEach(stepDescription -> {
                    out.println(stepDescription.step());
                    if (!stepDescription.highlighted().isEmpty()) {
                        out.println("highlighted:");
                        out.println(Serialization.stringify(stepDescription.highlighted()));
                    }
                });
            });
            out.println("participants:");
            perspective.participants().participants().forEach((group, participants) -> {
                out.print(group);
                out.print(": ");
                out.println(participants);
            });

            out.println();
            out.println("spans:");
            perspective.calls().forEach(call ->
                    out.println(call.child().toString(call.child().attributes().keySet()))
            );
        }
    }
}
