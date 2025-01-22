package net.enjoyfelix.truedamageapi;

import net.enjoyfelix.truedamageapi.services.resistance.ResistanceProvider;
import net.enjoyfelix.truedamageapi.services.resistance.VanillaResistanceProvider;
import net.enjoyfelix.truedamageapi.services.strength.StrengthProvider;
import net.enjoyfelix.truedamageapi.services.strength.VanillaStrengthProvider;
import net.enjoyfelix.truedamageapi.services.weakness.VanillaWeaknessProvider;
import net.enjoyfelix.truedamageapi.services.weakness.WeaknessProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class DamageAPI extends JavaPlugin {

    private static DamageAPI instance;
    private final VanillaStrengthProvider vanillaStrengthProvider = new VanillaStrengthProvider();
    private final VanillaWeaknessProvider vanillaWeaknessProvider = new VanillaWeaknessProvider();

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.broadcastMessage("Damage API By EnjoyFelix !");
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);

        // registers the default providers;
        Bukkit.getServicesManager().register(StrengthProvider.class, vanillaStrengthProvider, this, ServicePriority.Normal);
        Bukkit.getServicesManager().register(WeaknessProvider.class, vanillaWeaknessProvider, this, ServicePriority.Normal);
        Bukkit.getServicesManager().register(ResistanceProvider.class, new VanillaResistanceProvider(), this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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

    public VanillaStrengthProvider getVanillaStrengthProvider() {
        return vanillaStrengthProvider;
    }

    public VanillaWeaknessProvider getVanillaWeaknessProvider() {
        return vanillaWeaknessProvider;
    }

    public static DamageAPI getInstance() {
        return instance;
    }
}
