package com.talania.classrpg.effects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.entities.EntityAnimationEffect;

/**
 * Simple poison damage-over-time effect.
 */
public final class PoisonEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> targetRef;
    private final Ref<EntityStore> sourceRef;
    private final float damagePerTick;
    private final long tickMs;
    private final long endAt;
    private long nextTickAt;

    public PoisonEffect(Ref<EntityStore> targetRef, Ref<EntityStore> sourceRef,
                        float damagePerTick, long durationMs, long tickMs) {
        this.targetRef = targetRef;
        this.sourceRef = sourceRef;
        this.damagePerTick = damagePerTick;
        this.tickMs = Math.max(250L, tickMs);
        this.endAt = System.currentTimeMillis() + Math.max(0L, durationMs);
        this.nextTickAt = System.currentTimeMillis();
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        // no-op
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        if (store == null || targetRef == null || !targetRef.isValid()) {
            return;
        }
        if (nowMs < nextTickAt) {
            return;
        }
        Damage.Source source = sourceRef == null ? Damage.NULL_SOURCE : new Damage.EntitySource(sourceRef);
        Damage damage = new Damage(source, DamageCause.ENVIRONMENT, damagePerTick);
        DamageSystems.executeDamage(targetRef, store, damage);
        nextTickAt = nowMs + tickMs;
    }

    @Override
    public void stop(Store<EntityStore> store) {
        // no-op
    }

    @Override
    public boolean isFinished(long nowMs) {
        return nowMs >= endAt;
    }
}
