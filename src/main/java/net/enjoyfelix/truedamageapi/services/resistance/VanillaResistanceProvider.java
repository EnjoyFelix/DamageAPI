package net.enjoyfelix.truedamageapi.services.resistance;

import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.MobEffectList;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class VanillaResistanceProvider implements ResistanceProvider {
    @Override
    public double getBaseScalar() {
        return 0.2;
    }

    @Override
    public double getAmplifier(final Player player) {
        // get the active resistance effect
        final MobEffect strengthEffect = ((CraftPlayer) player).getHandle().getEffect(MobEffectList.RESISTANCE);

        // + 1 because minecraft starts at 0
        return strengthEffect != null ? strengthEffect.getAmplifier() + 1 : 0;
    }

    @Override
    public double getTotalScalar(Player player) {
        return Math.max(1 - (getAmplifier(player) * getBaseScalar()), 0) ;
    }
}
