package net.enjoyfelix.truedamageapi.services.weakness;

import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.MobEffectList;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;

public class VanillaWeaknessProvider implements WeaknessProvider {

    @Override
    public double getAmplifier(Player player) {
        // get the active weakness effect
        final MobEffect strengthEffect = ((CraftPlayer) player).getHandle().getEffect(MobEffectList.WEAKNESS);

        // + 1 because minecraft starts at 0
        return strengthEffect != null ? strengthEffect.getAmplifier() + 1 : 0;
    }

    @Override
    public void setAmplier(@Nonnull final Player player, double amplifier, int time, boolean force) {
        // give weakness to the player
        final PotionEffect effect = new PotionEffect(PotionEffectType.WEAKNESS, (int) amplifier, time);
        player.addPotionEffect(effect, force);
    }

    @Override
    public double getDamageReduction(Player player) {
        return getAmplifier(player) * 0.5;
    }
}
