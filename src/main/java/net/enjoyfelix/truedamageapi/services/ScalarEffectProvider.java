package net.enjoyfelix.truedamageapi.services;

import org.bukkit.entity.Player;

public interface ScalarEffectProvider extends EffectProvider{
    double getTotalScalar(final Player player);
    double getBaseScalar();
    default  double getTotalPercentage(final Player player) {
        return (getAmplifier(player) * getBaseScalar()) * 100;
    }
}
