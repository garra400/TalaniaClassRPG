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
 * Temporary movement speed modifier for a player entity.
 */
public final class MovementModifierEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> ref;
    private final long endAt;
    private final float multiplier;
    private MovementSettings original;
    private boolean started;

    public MovementModifierEffect(Ref<EntityStore> ref, float multiplier, long durationMs) {
        this.ref = ref;
        this.multiplier = Math.max(0.0f, multiplier);
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
        applyMultiplier(current, original, multiplier);
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
        restoreSettings(original, current);
        pushMovementSettings(ref, store, movementManager);
    }

    @Override
    public boolean isFinished(long nowMs) {
        return nowMs >= endAt;
    }

    private static void applyMultiplier(MovementSettings target, MovementSettings base, float multiplier) {
        target.baseSpeed = base.baseSpeed * multiplier;
        target.forwardWalkSpeedMultiplier = base.forwardWalkSpeedMultiplier * multiplier;
        target.backwardWalkSpeedMultiplier = base.backwardWalkSpeedMultiplier * multiplier;
        target.strafeWalkSpeedMultiplier = base.strafeWalkSpeedMultiplier * multiplier;
        target.forwardRunSpeedMultiplier = base.forwardRunSpeedMultiplier * multiplier;
        target.backwardRunSpeedMultiplier = base.backwardRunSpeedMultiplier * multiplier;
        target.strafeRunSpeedMultiplier = base.strafeRunSpeedMultiplier * multiplier;
        target.forwardCrouchSpeedMultiplier = base.forwardCrouchSpeedMultiplier * multiplier;
        target.backwardCrouchSpeedMultiplier = base.backwardCrouchSpeedMultiplier * multiplier;
        target.strafeCrouchSpeedMultiplier = base.strafeCrouchSpeedMultiplier * multiplier;
        target.forwardSprintSpeedMultiplier = base.forwardSprintSpeedMultiplier * multiplier;
        target.maxSpeedMultiplier = base.maxSpeedMultiplier * multiplier;
        target.minSpeedMultiplier = base.minSpeedMultiplier * multiplier;
    }

    public static void restoreSettings(MovementSettings src, MovementSettings dest) {
        dest.mass = src.mass;
        dest.dragCoefficient = src.dragCoefficient;
        dest.invertedGravity = src.invertedGravity;
        dest.velocityResistance = src.velocityResistance;
        dest.jumpForce = src.jumpForce;
        dest.swimJumpForce = src.swimJumpForce;
        dest.jumpBufferDuration = src.jumpBufferDuration;
        dest.jumpBufferMaxYVelocity = src.jumpBufferMaxYVelocity;
        dest.acceleration = src.acceleration;
        dest.airDragMin = src.airDragMin;
        dest.airDragMax = src.airDragMax;
        dest.airDragMinSpeed = src.airDragMinSpeed;
        dest.airDragMaxSpeed = src.airDragMaxSpeed;
        dest.airFrictionMin = src.airFrictionMin;
        dest.airFrictionMax = src.airFrictionMax;
        dest.airFrictionMinSpeed = src.airFrictionMinSpeed;
        dest.airFrictionMaxSpeed = src.airFrictionMaxSpeed;
        dest.airSpeedMultiplier = src.airSpeedMultiplier;
        dest.airControlMinSpeed = src.airControlMinSpeed;
        dest.airControlMaxSpeed = src.airControlMaxSpeed;
        dest.airControlMinMultiplier = src.airControlMinMultiplier;
        dest.airControlMaxMultiplier = src.airControlMaxMultiplier;
        dest.comboAirSpeedMultiplier = src.comboAirSpeedMultiplier;
        dest.baseSpeed = src.baseSpeed;
        dest.climbSpeed = src.climbSpeed;
        dest.climbSpeedLateral = src.climbSpeedLateral;
        dest.climbUpSprintSpeed = src.climbUpSprintSpeed;
        dest.climbDownSprintSpeed = src.climbDownSprintSpeed;
        dest.horizontalFlySpeed = src.horizontalFlySpeed;
        dest.verticalFlySpeed = src.verticalFlySpeed;
        dest.maxSpeedMultiplier = src.maxSpeedMultiplier;
        dest.minSpeedMultiplier = src.minSpeedMultiplier;
        dest.wishDirectionGravityX = src.wishDirectionGravityX;
        dest.wishDirectionGravityY = src.wishDirectionGravityY;
        dest.wishDirectionWeightX = src.wishDirectionWeightX;
        dest.wishDirectionWeightY = src.wishDirectionWeightY;
        dest.canFly = src.canFly;
        dest.collisionExpulsionForce = src.collisionExpulsionForce;
        dest.forwardWalkSpeedMultiplier = src.forwardWalkSpeedMultiplier;
        dest.backwardWalkSpeedMultiplier = src.backwardWalkSpeedMultiplier;
        dest.strafeWalkSpeedMultiplier = src.strafeWalkSpeedMultiplier;
        dest.forwardRunSpeedMultiplier = src.forwardRunSpeedMultiplier;
        dest.backwardRunSpeedMultiplier = src.backwardRunSpeedMultiplier;
        dest.strafeRunSpeedMultiplier = src.strafeRunSpeedMultiplier;
        dest.forwardCrouchSpeedMultiplier = src.forwardCrouchSpeedMultiplier;
        dest.backwardCrouchSpeedMultiplier = src.backwardCrouchSpeedMultiplier;
        dest.strafeCrouchSpeedMultiplier = src.strafeCrouchSpeedMultiplier;
        dest.forwardSprintSpeedMultiplier = src.forwardSprintSpeedMultiplier;
        dest.variableJumpFallForce = src.variableJumpFallForce;
        dest.fallEffectDuration = src.fallEffectDuration;
        dest.fallJumpForce = src.fallJumpForce;
        dest.fallMomentumLoss = src.fallMomentumLoss;
        dest.autoJumpObstacleSpeedLoss = src.autoJumpObstacleSpeedLoss;
        dest.autoJumpObstacleSprintSpeedLoss = src.autoJumpObstacleSprintSpeedLoss;
        dest.autoJumpObstacleEffectDuration = src.autoJumpObstacleEffectDuration;
        dest.autoJumpObstacleSprintEffectDuration = src.autoJumpObstacleSprintEffectDuration;
        dest.autoJumpObstacleMaxAngle = src.autoJumpObstacleMaxAngle;
        dest.autoJumpDisableJumping = src.autoJumpDisableJumping;
        dest.minSlideEntrySpeed = src.minSlideEntrySpeed;
        dest.slideExitSpeed = src.slideExitSpeed;
        dest.minFallSpeedToEngageRoll = src.minFallSpeedToEngageRoll;
        dest.maxFallSpeedToEngageRoll = src.maxFallSpeedToEngageRoll;
        dest.rollStartSpeedModifier = src.rollStartSpeedModifier;
        dest.rollExitSpeedModifier = src.rollExitSpeedModifier;
        dest.rollTimeToComplete = src.rollTimeToComplete;
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
