package net.enjoyfelix.truedamageapi.listeners;
import net.enjoyfelix.truedamageapi.utils.DamageUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    /**
     * Recalculates the damage if the daamager is a player
     * @param event the event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagePlayer(final EntityDamageByEntityEvent event) {
        final Entity _damager = event.getDamager();
        final Entity _damagee = event.getEntity();

        // TODO: EntityTypes
        // Damager and damagee should be Players
        if (!(_damager instanceof Player) || !(_damagee instanceof Player))
            return;

        // compute the damages
        DamageUtils.computeDamage(event);
    }
}
