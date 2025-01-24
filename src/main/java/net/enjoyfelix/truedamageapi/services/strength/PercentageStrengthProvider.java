package net.enjoyfelix.truedamageapi.services.strength;

import lombok.NonNull;
import net.enjoyfelix.truedamageapi.model.TimedEffect;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PercentageStrengthProvider implements StrengthProvider {
    // the percentage for strength 0 (1 in game)
    private final double baseScalar;
    private final Map<UUID, TimedEffect> playerEffectMap;

    /**
     * A strength provider that does percentage <3
     * @param baseScalar The percentage (should be lower than 1) at level 0  (minecraft does that shit)
     */
    public PercentageStrengthProvider (final double baseScalar) {
        this.baseScalar = baseScalar;
        this.playerEffectMap = new HashMap<>();
    }

    @Override
    public double getTotalScalar(final @NonNull Player player) {
        return 1 + (baseScalar * getAmplifier(player));
    }

    @Override
    public double getBaseScalar() {
        return baseScalar;
    }

    @Override
    public double getAmplifier(Player player) {
        final UUID uuid = player.getUniqueId();

        // get the TimedEffect associated to the player
        final TimedEffect effect = playerEffectMap.get(uuid);

        // early termination if the player doesn't have an effect active
        if (effect == null)
            return 0;

        // has the effect expired ?
        if (effect.getBestBy() < System.currentTimeMillis()){
            playerEffectMap.remove(uuid);
            return 0;
        }

        // return the amplifier
        return effect.getAmplifier();
    }

    @Override
    public void setAmplier(final @NonNull Player player, double amplifier, int time, boolean force) {
        final UUID uuid = player.getUniqueId();

        // early termination if the player already has an effect, and we don't want to ovverride it
        if (!force && playerEffectMap.containsKey(uuid))
            return;

        // convert the time in ticks to ms [(time / 20) * 1000]
        final long timeInMs = time * 50L;
        final long bestBy = System.currentTimeMillis() + timeInMs;

        // give the player the effect
        playerEffectMap.put(uuid, TimedEffect.from(bestBy, amplifier + 1));
    }
}
