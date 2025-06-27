package net.enjoyfelix.truedamageapi;

import lombok.Getter;
import net.enjoyfelix.truedamageapi.listeners.DamageListener;
import net.enjoyfelix.truedamageapi.services.armor.ArmorProtectionProvider;
import net.enjoyfelix.truedamageapi.services.armor.VanillaArmorProtectionProvider;
import net.enjoyfelix.truedamageapi.services.bonusdamage.BonusProvider;
import net.enjoyfelix.truedamageapi.services.bonusdamage.VanillaBonusProvider;
import net.enjoyfelix.truedamageapi.services.item.ItemDamageProvider;
import net.enjoyfelix.truedamageapi.services.item.VanillaItemDamageProvider;
import net.enjoyfelix.truedamageapi.services.resistance.ResistanceProvider;
import net.enjoyfelix.truedamageapi.services.resistance.VanillaResistanceProvider;
import net.enjoyfelix.truedamageapi.services.strength.StrengthProvider;
import net.enjoyfelix.truedamageapi.services.strength.VanillaStrengthProvider;
import net.enjoyfelix.truedamageapi.services.weakness.VanillaWeaknessProvider;
import net.enjoyfelix.truedamageapi.services.weakness.WeaknessProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

public final class DamageAPI {
    private static DamageAPI instance;

    // These providers are kept here because they are used in the client side computations
    @Getter
    private final VanillaStrengthProvider vanillaStrengthProvider = new VanillaStrengthProvider();
    @Getter
    private final VanillaWeaknessProvider vanillaWeaknessProvider = new VanillaWeaknessProvider();
    @Getter
    private final VanillaItemDamageProvider vanillaItemDamageProvider = new VanillaItemDamageProvider();
    @Getter
    private final VanillaBonusProvider vanillaBonusProvider = new VanillaBonusProvider();

    private boolean registered = false;

    private DamageAPI() {}

    public static DamageAPI getInstance() {
        if (instance == null) {
            instance = new DamageAPI();
        }

        return instance;
    }

    public void register(final Plugin plugin){
        if (registered)
            return;
        // register the listener
        Bukkit.getPluginManager().registerEvents(new DamageListener(), plugin);

        // register the default providers;
        Bukkit.getServicesManager().register(StrengthProvider.class, vanillaStrengthProvider, plugin, ServicePriority.Normal);
        Bukkit.getServicesManager().register(WeaknessProvider.class, vanillaWeaknessProvider, plugin, ServicePriority.Normal);
        Bukkit.getServicesManager().register(ResistanceProvider.class, new VanillaResistanceProvider(), plugin, ServicePriority.Normal);
        Bukkit.getServicesManager().register(ItemDamageProvider.class, vanillaItemDamageProvider, plugin, ServicePriority.Normal);
        Bukkit.getServicesManager().register(BonusProvider.class, vanillaBonusProvider, plugin, ServicePriority.Normal);
        Bukkit.getServicesManager().register(ArmorProtectionProvider.class, new VanillaArmorProtectionProvider(), plugin, ServicePriority.Normal);
        this.registered = true;
    }

    public StrengthProvider getStrengthProvider() {
        return Bukkit.getServicesManager().getRegistration(StrengthProvider.class).getProvider();
    }

    public ResistanceProvider getResistanceProvider() {
        return Bukkit.getServicesManager().getRegistration(ResistanceProvider.class).getProvider();
    }

    public WeaknessProvider getWeaknessProvider() {
        return Bukkit.getServicesManager().getRegistration(WeaknessProvider.class).getProvider();
    }

    public ItemDamageProvider getItemDamageProvider(){
        return Bukkit.getServicesManager().getRegistration(ItemDamageProvider.class).getProvider();
    }

    public BonusProvider getBonusProvider(){
        return Bukkit.getServicesManager().getRegistration(BonusProvider.class).getProvider();
    }

    public ArmorProtectionProvider getArmorProtectionProvider(){
        return Bukkit.getServicesManager().getRegistration(ArmorProtectionProvider.class).getProvider();
    }

}
