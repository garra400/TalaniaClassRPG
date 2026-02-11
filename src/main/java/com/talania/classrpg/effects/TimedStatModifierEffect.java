package com.talania.classrpg.effects;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.entities.EntityAnimationEffect;
import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatModifier;
import com.talania.core.stats.StatsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Temporary stat modifiers applied for a fixed duration.
 */
public final class TimedStatModifierEffect implements EntityAnimationEffect {
    private final UUID entityId;
    private final List<StatModifier> modifiers;
    private final long endAt;
    private boolean started;

    public TimedStatModifierEffect(UUID entityId, List<StatModifier> modifiers, long durationMs) {
        this.entityId = entityId;
        this.modifiers = modifiers == null ? List.of() : new ArrayList<>(modifiers);
        this.endAt = System.currentTimeMillis() + Math.max(0L, durationMs);
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        if (started || entityId == null) {
            return;
        }
        EntityStats stats = StatsManager.getOrCreate(entityId);
        for (StatModifier modifier : modifiers) {
            stats.addModifier(modifier);
        }
        started = true;
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        // no-op
    }

    @Override
    public void stop(Store<EntityStore> store) {
        if (entityId == null) {
            return;
        }
        EntityStats stats = StatsManager.get(entityId);
        if (stats == null) {
            return;
        }
        for (StatModifier modifier : modifiers) {
            if (modifier != null) {
                stats.removeModifier(modifier.getId());
            }
        }
    }

    @Override
    public boolean isFinished(long nowMs) {
        return nowMs >= endAt;
    }
}
