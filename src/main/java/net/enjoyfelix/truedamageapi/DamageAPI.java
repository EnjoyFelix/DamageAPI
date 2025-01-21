package net.enjoyfelix.truedamageapi;

import net.enjoyfelix.truedamageapi.services.strength.StrengthProvider;
import net.enjoyfelix.truedamageapi.services.strength.VanillaStrengthProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class DamageAPI extends JavaPlugin {

    private static DamageAPI instance;
    private VanillaStrengthProvider vanillaStrengthProvider = new VanillaStrengthProvider();

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.broadcastMessage("Damage API By EnjoyFelix !");
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);

        // registers the default strength provider;
        Bukkit.getServicesManager().register(StrengthProvider.class, vanillaStrengthProvider, this, ServicePriority.High);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public StrengthProvider getStrengthProvider() {
        return (StrengthProvider) Bukkit.getServicesManager().getRegistration(StrengthProvider.class).getProvider();
    }

    public VanillaStrengthProvider getVanillaStrengthProvider() {
        return vanillaStrengthProvider;
    }

    public static DamageAPI getInstance() {
        return instance;
    }
}
