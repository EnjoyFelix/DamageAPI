package net.enjoyfelix.truedamageapi.services.item;
import org.bukkit.inventory.ItemStack;

public interface ItemDamageProvider {
    double getDamage(final ItemStack itemstack);
}
