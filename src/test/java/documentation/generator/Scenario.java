package documentation.generator;

import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;

import static io.opentelemetry.api.trace.Span.current;

public class Scenario {

    private String title;
    private String description;
    private List<String> preconditions = new ArrayList<>();
    private List<String> includes = new ArrayList<>();
    private List<String> excludes = new ArrayList<>();

    public static Scenario title(@Language("Markdown") String title) {
        Scenario scenario = new Scenario();
        scenario.title = title;
        return scenario;
    }

    public Scenario description(@Language("Markdown") String description) {
        this.description = description;
        return this;
    }

    public Scenario precondition(@Language("Markdown") String... preconditions) {
        this.preconditions.addAll(List.of(preconditions));
        return this;
    }

    public Scenario includes(@Language("Markdown") String... inclusions) {
        this.includes.addAll(List.of(inclusions));
        return this;
    }

    public Scenario excludes(@Language("Markdown") String... exclusions) {
        this.excludes.addAll(List.of(exclusions));
        return this;
    }

    public void begin() {
        current()
                .updateName(title)
                .setAttribute(Convention.DOCUMENTING_SCENARIO, toString());
    }

    @Override
    public String toString() {
        return Serialization.stringify(this);
    }
}
