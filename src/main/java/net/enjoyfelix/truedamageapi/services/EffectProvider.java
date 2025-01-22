package net.enjoyfelix.truedamageapi.services;

import org.bukkit.entity.Player;

public interface EffectProvider {

    double getBaseScalar();
    double getAmplifier(final Player player);
    default  double getTotalPercentage(final Player player) {
        return (getAmplifier(player) * getBaseScalar()) * 100;
    }
    double getTotalScalar(final Player player);
}
