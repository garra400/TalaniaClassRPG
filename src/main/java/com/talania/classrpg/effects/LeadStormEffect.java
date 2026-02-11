package com.talania.classrpg.effects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.combat.utils.AreaDamage;
import com.talania.core.entities.EntityAnimationEffect;

/**
 * Repeating AoE damage around the caster.
 */
public final class LeadStormEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> sourceRef;
    private final double radius;
    private final float damage;
    private final long tickMs;
    private final long endAt;
    private long nextTickAt;

    public LeadStormEffect(Ref<EntityStore> sourceRef, double radius, float damage, long durationMs, long tickMs) {
        this.sourceRef = sourceRef;
        this.radius = radius;
        this.damage = damage;
        this.tickMs = Math.max(100L, tickMs);
        this.endAt = System.currentTimeMillis() + Math.max(0L, durationMs);
        this.nextTickAt = System.currentTimeMillis();
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        // no-op
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        if (store == null || sourceRef == null || !sourceRef.isValid()) {
            return;
        }
        if (nowMs < nextTickAt) {
            return;
        }
        com.hypixel.hytale.server.core.modules.entity.component.TransformComponent transform =
                store.getComponent(sourceRef, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) {
            return;
        }
        AreaDamage.damageSphere(sourceRef, store, transform.getPosition(), radius, DamageCause.PHYSICAL, damage, true, null);
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
