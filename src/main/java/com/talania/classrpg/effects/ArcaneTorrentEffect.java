package com.talania.classrpg.effects;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.entities.EntityAnimationEffect;
import com.talania.core.projectiles.ProjectileTargetingUtil;

/**
 * Spawns a stream of projectiles toward a target position.
 */
public final class ArcaneTorrentEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> sourceRef;
    private final Vector3d target;
    private final String projectileId;
    private final long endAt;
    private final long tickMs;
    private long nextTickAt;

    public ArcaneTorrentEffect(Ref<EntityStore> sourceRef, Vector3d target,
                               String projectileId, long durationMs, long tickMs) {
        this.sourceRef = sourceRef;
        this.target = target == null ? null : new Vector3d(target);
        this.projectileId = projectileId;
        this.endAt = System.currentTimeMillis() + Math.max(0L, durationMs);
        this.tickMs = Math.max(100L, tickMs);
        this.nextTickAt = System.currentTimeMillis();
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        // no-op
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        if (store == null || sourceRef == null || !sourceRef.isValid() || target == null) {
            return;
        }
        if (nowMs < nextTickAt) {
            return;
        }
        spawnProjectile(store, sourceRef, target, projectileId);
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

    private static boolean spawnProjectile(Store<EntityStore> store, Ref<EntityStore> sourceRef,
                                           Vector3d target, String projectileId) {
        if (store == null || sourceRef == null || target == null || projectileId == null || projectileId.isBlank()) {
            return false;
        }
        TransformComponent transform = store.getComponent(sourceRef, TransformComponent.getComponentType());
        if (transform == null) {
            return false;
        }
        Vector3d origin = new Vector3d(transform.getPosition());
        origin.y += 1.4;
        Vector3f rotation = ProjectileTargetingUtil.rotationToward(origin, target);
        if (rotation == null) {
            return false;
        }
        TimeResource timeResource = store.getResource(TimeResource.getResourceType());
        if (timeResource == null) {
            return false;
        }
        Holder<EntityStore> holder = ProjectileComponent.assembleDefaultProjectile(timeResource, projectileId, origin, rotation);
        ProjectileComponent projectileComponent = holder.getComponent(ProjectileComponent.getComponentType());
        if (projectileComponent == null) {
            return false;
        }
        if (projectileComponent.getProjectile() == null) {
            projectileComponent.initialize();
            if (projectileComponent.getProjectile() == null) {
                return false;
            }
        }
        UUIDComponent uuid = store.getComponent(sourceRef, UUIDComponent.getComponentType());
        if (uuid == null) {
            return false;
        }
        projectileComponent.shoot(holder, uuid.getUuid(), origin.getX(), origin.getY(), origin.getZ(), rotation.getYaw(), rotation.getPitch());
        store.addEntity(holder, AddReason.SPAWN);
        return true;
    }
}
