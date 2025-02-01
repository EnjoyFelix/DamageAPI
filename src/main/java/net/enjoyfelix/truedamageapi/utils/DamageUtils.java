package net.enjoyfelix.truedamageapi.utils;

import net.enjoyfelix.truedamageapi.DamageAPI;
import net.enjoyfelix.truedamageapi.services.bonusdamage.BonusProvider;
import net.enjoyfelix.truedamageapi.services.bonusdamage.VanillaBonusProvider;
import net.enjoyfelix.truedamageapi.services.item.ItemDamageProvider;
import net.enjoyfelix.truedamageapi.services.item.VanillaItemDamageProvider;
import net.enjoyfelix.truedamageapi.services.resistance.ResistanceProvider;
import net.enjoyfelix.truedamageapi.services.strength.StrengthProvider;
import net.enjoyfelix.truedamageapi.services.weakness.WeaknessProvider;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.*;

public class DamageUtils {

    private static final Random random = new Random();
    private static final Map<Material, Integer> ARMOR_RESISTANCE_MAP = new HashMap<>();
    private static final Map<Material, Integer> ITEM_DAMAGE_MAP = new HashMap<>();

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


    // SOURCED FROM EntityHuman::d(DamageSource damagesource, float f) {
    public static void computeDamage(final EntityDamageByEntityEvent event) {
        final Player damager = (Player) event.getDamager();
        final Player damagee = (Player) event.getEntity();
        double originalDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);

        // compute the base damage;
        double damage = computeBaseDamage(damager, originalDamage);
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, damage);

        // blocking test
        // previously had a "ignoresArmor" check but i dont think it applies
        // > 1.0 while  it is 0 in the code because the +1 could actually add damage instead of reducing them
        if (damagee.isBlocking() && damage > 1.0F) {
            final double blockingDeductible = -(damage - 1) * 0.5;
            damage += blockingDeductible;
            event.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, blockingDeductible);
        }

        // update the damage based on the armor and magic
        // get the armor's ratio
        final double armorModifier = getArmorModifier(damagee);
        final double armorDeductible = -damage * (1 - armorModifier);
        damage += armorDeductible;
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, armorDeductible);


        // apply the resistance;
        final double resistancePercent = getResistanceScalar(damagee);
        final double resistanceDeductible = -damage * (1 - resistancePercent);
        damage += resistanceDeductible;
        event.setDamage(EntityDamageEvent.DamageModifier.RESISTANCE, resistanceDeductible);

        // apply the level of protection
        final double protectionPercent = getArmorEnchantmentsPercent(damagee);
        final double protectionDeductible = -damage * (1 - protectionPercent);
        damage += protectionDeductible;
        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, protectionDeductible);


        // remove the absorption
        double damageCopy = damage;
        final double currentAbsorptionHearts = getAbsorptionHearts(damagee);
        damage -= currentAbsorptionHearts;
        if (damage < 0)
            damage = 0;

        final double takenAbsorptionHearts = damage - damageCopy;
        event.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, takenAbsorptionHearts);
    }

    private static float getArmorModifier(final Player damagee) {
        return (25f - getArmorCoverForPlayer(damagee)) / 25f;
    }

    /**
     * Computes the total cover given by the player's armor (does not include the enchantments)
     * @param damagee the damaged player
     * @return the total cover
     */
    public static int getArmorCoverForPlayer(final Player damagee) {

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
            Integer materialCover = ARMOR_RESISTANCE_MAP.get(itemType);

            // the material is not an armor
            if (materialCover == null)
                continue;

            totalCover += materialCover;
        }

        return totalCover;
    }

    /**
     * Returns the percentage to multiply the damage by
     * /!\: The armor's reduction should already have been removed
     * @param damagee The damagee
     * @return 1 - ([resistance level + 1] / 5);
     */
    public static double getResistanceScalar(final Player damagee) {
        // get the resistance provider
        final DamageAPI damageAPI = DamageAPI.getInstance();
        final ResistanceProvider resistanceProvider = damageAPI.getResistanceProvider();

        // return the scalar of the player
        return resistanceProvider.getTotalScalar(damagee);
    }

    private static double getAbsorptionHearts(final Player damagee) {
        try {
            // get the "craftplayer"
            Object entityPlayer = damagee.getClass().getMethod("getHandle").invoke(damagee);

            // the "getAbsorptionHearts" method
            Method methodGetAbsorptionHeart = entityPlayer.getClass().getDeclaredMethod("getAbsorptionHearts");

            // invoke
            return (Double) methodGetAbsorptionHeart.invoke(entityPlayer);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static float getArmorEnchantmentsPercent(final Player damagee) {
        final ItemStack[] armorContent = damagee.getEquipment().getArmorContents();

        int protectionCount = getProtectionCount(armorContent);
        if (protectionCount > 20) {
            protectionCount = 20;
        }

        if (protectionCount <= 0)
            return 1.0f;


        return (25f - protectionCount) / 25f;
    }

    public static int getProtectionCount(final ItemStack[] armorContents) {

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

    private static int getDamageReductionForPiece(final ItemStack armorPiece) {
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


    /**
     * Computes the new Base Damages
     * @param player the player dealing the damages
     * @param originalDamage the original client side damages
     * @return The new base damages
     */
    private static double computeBaseDamage(final Player player, final double originalDamage) {
        // get the base damage dealt by the item
        final DamageAPI damageAPI = DamageAPI.getInstance();
        final ItemStack itemInHand = player.getItemInHand();

        // we need to know if the original damage was a crit or not
        // we can know using math and the vanilla providers
        final double vanillaBaseDamage = damageAPI.getVanillaItemDamageProvider().getDamage(itemInHand);
        // TODO: EntityType
        final double vanillaEnchantBonus = damageAPI.getVanillaBonusProvider().getBonus(itemInHand, EntityType.PLAYER);
        final double vanillaStrengthScalar = damageAPI.getVanillaStrengthProvider().getTotalScalar(player);
        final double vanillaWeaknessReduction = damageAPI.getVanillaWeaknessProvider().getDamageReduction(player);
        final double critScalar = isHitCritical(originalDamage, vanillaBaseDamage, vanillaStrengthScalar, vanillaWeaknessReduction, vanillaEnchantBonus) ? 1.5 : 1;


        // get the new strength scalar
        final StrengthProvider strengthProvider = damageAPI.getStrengthProvider();
        final double strengthScalar = strengthProvider.getTotalScalar(player);

        // get the new weakness reduction
        final WeaknessProvider weaknessProvider = damageAPI.getWeaknessProvider();
        final double weaknessReduction = weaknessProvider.getDamageReduction(player);

        // get the new base base
        final ItemDamageProvider itemDamageProvider = damageAPI.getItemDamageProvider();
        final double baseDamage = itemDamageProvider.getDamage(itemInHand);

        // get the new bonus
        final BonusProvider bonusProvider = damageAPI.getBonusProvider();
        final double enchantBonus = bonusProvider.getBonus(itemInHand, EntityType.PLAYER);

        // return the total damages
        return ((baseDamage - weaknessReduction) * strengthScalar * critScalar) + enchantBonus;
    }

    /**
     * Computes the final damages and checks if the hit was a critical
     * @param originalDamage the original "Base" damages
     * @param basedamage the attack damages of the item in hand
     * @param strengthScalar the vanilla strength scalar
     * @param weaknessReduction the vanilla weakness reduction
     * @param enchantBonus the vanilla enchant bonuses
     * @return true if the hit was critical
     */
    private static boolean isHitCritical(double originalDamage, double basedamage, double strengthScalar, double weaknessReduction, double enchantBonus){
        // float conversion issue so i have to do this
        return (int) ((originalDamage - enchantBonus) * 100) >= (int) ((basedamage - weaknessReduction) * strengthScalar * 150);
    }

    public static boolean isHitCritical(final EntityDamageByEntityEvent event) {
        final Entity _damager = event.getDamager();
        if (!(_damager instanceof Player))
            return false;

        // get the strength scalar
        final DamageAPI damageAPI = DamageAPI.getInstance();
        final Player player = (Player) _damager;
        final EntityType damagedEntityType = event.getEntityType();
        final double originalDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);

        // get the base damage dealt by the item
        final ItemStack itemInHand = player.getItemInHand();
        final double baseDamage;
        final double enchantementBonus;

        // if the player isn't holding anything
        if (itemInHand == null) {
            baseDamage = 1;
            enchantementBonus = 0;
        }

        // the player is actually holding something
        else {
            final VanillaItemDamageProvider vanillaItemProvider = damageAPI.getVanillaItemDamageProvider();
            baseDamage = vanillaItemProvider.getDamage(itemInHand);

            // compute the bonus given by enchantments from the item and the entity type
            final VanillaBonusProvider vanillaBonusProvider = damageAPI.getVanillaBonusProvider();
            enchantementBonus = vanillaBonusProvider.getBonus(itemInHand, damagedEntityType);
        }

        final StrengthProvider strengthProvider  = damageAPI.getVanillaStrengthProvider();
        final double strengthScalar = strengthProvider.getTotalScalar(player);

        // get the weakness reduction
        final WeaknessProvider weaknessProvider = damageAPI.getVanillaWeaknessProvider();
        final double weaknessReduction = weaknessProvider.getDamageReduction(player);

        return isHitCritical(originalDamage, baseDamage, strengthScalar, weaknessReduction, enchantementBonus);
    }

/*    public int functionA(int level, final Enchantment enchantment) {
        float var3 = (float) (6 + level * level) / 3.0F;

        if (enchantment == Enchantment.PROTECTION_ENVIRONMENTAL) {
            return MathHelper.d(var3 * 0.75F);
        }

        // Fire protection, not relevant
        else if (enchantment == Enchantment.PROTECTION_FIRE && var2.o()) {
            return MathHelper.d(var3 * 1.25F);
        }

        // Feather falling, not relevant
        else if (enchantment == Enchantment.PROTECTION_FALL && var2 == DamageSource.FALL) {
            return MathHelper.d(var3 * 2.5F);
        }

        // explosion damages, not relevant here
        else if (enchantment == Enchantment.PROTECTION_EXPLOSIONS && var2.isExplosion()) {
            return MathHelper.d(var3 * 1.5F);
        }

        // projectile projection, not relevant
        else {
            return this.a == 4 && var2.a() ? MathHelper.d(var3 * 1.5F) : 0;
        }

        return -1;
    }*/
}
