package documentation.generator;

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class TelemetryCollector implements AutoCloseable {
    private final HttpServer server;

    private TelemetryCollector(HttpServer server) {
        this.server = server;
    }

    public static TelemetryCollector collectorToTempDirectory() throws IOException {
        Path directory = Files.createTempDirectory("traces-");
        var atomic = new AtomicLong(0);
        return TelemetryCollector.collector(
                () -> Files.newOutputStream(directory.resolve(atomic.getAndIncrement() + ".trace"), CREATE, TRUNCATE_EXISTING)
        );
    }

    public static TelemetryCollector collectorToDirectory(Path directory) throws IOException {
        Files.createDirectories(directory);
        var atomic = new AtomicLong(0);
        return TelemetryCollector.collector(
                () -> Files.newOutputStream(directory.resolve(atomic.getAndIncrement() + ".trace"), CREATE, TRUNCATE_EXISTING)
        );
    }

    public static TelemetryCollector collectorToPipe(Consumer<InputStream> downstream) throws IOException {
        return TelemetryCollector.collector(() -> {
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);
            downstream.accept(in);
            return out;
        });
    }

    public interface OutputStreamProvider {
        OutputStream get() throws IOException;
    }

    public static TelemetryCollector collector(OutputStreamProvider downstream) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(43418), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream out = downstream.get()) {
                exchange.getRequestBody().transferTo(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            exchange.close();
        });
        server.start();
        return new TelemetryCollector(server);
    }

    @Override
    public void close() throws Exception {
        server.stop(1);
    }
}
