package net.enjoyfelix.truedamageapi.services.strength;

import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.MobEffectList;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class VanillaStrengthProvider implements StrengthProvider{
    @Override
    public double getBaseScalar() {
        return (float) 1.3;
    }

    @Override
    public double getAmplifier(final Player player) {
        // get the active strength effect
        final MobEffect strengthEffect = ((CraftPlayer) player).getHandle().getEffect(MobEffectList.INCREASE_DAMAGE);

        // + 1 because minecraft starts at 0
        return strengthEffect != null ? strengthEffect.getAmplifier() + 1 : 0;
    }

    @Override
    public void setAmplier(@Nonnull final Player player, double amplifier, int time, boolean force) {
        // give Strength to the player
        final PotionEffect effect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) amplifier, time);
        player.addPotionEffect(effect, force);
    }

    @Override
    public double getTotalScalar(Player player) {
        return getAmplifier(player) * getBaseScalar() + 1 ;
    }
}
