package net.enjoyfelix.truedamageapi;

import net.minecraft.server.v1_8_R3.ItemArmor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DamageListener implements Listener {

    /**
     * Handles a player hitting another player.
     * @param event the event
     */
    @EventHandler
    public void onPlayerDamagePlayer(final EntityDamageByEntityEvent event) {
        final Entity _damager = event.getDamager();
        final Entity _damagee = event.getEntity();

        // Damager and damagee should be Players
        if (!(_damager instanceof Player) || !(_damagee instanceof Player))
            return;

        // get the entities as player
        final Player damager = (Player) _damager;
        final Player damagee = (Player) _damagee;

        // get a copy of spigot's intepretation of the damage
        final Map<EntityDamageEvent.DamageModifier, Double> damages = new HashMap<>();
        for (final EntityDamageEvent.DamageModifier type : EntityDamageEvent.DamageModifier.values()) {

            // get the damage of the type and copy it into the new map
            final double dmg = event.getDamage(type);
            damages.put(type, dmg);
        }

        damager.sendMessage(getPrettyDamageMap(damages));

        // calculate our own;
        final Map<EntityDamageEvent.DamageModifier, Double> computesDamages = DamageUtils.computeDamage(damager, damagee, event.getDamage());
        for (final EntityDamageEvent.DamageModifier type : EntityDamageEvent.DamageModifier.values()) {
            if (computesDamages.containsKey(type)) continue;
            computesDamages.put(type, 0.0);
        }

        damager.sendMessage(getPrettyDamageMap(computesDamages));
    }

    /**
     * Creates a table containing the values of the map
     * @param damages a map of the damages by types
     * @return A pretty string
     */
    private static String getPrettyDamageMap(final Map<EntityDamageEvent.DamageModifier, Double> damages){
        // the padding before and after a value
        final String padding = " ";

        // Get a string build for each line
        final StringBuilder typeLine = new StringBuilder();
        final StringBuilder valueLine = new StringBuilder();

        // create the header of the table
        typeLine.append("┃");
        valueLine.append("┃");

        // iterate of the types
        for (final EntityDamageEvent.DamageModifier type : EntityDamageEvent.DamageModifier.values()) {
            // get the damage as a string of pair length
            String dmgstr = damages.get(type) + "";
            if (dmgstr.length() % 2 != 0)
                dmgstr += padding;

            // get the type as a string of pair length
            String typestr = type.toString();
            if (typestr.length() % 2 != 0)
                typestr += padding;

            // get the length of both string
            final int dmgstrLength = dmgstr.length();
            final int typestrLength = typestr.length();

            //sort them
            String smallestStr;
            String largestStr;

            if (dmgstrLength > typestrLength) {
                smallestStr = typestr;
                largestStr = dmgstr;
            } else {
                smallestStr = dmgstr;
                largestStr = typestr;
            }


            // padd the smallest one
            while (smallestStr.length() < largestStr.length()) {
                smallestStr = padding + smallestStr + padding;
            }

            // print them
            typeLine.append(padding + typestr + padding + "┃");
            valueLine.append(padding + dmgstr + padding + "┃");
        }

        // build the strings
        final String typeString = typeLine.toString();
        final String valueString = valueLine.toString();

        // create intermediate lines
        String topLineString = "┏";
        String midLineString = "┣";
        String bottomLineString = "┗";
        for (int i = 1; i < typeString.length() -1; i++) {
            char charAt = typeString.charAt(i);

            if (charAt != '┃') {
                topLineString += "━";
                midLineString += "━";
                bottomLineString += "━";
            } else {
                topLineString += "┳";
                midLineString += "╋";
                bottomLineString += "┻";
            }
        }

        // return the builder char
        return  topLineString + "\n" +
                typeString + "\n" +
                midLineString + "\n" +
                valueString + "\n" +
                bottomLineString + "\n";
    }
}
