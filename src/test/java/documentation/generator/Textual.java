package documentation.generator;

import java.io.PrintWriter;
import java.util.List;

class Textual {

    public static String toKebabCase(String text) {
        return text.toLowerCase().replaceAll(" +", "-");
    }

    static class Markdown implements Printable {
        private final Perspective perspective;
        private final PerspectiveParameters parameters;

        Markdown(Perspective perspective, PerspectiveParameters parameters) {
            this.perspective = perspective;
            this.parameters = parameters;
        }

        Printable scenariosToc() {
            return out -> {
                out.print("# ");
                out.println("Scenarios");
                perspective.scenarios().forEachScenario(scenario -> {
                    String scenarioTitle = scenario.description().scenario().title;
                    linkToHeader(out, scenarioTitle, scenarioTitle);
                });
                out.println();
            };
        }

        Printable scenarioSection(Scenario scenario, List<Step> steps) {
            return out -> {
                String scenarioTitle = scenario.title;
                targetHeader(out, 2, scenarioTitle, scenarioTitle);
                steps.forEach(stepDescription -> {
                    String stepTitle = stepDescription.step();
                    linkToHeader(out, stepTitle, scenarioTitle + "--" + stepTitle);
                });
                out.println();
            };
        }

        @Override
        public void print(PrintWriter out) {
            out.print("# ");
            out.println("Scenarios");
            perspective.scenarios().forEachScenario(scenario -> {
                String scenarioTitle = scenario.description().scenario().title;
                linkToHeader(out, scenarioTitle, scenarioTitle);
            });
            out.println();

            perspective.scenarios().forEachScenario(scenario -> {
                String scenarioTitle = scenario.description().scenario().title;
                out.print("## ");
                out.println(scenarioTitle);
                out.println();
                scenario.description().steps().forEach(stepDescription -> {
                    String stepTitle = stepDescription.step().step();
                    linkToHeader(out, stepTitle, scenarioTitle + "--" + stepTitle);
                });
                out.println();

                scenario.forEachStep(stepDescription -> {
                    String stepTitle = stepDescription.step().step();
                    targetHeader(out, 3, stepTitle, scenarioTitle + "--" + stepTitle);
                    out.print("*actors:* ");
                    out.println(stepDescription.actorName());
                    if (!stepDescription.highlighted().isEmpty()) {
                        out.println("*highlighted:*");
                        out.println(Serialization.stringify(stepDescription.highlighted()));
                    }
                    out.println();
                });
            });
        }

        private static void targetHeader(PrintWriter out, int level, String title, String target) {
            out.println("<h" + level + " id=\"" + toKebabCase(target) + "\">" + title + "</h" + level + ">");
        }

        private static void linkToHeader(PrintWriter out, String title, String target) {
            out.print("1. [");
            out.print(title);
            out.print("](#");
            out.print(toKebabCase(target));
            out.println(")");
        }
    }

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
