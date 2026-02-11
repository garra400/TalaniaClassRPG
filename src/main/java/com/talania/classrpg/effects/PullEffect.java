package com.talania.classrpg.effects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.combat.targeting.AreaOfEffect;
import com.talania.core.entities.EntityAnimationEffect;

import java.util.List;

/**
 * Pulls nearby entities toward a center point for a short duration.
 */
public final class PullEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> sourceRef;
    private final Vector3d center;
    private final double radius;
    private final double strength;
    private final long endAt;
    private final long tickMs;
    private long nextTickAt;

    public PullEffect(Ref<EntityStore> sourceRef, Vector3d center, double radius,
                      double strength, long durationMs, long tickMs) {
        this.sourceRef = sourceRef;
        this.center = center == null ? null : new Vector3d(center);
        this.radius = radius;
        this.strength = strength;
        this.endAt = System.currentTimeMillis() + Math.max(0L, durationMs);
        this.tickMs = Math.max(50L, tickMs);
        this.nextTickAt = System.currentTimeMillis();
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        // no-op
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        if (store == null || center == null) {
            return;
        }
        if (nowMs < nextTickAt) {
            return;
        }
        List<Ref<EntityStore>> targets = AreaOfEffect.collectSphere(sourceRef, store, center, radius, true, null);
        for (Ref<EntityStore> target : targets) {
            if (target == null || !target.isValid()) {
                continue;
            }
            Velocity velocity = store.getComponent(target, Velocity.getComponentType());
            if (velocity == null) {
                continue;
            }
            TransformComponent transform = store.getComponent(target, TransformComponent.getComponentType());
            if (transform == null) {
                continue;
            }
            Vector3d delta = new Vector3d(center).subtract(transform.getPosition());
            if (delta.length() < 0.001) {
                continue;
            }
            delta.normalize();
            velocity.addForce(delta.scale(strength));
        }
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
