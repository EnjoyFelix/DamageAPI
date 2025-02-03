package net.enjoyfelix.truedamageapi.services.armor;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ArmorProtectionProvider {

    double getArmorModifier(final @NonNull Player damagee);

    double getArmorEnchantmentsPercent(final @NonNull Player damagee);
}
