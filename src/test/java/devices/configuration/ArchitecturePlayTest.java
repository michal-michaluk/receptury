package devices.configuration;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.thirdparty.com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.EventListener;

import java.util.Set;
import java.util.function.Consumer;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.base.DescribedPredicate.or;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.core.domain.JavaCodeUnit.Predicates.method;
import static com.tngtech.archunit.core.domain.JavaMember.Predicates.declaredIn;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameMatching;

public class ArchitecturePlayTest {
    JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("devices.configuration");

    @Test
    public void architecture() {
        var printer = new ConsolePrinter(importedClasses);

        importedClasses.getPackage("devices.configuration")
                .getSubpackages().stream()
                .filter(javaPackage -> !Set.of("mediators", "tools").contains(javaPackage.getRelativeName()))
                .forEach(javaPackage -> {
                    printer.printScope(javaPackage.getRelativeName(), () -> {
                        var inScope = resideInAPackage(javaPackage.getName());

                        var interactions = method()
                                .and(declaredIn(ArchitectureDescription.services.and(inScope)));
                        var reads = interactions.and(nameMatching("^(get|find|fetch).*"));
                        var listeners = interactions.and(annotatedWith(EventListener.class));
                        var commands = interactions.and(DescribedPredicate.not(reads.or(listeners)));

                        var model = ArchitectureDescription.model.and(inScope)
                                .and(TOP_LEVEL_CLASSES);

                        var events = implement(simpleName("DomainEvent")).and(inScope);

                        var ports = inScope.and(or(
                                ArchitectureDescription.services.and(inScope),
                                INTERFACES
                                        .and(TOP_LEVEL_CLASSES)
                                        .and(assignableTo(ArchitectureDescription.adapters
                                                .or(ArchitectureDescription.mediators))
                                        )
                        ));

                        var adapters = ArchitectureDescription.adapters.and(inScope)
                                .and(TOP_LEVEL_CLASSES).and(not(INTERFACES));

                        printer.printMethods("reads", reads);
                        printer.printMethods("commands", commands);
                        printer.printMethods("listeners", listeners);
                        printer.printClasses("model", model);
                        printer.printClasses("events", events);
                        printer.printClasses("ports", ports);
                        printer.printClasses("adapters", adapters);
                        printer.print("");
                    });
                });
    }

    @RequiredArgsConstructor
    public static class ConsolePrinter {

        private final JavaClasses importedClasses;
        Consumer<String> out = System.out::println;
        int depth = 0;

        private void print(String text) {
            out.accept(" ".repeat(depth) + text);
        }

        private void printScope(String description, Runnable function) {
            print(description + ":");

            depth++;
            function.run();
            depth--;
        }

        private void printClasses(String description, DescribedPredicate<JavaClass> predicate) {
            print(description + ":");
            importedClasses.forEach(clazz -> {
                if (predicate.test(clazz)) {
                    print("  " + clazz.getSimpleName());
                }
            });
        }

        private void printMethods(String description, DescribedPredicate<JavaCodeUnit> predicate) {
            AbstractClassesTransformer<JavaMethod> transformer = new AbstractClassesTransformer<>("methods") {
                @Override
                public Iterable<JavaMethod> doTransform(JavaClasses collection) {
                    ImmutableSet.Builder<JavaMethod> result = ImmutableSet.builder();
                    for (JavaClass javaClass : collection) {
                        result.addAll(javaClass.getMethods());
                    }
                    return result.build();
                }
            };
            print(description + ":");
            transformer.doTransform(importedClasses).forEach(method -> {
                if (predicate.test(method)) {
                    print("  " + method.getName());
                }
            });
        }
    }
}
