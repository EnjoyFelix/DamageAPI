package net.enjoyfelix.truedamageapi.services.resistance;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VanillaResistanceProvider implements ResistanceProvider {
    @Override
    public double getBaseScalar() {
        return 0.2;
    }

    @Override
    public double getAmplifier(final Player player) {
        // get the active resistance effect
        final PotionEffect resistanceEffect = player.getActivePotionEffects()
                .stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE))
                .findFirst()
                .orElse(null);

        // + 1 because minecraft starts at 0
        return resistanceEffect != null ? resistanceEffect.getAmplifier() + 1 : 0;
    }

    @Override
    public void setAmplier(final @NonNull Player player, double amplifier, int time, boolean force) {
        // give resistance to the player
        final PotionEffect effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) amplifier, time);
        player.addPotionEffect(effect, force);
    }

    @Override
    public double getTotalScalar(Player player) {
        return Math.max(1 - (getAmplifier(player) * getBaseScalar()), 0) ;
    }
}
