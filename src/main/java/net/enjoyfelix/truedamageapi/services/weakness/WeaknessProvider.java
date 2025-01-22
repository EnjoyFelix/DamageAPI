package net.enjoyfelix.truedamageapi.services.weakness;

import net.enjoyfelix.truedamageapi.services.EffectProvider;
import org.bukkit.entity.Player;

public interface WeaknessProvider extends EffectProvider {

    double getDamageReduction(Player player);
}
