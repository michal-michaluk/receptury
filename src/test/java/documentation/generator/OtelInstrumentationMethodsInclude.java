package documentation.generator;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.thirdparty.com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public class OtelInstrumentationMethodsInclude {

    private final JavaClasses importedClasses;
    private final DescribedPredicate<JavaCodeUnit> predicate;

    public void printInclude(PrintWriter out) {
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
        record Method(String clazz, String method) {}
        Iterable<JavaMethod> interest = transformer.doTransform(importedClasses);
        StreamSupport.stream(interest.spliterator(), false)
                .filter(predicate)
                .map(method -> new Method(method.getOwner().getFullName(), method.getName()))
                .collect(Collectors.collectingAndThen(Collectors.groupingBy(
                        method -> method.clazz,
                        Collectors.mapping(method -> method.method, Collectors.joining(",", "[", "]"))
                ), Map::entrySet))
                .forEach(entry -> {
                    out.print(entry.getKey());
                    out.print(entry.getValue());
                    out.print(';');
                });
    }

    public String includeString() {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             PrintWriter stream = new PrintWriter(output)) {

            this.printInclude(stream);
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSystemProperty() {
        System.setProperty("otel.instrumentation.methods.include", includeString());
    }

    public void toPropertyFile(Path path) {
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path);
             PrintWriter out = new PrintWriter(writer)) {
            out.print("otel.instrumentation.methods.include = ");
            this.printInclude(out);
            out.println();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
