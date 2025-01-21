package net.enjoyfelix.truedamageapi.services;

import org.bukkit.entity.Player;

public interface EffectProvider {

    double getBaseScalar();
    double getAmplifier(final Player player);
    double getTotalPercentage(final Player player);
    double getTotalScalar(final Player player);
}
