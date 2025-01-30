package net.enjoyfelix.truedamageapi.services.item;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public interface ItemDamageProvider {
    double getDamage(final ItemStack itemstack);
}
