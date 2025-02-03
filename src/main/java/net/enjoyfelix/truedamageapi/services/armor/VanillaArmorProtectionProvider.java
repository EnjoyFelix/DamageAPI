package net.enjoyfelix.truedamageapi.services.armor;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VanillaArmorProtectionProvider implements ArmorProtectionProvider {
    private static final Random random = new Random(System.currentTimeMillis());
    private static final Map<Material, Integer> ARMOR_RESISTANCE_MAP = new HashMap<>();
    static {
        // leather armor
        ARMOR_RESISTANCE_MAP.put(Material.LEATHER_HELMET, 1);
        ARMOR_RESISTANCE_MAP.put(Material.LEATHER_CHESTPLATE, 3);
        ARMOR_RESISTANCE_MAP.put(Material.LEATHER_LEGGINGS, 2);
        ARMOR_RESISTANCE_MAP.put(Material.LEATHER_BOOTS, 1);

        // chainmail armor
        ARMOR_RESISTANCE_MAP.put(Material.CHAINMAIL_HELMET, 2);
        ARMOR_RESISTANCE_MAP.put(Material.CHAINMAIL_CHESTPLATE, 5);
        ARMOR_RESISTANCE_MAP.put(Material.CHAINMAIL_LEGGINGS, 4);
        ARMOR_RESISTANCE_MAP.put(Material.CHAINMAIL_BOOTS, 1);

        // iron armor
        ARMOR_RESISTANCE_MAP.put(Material.IRON_HELMET, 2);
        ARMOR_RESISTANCE_MAP.put(Material.IRON_CHESTPLATE, 6);
        ARMOR_RESISTANCE_MAP.put(Material.IRON_LEGGINGS, 5);
        ARMOR_RESISTANCE_MAP.put(Material.IRON_BOOTS, 2);

        // gold armor
        ARMOR_RESISTANCE_MAP.put(Material.GOLD_HELMET, 2);
        ARMOR_RESISTANCE_MAP.put(Material.GOLD_CHESTPLATE, 5);
        ARMOR_RESISTANCE_MAP.put(Material.GOLD_LEGGINGS, 3);
        ARMOR_RESISTANCE_MAP.put(Material.GOLD_BOOTS, 1);

        // diamond amor
        ARMOR_RESISTANCE_MAP.put(Material.DIAMOND_HELMET, 3);
        ARMOR_RESISTANCE_MAP.put(Material.DIAMOND_CHESTPLATE, 8);
        ARMOR_RESISTANCE_MAP.put(Material.DIAMOND_LEGGINGS, 6);
        ARMOR_RESISTANCE_MAP.put(Material.DIAMOND_BOOTS, 3);
    }

    /**
     * Computes the total cover given by the player's armor (does not include the enchantments)
     * @param damagee the damaged player
     * @return the total cover
     */
    private int getArmorCover(final @NonNull Player damagee) {
        int totalCover = 0;
        final ItemStack[] armor = damagee.getEquipment().getArmorContents();

        // sum every piece's cover
        for (final ItemStack currentPiece : armor) {
            // filter for non null items
            if (currentPiece == null)
                continue;

            // material of the piece;
            final Material itemType = currentPiece.getType();

            // get the armor piece's absorption (ex: a diamond chestplate gets you 8);
            // 0 if the material is not an armor piece
            int materialCover = ARMOR_RESISTANCE_MAP.getOrDefault(itemType, 0);

            totalCover += materialCover;
        }

        return Math.min(totalCover, 25);
    }

    @Override
    public double getArmorModifier(final @NonNull Player damagee) {
        return (25f - getArmorCover(damagee)) / 25f;
    }

    /**
     * Returns the Protection level count (out of 25)
     * @param armorContents the armor slots
     * @return the sum of all the level of the protection enchants
     */
    private int getProtectionCount(final @NonNull ItemStack[] armorContents) {
        // idk but this seems like the total number of protections (weirdaf)
        int modifierProtectionCount = 0;
        for (int i = 0; i < armorContents.length; ++i) {
            final ItemStack armorPiece = armorContents[i];
            modifierProtectionCount += getDamageReductionForPiece(armorPiece);
        }

        // cap the count
        if (modifierProtectionCount > 25) {
            modifierProtectionCount = 25;

        } else if (modifierProtectionCount < 0) {
            modifierProtectionCount = 0;
        }

        return (modifierProtectionCount + 1 >> 1) + random.nextInt((modifierProtectionCount >> 1) + 1);
    }

    /**
     * Queries the level of the protection enchant of the piece
     * @param armorPiece the armor piece
     * @return The level of the protection enchantement
     */
    private int getDamageReductionForPiece(final @NonNull ItemStack armorPiece) {
        if (armorPiece == null)
            return 0;

        // get the enchant map
        final Map<Enchantment, Integer> enchantMap = armorPiece.getEnchantments();
        if (enchantMap == null)
            return 0;

        // get the level of protection
        final Integer protectionLevel = enchantMap.get(Enchantment.PROTECTION_ENVIRONMENTAL);
        if (protectionLevel == null)
            return 0;

        // get the damageReductionFrom given by the protection level of the piece
        float value = (float) (6 + protectionLevel * protectionLevel) / 3.0F;

        // Math.d()
        int valueInt = (int) (value * 0.75F);
        return value < (float) valueInt ? valueInt - 1 : valueInt;
    }

    @Override
    public double getArmorEnchantmentsPercent(final @NonNull Player damagee) {
        final ItemStack[] armorContent = damagee.getEquipment().getArmorContents();

        int protectionCount = getProtectionCount(armorContent);
        if (protectionCount > 20) {
            protectionCount = 20;
        }

        if (protectionCount <= 0)
            return 1.0f;


        return (25f - protectionCount) / 25f;
    }


}
