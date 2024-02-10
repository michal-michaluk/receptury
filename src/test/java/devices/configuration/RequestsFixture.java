package devices.configuration;

import devices.configuration.installations.InstallationProcessState;
import devices.configuration.tools.JsonConfiguration;
import io.netty.handler.logging.LogLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import javax.annotation.PostConstruct;

@Lazy
@Component
@Profile("integration-test")
public class RequestsFixture {

    @Value("${local.server.port}")
    private int port;
    private WebClient client;
    private String jwt = null;
    public final Installations installations = new Installations();

    @PostConstruct
    void setUp() {
        this.client = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().wiretap(this.getClass().getCanonicalName(), LogLevel.INFO, AdvancedByteBufFormat.TEXTUAL)
                ))
                .build();
    }

    public void withJwt(String jwt) {
        this.jwt = jwt;
    }

    public void clearJwt() {
        this.jwt = null;
    }

    public class Installations {
        public Iterable<InstallationProcessState> get(int page, int size) {
            return client.get()
                    .uri("/installations?page={page}&size={size}", page, size)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(
                            new ParameterizedTypeReference<JsonConfiguration.SimplePage<InstallationProcessState>>() {}
                    ).block();
        }

        public InstallationProcessState get(String orderId1) {
            return client.get()
                    .uri("/installations/{orderId}", orderId1)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(InstallationProcessState.class).block();
        }

        public InstallationProcessState patch(String orderId, Object body) {
            return client.patch()
                    .uri("/installations/{orderId}", orderId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(body))
                    .retrieve().bodyToMono(InstallationProcessState.class).block();
        }
    }
}
