package com.talania.classrpg.effects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.combat.targeting.AreaOfEffect;
import com.talania.core.combat.utils.AreaHealing;
import com.talania.core.entities.EntityAnimationEffect;
import com.talania.core.entities.EntityAnimationManager;
import com.talania.core.hytale.effects.EntityEffectService;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;

import java.util.List;

/**
 * Paladin Radiance healing zone with enemy slow.
 */
public final class RadianceEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> sourceRef;
    private final AreaHealing.State healingState = new AreaHealing.State();
    private final AreaHealing.Settings settings;
    private final double radius;
    private final float slowMultiplier;
    private final long slowDurationMs;
    private final String slowEffectId;
    private final long slowEffectDurationMs;
    private final long endAt;
    private boolean started;

    public RadianceEffect(Ref<EntityStore> sourceRef, long durationMs, AreaHealing.Settings settings, double radius,
                          float slowMultiplier, long slowDurationMs, String slowEffectId, long slowEffectDurationMs) {
        this.sourceRef = sourceRef;
        this.settings = settings == null ? new AreaHealing.Settings() : settings;
        this.radius = radius;
        this.slowMultiplier = Math.max(0.0f, slowMultiplier);
        this.slowDurationMs = Math.max(0L, slowDurationMs);
        this.slowEffectId = slowEffectId;
        this.slowEffectDurationMs = Math.max(0L, slowEffectDurationMs);
        this.endAt = System.currentTimeMillis() + Math.max(0L, durationMs);
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        if (started || store == null || sourceRef == null || !sourceRef.isValid()) {
            return;
        }
        Vector3d center = resolvePosition(store, sourceRef);
        if (center == null) {
            return;
        }
        AreaHealing.start(healingState, center, nowMs, Math.max(0L, endAt - nowMs));
        // Slow nearby players (excluding caster) for a short time.
        List<Ref<EntityStore>> players = AreaOfEffect.collectSphere(sourceRef, store, center, radius, true, null);
        for (Ref<EntityStore> target : players) {
            if (target == null || !target.isValid() || target.equals(sourceRef)) {
                continue;
            }
            Player player = store.getComponent(target, Player.getComponentType());
            if (player == null) {
                continue;
            }
            if (slowDurationMs > 0L && slowMultiplier > 0.0f && slowMultiplier != 1.0f) {
                EntityAnimationManager.get().add(new MovementModifierEffect(target, slowMultiplier, slowDurationMs), store, nowMs);
            }
            long effectDuration = slowEffectDurationMs > 0L ? slowEffectDurationMs : slowDurationMs;
            if (effectDuration > 0L) {
                EntityEffectService.apply(target, store, slowEffectId, effectDuration, OverlapBehavior.OVERWRITE);
            }
        }
        started = true;
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        if (store == null) {
            return;
        }
        AreaHealing.tick(healingState, store, settings, 1.0f);
    }

    @Override
    public void stop(Store<EntityStore> store) {
        AreaHealing.end(healingState);
    }

    @Override
    public boolean isFinished(long nowMs) {
        return nowMs >= endAt;
    }

    private static Vector3d resolvePosition(Store<EntityStore> store, Ref<EntityStore> ref) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            return null;
        }
        return new Vector3d(transform.getPosition());
    }
}
