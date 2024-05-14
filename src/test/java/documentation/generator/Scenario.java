package documentation.generator;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;

import static io.opentelemetry.api.trace.Span.current;

public class Scenario {

    String title;
    String description;
    List<String> preconditions = new ArrayList<>();
    List<String> includes = new ArrayList<>();
    List<String> excludes = new ArrayList<>();

    public static Scenario title(@Language("Markdown") String title) {
        Scenario scenario = new Scenario();
        scenario.title = title;
        return scenario;
    }

    public static void begin(@Language("Markdown") String title, Runnable implementation) {
        title(title).begin(implementation);
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
        Span span = current();
        span.setAttribute(Convention.DOCUMENTING_SCENARIO, toString());
        if (title != null) {
            span.updateName(title);
        }
    }

    @WithSpan
    public void begin(Runnable implementation) {
        begin();
        implementation.run();
    }

    @Override
    public String toString() {
        return Serialization.stringify(this);
    }
}
