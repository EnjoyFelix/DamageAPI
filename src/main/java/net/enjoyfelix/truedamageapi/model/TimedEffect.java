package net.enjoyfelix.truedamageapi.model;

import lombok.Data;

@Data(staticConstructor = "from")
public class TimedEffect {
    private final long bestBy;
    private final double amplifier;
}
