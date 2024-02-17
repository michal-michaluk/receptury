package devices.configuration.installations;

import devices.configuration.installations.InstallationProcessState.State;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;
import java.util.Optional;

@Primary
@Component
@Transactional
@AllArgsConstructor
class InstallationReadModel {
    private final JpaRepository repository;

    @Builder(builderMethodName = "params")
    record QueryParams(List<InstallationProcessState.State> states) {
        QueryParams(List<State> states) {
            this.states = states == null ? List.of() : states;
        }

        public boolean anyStatus() {
            return states.isEmpty();
        }
    }

    @Transactional(readOnly = true)
    @WithSpan
    public Optional<InstallationProcessState> queryByOrderId(@SpanAttribute String deviceId) {
        return repository.findById(deviceId)
                .map(InstallationEntity::state);
    }

    @Transactional(readOnly = true)
    @WithSpan
    public Page<InstallationProcessState> query(@SpanAttribute QueryParams params, @SpanAttribute Pageable pageable) {
        return repository.findAllMatching(params.anyStatus(), params.states(), pageable)
                .map(InstallationEntity::state);
    }

    @EventListener
    @WithSpan
    public void projectionOf(@SpanAttribute InstallationProcessState snapshot) {
        put(snapshot.orderId(), snapshot);
    }

    private void put(String orderId, InstallationProcessState state) {
        repository.save(repository.findById(orderId)
                .orElseGet(() -> new InstallationEntity(orderId))
                .state(state)
        );
    }

    interface JpaRepository extends CrudRepository<InstallationEntity, String> {
        @Query(
                value = """
                        select * from installation
                        where (:anyStatus or (state ->> 'state') in (:states))
                        """,
                countQuery = """
                        select count(*) from installation
                        where (:anyStatus or (state ->> 'state') in (:states))
                        """,
                nativeQuery = true)
        Page<InstallationEntity> findAllMatching(
                @Param("anyStatus") boolean anyStatus, @Param("states") List<State> states,
                Pageable pageable
        );

        @Modifying
        @Query(value = "delete from installation where true", nativeQuery = true)
        void truncate();
    }

    @Entity
    @Table(name = "installation")
    @NoArgsConstructor
    static class InstallationEntity {
        @Id
        private String orderId;
        @Type(type = "jsonb")
        private InstallationProcessState state;

        public InstallationEntity(String orderId) {
            this.orderId = orderId;
        }

        public InstallationProcessState state() {
            return state;
        }

        public InstallationEntity state(InstallationProcessState state) {
            this.state = state;
            return this;
        }
    }
}
