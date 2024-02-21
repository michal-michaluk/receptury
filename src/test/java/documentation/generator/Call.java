package documentation.generator;

import java.time.Instant;
import java.util.function.Function;
import java.util.stream.Stream;

record Call(
        Span parent,
        String parentParticipant,
        Span child,
        String childParticipant,
        String callName,
        Option variant) {

    enum Option {
        INITIATING, RETURNING, SELF_CALL
    }

    static Call of(Span parent, Span child, Function<Span, String> participantName, Function<Span, String> callName) {
        String parentParticipant = parent != null ? participantName.apply(parent) : null;
        String childParticipant = participantName.apply(child);
        Option option = childParticipant.equals(parentParticipant) ? Option.SELF_CALL : Option.INITIATING;
        return new Call(
                parent,
                parentParticipant,
                child,
                childParticipant,
                callName.apply(child),
                option
        );
    }

    Instant start() {
        if (variant == Option.RETURNING) {
            return parent.end();
        }
        return child.start();
    }

    Instant end() {
        if (variant == Option.RETURNING) {
            return parent.start();
        }
        return child.end();
    }

    Stream<Call> includeReturning() {
        if (parent() == null || variant == Option.SELF_CALL) {
            return Stream.of(this);
        }
        return Stream.of(this, returning());
    }

    private Call returning() {
        return new Call(
                child, childParticipant,
                parent, parentParticipant,
                "return " + callName,
                Option.RETURNING
        );
    }

    @Override
    public String toString() {
        return "call:" +
               "\n from: " + parentParticipant +
               "\n to: " + childParticipant +
               "\n call: " + callName +
               "\n start: " + child.start() +
               "\n end: " + child.end();
    }
}
