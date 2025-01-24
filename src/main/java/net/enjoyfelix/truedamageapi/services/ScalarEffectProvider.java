package net.enjoyfelix.truedamageapi.services;

import org.bukkit.entity.Player;

public interface ScalarEffectProvider extends EffectProvider{
    double getTotalScalar(final Player player);

    /**
     * @return The scalar of the effect at level 0
     */
    double getBaseScalar();

    /**
     * @param player The player affected by the effect
     * @return the boost given to the player in percent
     */
    default double getTotalAsPercent(final Player player) {
        return (getAmplifier(player) * getBaseScalar()) * 100;
    }
}
