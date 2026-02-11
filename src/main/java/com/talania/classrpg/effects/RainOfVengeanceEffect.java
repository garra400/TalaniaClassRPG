package com.talania.classrpg.effects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.combat.targeting.AreaOfEffect;
import com.talania.core.entities.EntityAnimationEffect;
import com.talania.core.entities.EntityAnimationManager;
import com.talania.core.hytale.effects.EntityEffectService;
import com.talania.core.projectiles.RainOfArrowsUtil;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;

import java.util.List;

/**
 * Delayed rain-of-arrows strike with slow.
 */
public final class RainOfVengeanceEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> sourceRef;
    private final Vector3d target;
    private final double radius;
    private final int arrows;
    private final float slowMultiplier;
    private final long slowDurationMs;
    private final String slowEffectId;
    private final long slowEffectDurationMs;
    private final long triggerAt;
    private final long endAt;
    private boolean triggered;

    public RainOfVengeanceEffect(Ref<EntityStore> sourceRef, Vector3d target, long delayMs,
                                 double radius, int arrows, float slowMultiplier, long slowDurationMs,
                                 String slowEffectId, long slowEffectDurationMs) {
        this.sourceRef = sourceRef;
        this.target = target == null ? null : new Vector3d(target);
        this.radius = Math.max(0.0, radius);
        this.arrows = Math.max(0, arrows);
        this.slowMultiplier = Math.max(0.0f, slowMultiplier);
        this.slowDurationMs = Math.max(0L, slowDurationMs);
        this.slowEffectId = slowEffectId;
        this.slowEffectDurationMs = Math.max(0L, slowEffectDurationMs);
        long now = System.currentTimeMillis();
        this.triggerAt = now + Math.max(0L, delayMs);
        this.endAt = this.triggerAt + 2000L;
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        // no-op
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        if (triggered || store == null || target == null || sourceRef == null) {
            return;
        }
        if (nowMs < triggerAt) {
            return;
        }
        triggered = true;
        RainOfArrowsUtil.Settings settings = new RainOfArrowsUtil.Settings();
        for (int i = 0; i < arrows; i++) {
            Vector3d landing = RainOfArrowsUtil.pickRandomLandingPosition(
                    sourceRef, store, target, radius, true, 1.0, 1.0, 6);
            if (landing != null) {
                RainOfArrowsUtil.spawnRainProjectileAt(sourceRef, store, landing, settings);
            }
        }
        List<Ref<EntityStore>> players = AreaOfEffect.collectSphere(sourceRef, store, target, radius, true, null);
        for (Ref<EntityStore> ref : players) {
            if (ref == null || !ref.isValid() || ref.equals(sourceRef)) {
                continue;
            }
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                continue;
            }
            if (slowDurationMs > 0L && slowMultiplier > 0.0f && slowMultiplier != 1.0f) {
                EntityAnimationManager.get().add(new MovementModifierEffect(ref, slowMultiplier, slowDurationMs), store, nowMs);
            }
            long effectDuration = slowEffectDurationMs > 0L ? slowEffectDurationMs : slowDurationMs;
            if (effectDuration > 0L) {
                EntityEffectService.apply(ref, store, slowEffectId, effectDuration, OverlapBehavior.OVERWRITE);
            }
        }
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
