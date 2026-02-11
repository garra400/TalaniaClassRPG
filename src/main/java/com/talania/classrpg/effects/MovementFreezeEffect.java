package com.talania.classrpg.effects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.entities.EntityAnimationEffect;
import com.talania.core.utils.PlayerRefUtil;

/**
 * Fully freezes movement for a short duration.
 */
public final class MovementFreezeEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> ref;
    private final long endAt;
    private MovementSettings original;
    private boolean started;

    public MovementFreezeEffect(Ref<EntityStore> ref, long durationMs) {
        this.ref = ref;
        this.endAt = System.currentTimeMillis() + Math.max(0L, durationMs);
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        if (started || ref == null || store == null || !ref.isValid()) {
            return;
        }
        MovementManager movementManager = store.getComponent(ref, MovementManager.getComponentType());
        if (movementManager == null) {
            return;
        }
        MovementSettings current = movementManager.getSettings();
        if (current == null) {
            return;
        }
        original = new MovementSettings(current);
        current.baseSpeed = 0.0f;
        current.forwardWalkSpeedMultiplier = 0.0f;
        current.backwardWalkSpeedMultiplier = 0.0f;
        current.strafeWalkSpeedMultiplier = 0.0f;
        current.forwardRunSpeedMultiplier = 0.0f;
        current.backwardRunSpeedMultiplier = 0.0f;
        current.strafeRunSpeedMultiplier = 0.0f;
        current.forwardCrouchSpeedMultiplier = 0.0f;
        current.backwardCrouchSpeedMultiplier = 0.0f;
        current.strafeCrouchSpeedMultiplier = 0.0f;
        current.forwardSprintSpeedMultiplier = 0.0f;
        current.maxSpeedMultiplier = 0.0f;
        current.minSpeedMultiplier = 0.0f;
        current.jumpForce = 0.0f;
        current.swimJumpForce = 0.0f;
        pushMovementSettings(ref, store, movementManager);
        started = true;
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        // no-op
    }

    @Override
    public void stop(Store<EntityStore> store) {
        if (ref == null || store == null || !ref.isValid() || original == null) {
            return;
        }
        MovementManager movementManager = store.getComponent(ref, MovementManager.getComponentType());
        if (movementManager == null) {
            return;
        }
        MovementSettings current = movementManager.getSettings();
        if (current == null) {
            return;
        }
        // restore by copying
        MovementModifierEffect.restoreSettings(original, current);
        pushMovementSettings(ref, store, movementManager);
    }

    @Override
    public boolean isFinished(long nowMs) {
        return nowMs >= endAt;
    }

    private static void pushMovementSettings(Ref<EntityStore> ref, Store<EntityStore> store,
                                             MovementManager movementManager) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }
        PlayerRef playerRef = PlayerRefUtil.resolve(ref, store);
        if (playerRef == null) {
            return;
        }
        movementManager.update(playerRef.getPacketHandler());
    }
}
