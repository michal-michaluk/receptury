package devices.configuration.installations;

import devices.configuration.device.Ownership;

import javax.validation.constraints.NotNull;

record WorkOrder(@NotNull String orderId, @NotNull Ownership ownership) {
}
