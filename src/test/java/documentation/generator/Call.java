package documentation.generator;

import java.time.Instant;
import java.util.stream.Stream;

record Call(
        Span parent,
        String parentParticipant,
        Span child,
        String childParticipant,
        String callName,
        CallVariant variant) {

    enum CallVariant {
        INITIATING, RETURNING, SELF_CALL
    }

    static Call of(Span parent, Span child, PerspectiveParameters parameters) {
        String parentParticipant = parent != null ? parameters.participantName(parent) : null;
        String childParticipant = parameters.participantName(child);
        CallVariant callVariant = childParticipant.equals(parentParticipant) ? CallVariant.SELF_CALL : CallVariant.INITIATING;
        return new Call(
                parent,
                parentParticipant,
                child,
                childParticipant,
                parameters.callName(child),
                callVariant
        );
    }

    Instant start() {
        if (variant == CallVariant.RETURNING) {
            return parent.end();
        }
        return child.start();
    }

    Instant end() {
        if (variant == CallVariant.RETURNING) {
            return parent.start();
        }
        return child.end();
    }

    Stream<Call> replaceParentParticipantName(String newName) {
        return Stream.of(new Call(parent, newName, child, childParticipant, callName, variant));
    }

    Stream<Call> includeReturning() {
        if (parent() == null || variant == CallVariant.SELF_CALL) {
            return Stream.of(this);
        }
        return Stream.of(this, returning());
    }

    private Call returning() {
        return new Call(
                child, childParticipant,
                parent, parentParticipant,
                "return " + callName,
                CallVariant.RETURNING
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
