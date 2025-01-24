package net.enjoyfelix.truedamageapi.services;

import lombok.NonNull;
import org.bukkit.entity.Player;

import javax.annotation.Nonnegative;

public interface EffectProvider {

    double getAmplifier(final Player player);

    /**
     *
     * @param player The player affected by the effect
     * @param amplifier The level of the effect. /!\ Levels start at 0 !
     * @param time How long (in ticks) the player should have the effect
     * @param force Ovveride the previous effect
     */
    void setAmplier(final @NonNull Player player, final double amplifier, @Nonnegative final int time, final boolean force);
}
