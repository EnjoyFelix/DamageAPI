package net.enjoyfelix.truedamageapi;

import com.avaje.ebean.validation.NotNull;
import net.enjoyfelix.truedamageapi.services.strength.StrengthProvider;
import net.minecraft.server.v1_8_R3.BlockPressurePlateBinary;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class DamageUtils {

    private static final Random random = new Random();
    private static final Map<Material, Integer> ARMOR_RESISTANCE_MAP = new HashMap<Material, Integer>();
    private static final Map<Material, Integer> ITEM_DAMAGE_MAP = new HashMap<>();
    private static final Set<EntityType> UNDEAD_MOBS = new HashSet<>();

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

        // Undead mobs
        UNDEAD_MOBS.add(EntityType.ZOMBIE);
        UNDEAD_MOBS.add(EntityType.SKELETON);
        UNDEAD_MOBS.add(EntityType.WITHER);
        UNDEAD_MOBS.add(EntityType.PIG_ZOMBIE);
    }


    // SOURCED FROM EntityHuman::d(DamageSource damagesource, float f) {
    public static Map<EntityDamageEvent.DamageModifier, Double> computeDamage(final Player damager, final Player damagee, double originalDamage) {
        // the result
        final Map<EntityDamageEvent.DamageModifier, Double> resMap = new HashMap<>();

        // compute the base damage;
        double damage = 1.0d;
        Bukkit.broadcastMessage("Computed base:" + computeBaseDamage(damager, originalDamage));


        // blocking test
        // previously had a "ignorsArmor" check but i dont think it applies
        if (damagee.isBlocking() && damage > 0.0F) {
            damage = (1.0F + damage) * 0.5F;
            resMap.put(EntityDamageEvent.DamageModifier.BLOCKING, 0.5);
        }

        // update the damage based on the armor and magic
        // get the armor's ratio
        final double armorModifier = getArmorModifier(damagee);
        damage *= armorModifier;
        resMap.put(EntityDamageEvent.DamageModifier.ARMOR, armorModifier);


        // apply the resistance;
        final double resistancePercent = getResistanceScalar(damagee);
        damage *= resistancePercent;
        resMap.put(EntityDamageEvent.DamageModifier.RESISTANCE, resistancePercent);

        // apply the level of protection
        final double protectionPercent = getArmorEnchantmentsPercent(damagee);
        damage *= protectionPercent;
        resMap.put(EntityDamageEvent.DamageModifier.MAGIC, protectionPercent);


        // TODO: needs fixing, wrong value
        // remove the absorption
        double damageCopy = damage;
        final double currentAbsorptionHearts = ((CraftPlayer) damagee).getHandle().getAbsorptionHearts();
        damage -= currentAbsorptionHearts;
        if (damage > 0)
            damage = 0;

        final double takenAbsorptionHearts = damageCopy - damage;
        resMap.put(EntityDamageEvent.DamageModifier.ABSORPTION, takenAbsorptionHearts);

        return resMap;
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
        for (int k = 0; k < armor.length; ++k) {
            // filter for non null items
            final ItemStack currentPiece = armor[k];
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
    public static float getResistanceScalar(final Player damagee) {
        // filter resistance from the active effects
        final Optional<PotionEffect> resistance = damagee.getActivePotionEffects().stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE))
                .peek(System.out::println)
                .findFirst();

        // does the player have resistance ?
        if (!resistance.isPresent())
            return 1F;

        // calculate the  resistance from the level
        // +1 to account for the ingame difference
        final int resistanceLevel = (resistance.get().getAmplifier() + 1);

        // compute the scalar, cap at 0
        return Math.max(1 - (resistanceLevel / 5.0F), 0f);
    }

    /**
     * @param damagee
     * @return
     */
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


    private static double computeBaseDamage(final Player player, final double originalDamage) {
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
            baseDamage = getBaseDamageForItem(itemInHand);

            // compute the bonus given by enchantments from the item and the entity type
            final Map<Enchantment, Integer> activeEnchantments = itemInHand.getEnchantments();
            // TODO: specify entity types
            enchantementBonus = getGlintDamageBonus(activeEnchantments, EntityType.PLAYER);
        }

        // get the strength scalar
        final DamageAPI damageAPI = DamageAPI.getInstance();
        final StrengthProvider strengthProvider = damageAPI.getStrengthProvider();
        final double strengthScalar = strengthProvider.getTotalScalar(player);


        // we need to know if the original damage was a crit or not
        // we know using math and the default strengthProvider
        final double vanillaStrengthScalar = damageAPI.getVanillaStrengthProvider().getTotalScalar(player);
        final double critScalar = isHitCritical(originalDamage, baseDamage, vanillaStrengthScalar, enchantementBonus) ? 1.5 : 1;
        if (critScalar == 1.5){
            Bukkit.broadcastMessage("§4CRITICAL");
        }


        // return the total damages
        return (baseDamage * strengthScalar * critScalar) + enchantementBonus;
    }

    /* Calcul des dégats
    *   damage = dégats de l'item + 1
    *   critScalar = 1.5
    *   sharpness bonus = 1.25 * level
    *   strength scalar = (damage * (level + 1) * 1.3)
    *
    *   Calcul : (damage * strengthscalar + 1 ) * critScalar + sharpness
    * */


    private static int getBaseDamageForItem(final ItemStack is){
        // get the base damage of the item from the material
        final Material material = is.getType();
        return ITEM_DAMAGE_MAP.getOrDefault(material, 0) + 1 ;
    }

    private static double getGlintDamageBonus(final Map<Enchantment, Integer> activeEnchants, final EntityType mobType){
        double enchantBonus = 0;

        // early termination if the item is not enchanted
        if (activeEnchants == null || activeEnchants.isEmpty())
            return 0;

        // Sharpness, smite and BOA usually cannot be on a weapon at the same time, but idc this is not vanilla mc
        // sharpness applies to every entity type
        enchantBonus += activeEnchants.getOrDefault(Enchantment.DAMAGE_ALL, 0) * 1.25;

        // smite
        if (UNDEAD_MOBS.contains(mobType)){
            enchantBonus += activeEnchants.getOrDefault(Enchantment.DAMAGE_UNDEAD, 0) * 2.5;
        }

        // bane of arthropods
        if (mobType == EntityType.SPIDER || mobType == EntityType.CAVE_SPIDER) {
            enchantBonus += activeEnchants.getOrDefault(Enchantment.DAMAGE_ARTHROPODS, 0) * 2.5;
        }

        return enchantBonus;
    }

    private static boolean isHitCritical(double originalDamage, double basedamage, double strengthScalar, double enchantBonus){
        return (originalDamage - enchantBonus) / (basedamage * strengthScalar) == 1.5;
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
