package net.enjoyfelix.truedamageapi.services.strength;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class VanillaStrengthProvider implements StrengthProvider{
    @Override
    public double getBaseScalar() {
        return (float) 1.3;
    }

    @Override
    public double getAmplifier(final Player player) {
        // get the active strength effect
        final PotionEffect strengthEffect = player.getActivePotionEffects()
                .stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.INCREASE_DAMAGE))
                .findFirst()
                .orElse(null);

        // + 1 because minecraft starts at 0
        return strengthEffect != null ? strengthEffect.getAmplifier() + 1 : 0;
    }

    @Override
    public void setAmplier(final @NonNull Player player, double amplifier, int time, boolean force) {
        // give Strength to the player
        final PotionEffect effect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) amplifier, time);
        player.addPotionEffect(effect, force);
    }

    @Override
    public double getTotalScalar(Player player) {
        return getAmplifier(player) * getBaseScalar() + 1 ;
    }
}
