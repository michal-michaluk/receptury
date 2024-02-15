package documentation.generator;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public interface Sink {
    void accept(Printable printable);

    @NotNull
    static Sink toFile(Path path) {
        return printable -> {
            try {
                Files.deleteIfExists(path);
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path);
                 PrintWriter out = new PrintWriter(writer)) {
                printable.print(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Creates a Sink that writes the output of a Printable to a Markdown file.
     * If the file does not exist, it is created. If it does exist, previous Mermaid diagram content is replaced.
     * The output is placed in a mermaid code block, identified by the provided marker line.
     * <p>
     * <pre>Sink.toMarkdown("src/docs/markdown-with-diagram.md", "%%device-installation-sequence")`</pre>
     * Will inject the diagram into the following code block:
     * <pre>
     * ```mermaid
     * %%device-installation-sequence
     * sequenceDiagram
     *     participant scenario
     *     box web
     *     end
     * ```
     * </pre>
     * If file does not contain a code block with the provided marker line, a new code block with marker and diagram content is appended to the file.
     *
     * @param path       The path of the Markdown file to write to.
     * @param markerLine The first line of the target Mermaid code block.
     * @return A Sink that injects Mermaid diagram into to the specified Markdown file.
     */
    @NotNull
    static Sink toMarkdown(Path path, String markerLine) {
        return printable -> {
            try {
                if (Files.notExists(path)) {
                    Files.createFile(path);
                }
                String previousContent = Files.readString(path);
                String diagram = generate(printable);
                String markdown = prepare(previousContent, markerLine, diagram);
                Files.writeString(path, markdown);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @NotNull
    private static String generate(Printable printable) {
        StringWriter out = new StringWriter();
        printable.print(new PrintWriter(out));
        return out.toString().stripTrailing();
    }

    private static String prepare(String previousContent, String markerLine, String diagram) {
        Pattern pattern = Pattern.compile("[ \\t]*```mermaid\\s*\\n" + Pattern.quote(markerLine) + "\\s*\\n(\\n|.)*?\\n\\s*```");
        if (pattern.matcher(previousContent).find()) {
            return pattern.matcher(previousContent).replaceAll("```mermaid\n" + markerLine + "\n" + diagram + "\n```");
        } else {
            if (!markerLine.startsWith("%%")) {
                markerLine = "%%" + markerLine;
            }
            return previousContent + "\n```mermaid\n" + markerLine + "\n" + diagram + "\n```\n";
        }
    }
}
