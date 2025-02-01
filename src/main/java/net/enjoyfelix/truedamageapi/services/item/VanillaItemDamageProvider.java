package net.enjoyfelix.truedamageapi.services.item;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class VanillaItemDamageProvider implements ItemDamageProvider {
    public static final Map<Material, Integer> ITEM_DAMAGE_MAP = new HashMap<>();
    static {
        // wooden tools
        ITEM_DAMAGE_MAP.put(Material.WOOD_SPADE, 1);
        ITEM_DAMAGE_MAP.put(Material.WOOD_PICKAXE, 2);
        ITEM_DAMAGE_MAP.put(Material.WOOD_AXE, 3);
        ITEM_DAMAGE_MAP.put(Material.WOOD_SWORD, 4);

        // golden tools
        ITEM_DAMAGE_MAP.put(Material.GOLD_SPADE, 1);
        ITEM_DAMAGE_MAP.put(Material.GOLD_PICKAXE, 2);
        ITEM_DAMAGE_MAP.put(Material.GOLD_AXE, 3);
        ITEM_DAMAGE_MAP.put(Material.GOLD_SWORD, 4);

        // stone tools
        ITEM_DAMAGE_MAP.put(Material.STONE_SPADE, 2);
        ITEM_DAMAGE_MAP.put(Material.STONE_PICKAXE, 3);
        ITEM_DAMAGE_MAP.put(Material.STONE_AXE, 4);
        ITEM_DAMAGE_MAP.put(Material.STONE_SWORD, 5);

        // iron tools
        ITEM_DAMAGE_MAP.put(Material.IRON_SPADE, 3);
        ITEM_DAMAGE_MAP.put(Material.IRON_PICKAXE, 4);
        ITEM_DAMAGE_MAP.put(Material.IRON_AXE, 5);
        ITEM_DAMAGE_MAP.put(Material.IRON_SWORD, 6);

        // diamond tools
        ITEM_DAMAGE_MAP.put(Material.DIAMOND_SPADE, 4);
        ITEM_DAMAGE_MAP.put(Material.DIAMOND_PICKAXE, 5);
        ITEM_DAMAGE_MAP.put(Material.DIAMOND_AXE, 6);
        ITEM_DAMAGE_MAP.put(Material.DIAMOND_SWORD, 7);
    }

    @Override
    public double getDamage(final ItemStack itemstack) {
        double res = 1;
        if (itemstack != null){
            final Material material = itemstack.getType();
            res = ITEM_DAMAGE_MAP.getOrDefault(material, 0) + 1 ;
        }
        return res;
    }
}
