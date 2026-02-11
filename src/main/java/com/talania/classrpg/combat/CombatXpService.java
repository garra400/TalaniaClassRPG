package com.talania.classrpg.combat;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.classrpg.ClassProgressionService;
import com.talania.classrpg.ClassService;
import com.talania.classrpg.ClassType;
import com.talania.classrpg.config.ClassRpgConfig;
import com.talania.core.events.entity.NpcDeathEvent;
import com.talania.core.events.entity.PlayerDeathEvent;

import java.util.UUID;

/**
 * Awards class XP for combat interactions.
 */
public final class CombatXpService {
    private final ClassService classService;
    private final ClassProgressionService progressionService;
    private final ClassRpgConfig.CombatXp config;

    public CombatXpService(ClassService classService, ClassProgressionService progressionService, ClassRpgConfig.CombatXp config) {
        this.classService = classService;
        this.progressionService = progressionService;
        this.config = config == null ? new ClassRpgConfig.CombatXp() : config;
    }

    public void awardHitXp(UUID attackerId, ClassType attackerClass, boolean targetIsPlayer, float damageAmount) {
        if (attackerId == null || attackerClass == null || progressionService == null) {
            return;
        }
        if (!shouldAwardForTarget(targetIsPlayer)) {
            return;
        }
        long xp = Math.round(Math.max(0.0f, damageAmount) * Math.max(0.0f, config.damageToXp));
        xp += Math.max(0L, config.hitXp);
        if (config.maxHitXp > 0L) {
            xp = Math.min(xp, config.maxHitXp);
        }
        if (xp <= 0L) {
            return;
        }
        progressionService.addXp(attackerId, attackerClass.id(), xp);
    }

    public void handleNpcDeath(NpcDeathEvent event) {
        if (event == null) {
            return;
        }
        handleDeath(event.targetRef(), event.damage(), false);
    }

    public void handlePlayerDeath(PlayerDeathEvent event) {
        if (event == null) {
            return;
        }
        handleDeath(event.targetRef(), event.damage(), true);
    }

    private void handleDeath(Ref<EntityStore> targetRef, Damage damage, boolean targetIsPlayer) {
        if (!shouldAwardForTarget(targetIsPlayer) || damage == null) {
            return;
        }
        if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) {
            return;
        }
        Ref<EntityStore> attackerRef = entitySource.getRef();
        if (attackerRef == null || !attackerRef.isValid() || attackerRef.equals(targetRef)) {
            return;
        }
        Store<EntityStore> store = attackerRef.getStore();
        if (store == null && targetRef != null) {
            store = targetRef.getStore();
        }
        if (store == null) {
            return;
        }
        Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
        if (attackerPlayer == null) {
            return;
        }
        UUID attackerId = resolveUuid(store, attackerRef);
        if (attackerId == null) {
            return;
        }
        ClassType attackerClass = classService == null ? null : classService.getAssigned(attackerId);
        if (attackerClass == null || progressionService == null) {
            return;
        }
        long xp = targetIsPlayer ? config.killXpPlayer : config.killXpNpc;
        if (xp <= 0L) {
            return;
        }
        progressionService.addXp(attackerId, attackerClass.id(), xp);
    }

    private boolean shouldAwardForTarget(boolean targetIsPlayer) {
        if (targetIsPlayer) {
            return config.awardOnPlayer;
        }
        return config.awardOnNpc;
    }

    private static UUID resolveUuid(Store<EntityStore> store, Ref<EntityStore> ref) {
        if (store == null || ref == null) {
            return null;
        }
        UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
        return uuid == null ? null : uuid.getUuid();
    }
}
