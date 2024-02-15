package documentation.generator;

import org.assertj.core.api.Assertions;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;

class SinkTest {

    @TempDir
    Path tempDir;

    String markerLine = "%%test-marker";

    @Language("Markdown")
    String previousMarkdown = """
            # Sequence Diagram
            Some diagram description
            ```mermaid
            %%test-marker
            sequenceDiagram
                participant scenario
            ```
            # Next Scenario
            ```mermaid
            %%other-marker
            sequenceDiagram
                participant scenario2
            ```
            """;

    @Language("Mermaid")
    String newDiagram = """
            sequenceDiagram
                participant actor
                box web
                    participant web app
                end
            """;

    @Language("Markdown")
    String updatedMarkdown = """
            # Sequence Diagram
            Some diagram description
            ```mermaid
            %%test-marker
            sequenceDiagram
                participant actor
                box web
                    participant web app
                end
            ```
            # Next Scenario
            ```mermaid
            %%other-marker
            sequenceDiagram
                participant scenario2
            ```
            """;

    @Language("Markdown")
    String freshMarkdown = """
                        
            ```mermaid
            %%test-marker
            sequenceDiagram
                participant actor
                box web
                    participant web app
                end
            ```
            """;

    @Test
    void toMarkdownCreatesFileWhenItDoesNotExist() throws IOException {
        Path path = tempDir.resolve("nonexistent.md");

        whenMarkdownIsInjectedInto(path, markerLine);

        Assertions.assertThat(path)
                .exists()
                .content().isEqualTo(freshMarkdown);
    }

    @Test
    void toMarkdownReplacesExistingMermaidBlock() throws IOException {
        Path path = tempDir.resolve("existing.md");
        Files.writeString(path, previousMarkdown);

        whenMarkdownIsInjectedInto(path, markerLine);

        Assertions.assertThat(path)
                .exists()
                .content().isEqualTo(updatedMarkdown);
    }

    @Test
    void toMarkdownAppendsNewMermaidBlockWhenMarkerNotFound() throws IOException {
        Path path = tempDir.resolve("append.md");
        Files.writeString(path, previousMarkdown);

        String markerLine = "%%not-existing-marker";
        whenMarkdownIsInjectedInto(path, markerLine);

        Assertions.assertThat(path)
                .exists()
                .content().isEqualTo(
                        previousMarkdown +
                        freshMarkdown.replace("%%test-marker", "%%not-existing-marker"));
    }

    @Test
    void toMarkdownThrowsRuntimeException() throws IOException {
        Path path = tempDir.resolve("readonly.md").toAbsolutePath();
        Files.createFile(path);
        Files.setPosixFilePermissions(path, Set.of(OWNER_READ));

        Assertions.assertThatThrownBy(() -> whenMarkdownIsInjectedInto(path, markerLine))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(AccessDeniedException.class);
    }

    private void whenMarkdownIsInjectedInto(Path path, String markerLine) {
        Sink sink = Sink.toMarkdown(path, markerLine);
        sink.accept(out -> out.println(newDiagram));
    }
}
