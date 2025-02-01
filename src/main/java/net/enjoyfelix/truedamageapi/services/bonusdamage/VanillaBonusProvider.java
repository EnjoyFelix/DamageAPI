package net.enjoyfelix.truedamageapi.services.bonusdamage;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VanillaBonusProvider implements BonusProvider {

    private static final Set<EntityType> UNDEAD_MOBS = new HashSet<>();
    static {
        // Undead mobs
        UNDEAD_MOBS.add(EntityType.ZOMBIE);
        UNDEAD_MOBS.add(EntityType.SKELETON);
        UNDEAD_MOBS.add(EntityType.WITHER);
        UNDEAD_MOBS.add(EntityType.PIG_ZOMBIE);
    }

    @Override
    public double getBonus(final ItemStack itemInHand, EntityType entityType) {
        double enchantBonus = 0;

        // early termination if the item is null
        if (itemInHand == null)
            return enchantBonus;

        final Map<Enchantment, Integer> activeEnchants = itemInHand.getEnchantments();

        // early termination if the item is not enchanted
        if (activeEnchants == null || activeEnchants.isEmpty())
            return 0;

        // Sharpness, smite and BOA usually cannot be on a weapon at the same time, but idc this is not vanilla mc
        // sharpness applies to every entity type
        enchantBonus += activeEnchants.getOrDefault(Enchantment.DAMAGE_ALL, 0) * 1.25;

        // smite
        if (UNDEAD_MOBS.contains(entityType)){
            enchantBonus += activeEnchants.getOrDefault(Enchantment.DAMAGE_UNDEAD, 0) * 2.5;
        }

        // bane of arthropods
        if (entityType == EntityType.SPIDER || entityType == EntityType.CAVE_SPIDER) {
            enchantBonus += activeEnchants.getOrDefault(Enchantment.DAMAGE_ARTHROPODS, 0) * 2.5;
        }

        return enchantBonus;
    }
}
