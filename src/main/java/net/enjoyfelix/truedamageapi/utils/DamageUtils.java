package net.enjoyfelix.truedamageapi.utils;

import net.enjoyfelix.truedamageapi.DamageAPI;
import net.enjoyfelix.truedamageapi.services.armor.ArmorProtectionProvider;
import net.enjoyfelix.truedamageapi.services.bonusdamage.BonusProvider;
import net.enjoyfelix.truedamageapi.services.bonusdamage.VanillaBonusProvider;
import net.enjoyfelix.truedamageapi.services.item.ItemDamageProvider;
import net.enjoyfelix.truedamageapi.services.item.VanillaItemDamageProvider;
import net.enjoyfelix.truedamageapi.services.resistance.ResistanceProvider;
import net.enjoyfelix.truedamageapi.services.strength.StrengthProvider;
import net.enjoyfelix.truedamageapi.services.weakness.WeaknessProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

public class DamageUtils {

    // MAIN FUNCTION
    // SOURCED FROM EntityHuman::d(DamageSource damagesource, float f)
    public static void computeDamage(final EntityDamageByEntityEvent event) {
        final Player damager = (Player) event.getDamager();
        final Player damagee = (Player) event.getEntity();
        final DamageAPI damageAPI = DamageAPI.getInstance();
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
        final ArmorProtectionProvider armorProvider = damageAPI.getArmorProtectionProvider();
        final double armorModifier = armorProvider.getArmorModifier(damagee);
        final double armorDeductible = -damage * (1 - armorModifier);
        damage += armorDeductible;
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, armorDeductible);


        // apply the resistance;
        final ResistanceProvider resistanceProvider = damageAPI.getResistanceProvider();
        final double resistancePercent = resistanceProvider.getTotalScalar(damagee);
        final double resistanceDeductible = -damage * (1 - resistancePercent);
        damage += resistanceDeductible;
        event.setDamage(EntityDamageEvent.DamageModifier.RESISTANCE, resistanceDeductible);

        // apply the level of protection

        final double protectionPercent = armorProvider.getArmorEnchantmentsPercent(damagee);
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

    /**
     * Queries the number of absorption hearts of the player
     * @param damagee the player receiving the damages
     * @return the number of 1/2 absorption hearts
     */
    private static double getAbsorptionHearts(final Player damagee) {
        try {
            // get the "craftplayer"
            Object entityPlayer = damagee.getClass().getMethod("getHandle").invoke(damagee);

            // the "getAbsorptionHearts" method
            Method methodGetAbsorptionHeart = entityPlayer.getClass().getSuperclass().getDeclaredMethod("getAbsorptionHearts");

            // invoke
            return (Double) methodGetAbsorptionHeart.invoke(entityPlayer);
        } catch (Exception e) {
            return 0.0;
        }
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
