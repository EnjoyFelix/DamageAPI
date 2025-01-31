package net.enjoyfelix.truedamageapi.services.weakness;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VanillaWeaknessProvider implements WeaknessProvider {

    @Override
    public double getAmplifier(Player player) {
        // get the active weakness effect
        final PotionEffect weaknessEffect = player.getActivePotionEffects()
                .stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.WEAKNESS))
                .findFirst()
                .orElse(null);

        // + 1 because minecraft starts at 0
        return weaknessEffect != null ? weaknessEffect.getAmplifier() + 1 : 0;
    }

    @Override
    public void setAmplier(final @NonNull Player player, double amplifier, int time, boolean force) {
        // give weakness to the player
        final PotionEffect effect = new PotionEffect(PotionEffectType.WEAKNESS, (int) amplifier, time);
        player.addPotionEffect(effect, force);
    }

    @Override
    public double getDamageReduction(Player player) {
        return getAmplifier(player) * 0.5;
    }
}
