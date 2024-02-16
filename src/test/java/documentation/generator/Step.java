package documentation.generator;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.List.copyOf;
import static java.util.List.of;

public class Step {
    enum Type {GIVEN, WHEN, THEN}

    private static AtomicInteger counter = new AtomicInteger(0);

    private final String step;
    private final Type type;
    private final List<Actor> actors;

    Step(Type type, String step, List<Actor> actors) {
        this.type = type;
        this.step = step;
        this.actors = actors;
    }

    @WithSpan
    public static void given(@Language("Markdown") String step, Runnable implementation) {
        attach(new Step(Type.GIVEN, step, of()));
        implementation.run();
    }

    @WithSpan
    public static <T> T given(@Language("Markdown") String step, Supplier<T> implementation) {
        attach(new Step(Type.GIVEN, step, of()));
        return implementation.get();
    }

    @WithSpan
    public static void given(Actor actor, @Language("Markdown") String step, Runnable implementation) {
        attach(new Step(Type.GIVEN, step, of(actor)));
        implementation.run();
    }

    @WithSpan
    public static <T> T given(Actor actor, @Language("Markdown") String step, Supplier<T> implementation) {
        attach(new Step(Type.GIVEN, step, of(actor)));
        return implementation.get();
    }

    @WithSpan
    public static void given(List<Actor> actors, @Language("Markdown") String step, Runnable implementation) {
        attach(new Step(Type.GIVEN, step, actors));
        implementation.run();
    }

    @WithSpan
    public static <T> T given(List<Actor> actors, @Language("Markdown") String step, Supplier<T> implementation) {
        attach(new Step(Type.GIVEN, step, actors));
        return implementation.get();
    }

    @WithSpan
    public static void when(@Language("Markdown") String step, Runnable implementation) {
        attach(new Step(Type.WHEN, step, of()));
        implementation.run();
    }

    @WithSpan
    public static <T> T when(@Language("Markdown") String step, Supplier<T> implementation) {
        attach(new Step(Type.WHEN, step, of()));
        return implementation.get();
    }

    @WithSpan
    public static void when(Actor actor, @Language("Markdown") String step, Runnable implementation) {
        attach(new Step(Type.WHEN, step, of(actor)));
        implementation.run();
    }

    @WithSpan
    public static <T> T when(Actor actor, @Language("Markdown") String step, Supplier<T> implementation) {
        attach(new Step(Type.WHEN, step, of(actor)));
        return implementation.get();
    }

    @WithSpan
    public static void when(List<Actor> actors, @Language("Markdown") String step, Runnable implementation) {
        attach(new Step(Type.WHEN, step, copyOf(actors)));
        implementation.run();
    }

    @WithSpan
    public static <T> T when(List<Actor> actors, @Language("Markdown") String step, Supplier<T> implementation) {
        attach(new Step(Type.WHEN, step, copyOf(actors)));
        return implementation.get();
    }

    @WithSpan
    public static void then(@Language("Markdown") String step, Runnable implementation) {
        attach(new Step(Type.THEN, step, of()));
        implementation.run();
    }

    @WithSpan
    public static <T> T then(@Language("Markdown") String step, Supplier<T> implementation) {
        attach(new Step(Type.THEN, step, of()));
        return implementation.get();
    }

    @WithSpan
    public static void then(Actor actor, @Language("Markdown") String step, Runnable implementation) {
        attach(new Step(Type.THEN, step, of(actor)));
        implementation.run();
    }

    @WithSpan
    public static <T> T then(Actor actor, @Language("Markdown") String step, Supplier<T> implementation) {
        attach(new Step(Type.THEN, step, of(actor)));
        return implementation.get();
    }

    @WithSpan
    public static void then(List<Actor> actors, @Language("Markdown") String step, Runnable implementation) {
        attach(new Step(Type.THEN, step, copyOf(actors)));
        implementation.run();
    }

    @WithSpan
    public static <T> T then(List<Actor> actors, @Language("Markdown") String step, Supplier<T> implementation) {
        attach(new Step(Type.THEN, step, copyOf(actors)));
        return implementation.get();
    }

    public static <T> T highlight(@Language("Markdown") String information, T data) {
        int number = counter.getAndIncrement();
        Span.current().setAttribute(
                "documenting.scenario.highlight." + number + "information",
                information
        );
        Span.current().setAttribute(
                "documenting.scenario.highlight." + number + "data",
                Tools.stringify(data)
        );
        return data;
    }

    private static void attach(Step step) {
        Span.current()
                .updateName(step.step)
                .setAttribute("documenting.scenario.step", step.toString());
    }

    @Override
    public String toString() {
        return Tools.stringify(this);
    }
}
