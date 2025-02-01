package net.enjoyfelix.truedamageapi.services.bonusdamage;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface BonusProvider {

    double getBonus(final ItemStack itemInHand, final EntityType entityType);
}
