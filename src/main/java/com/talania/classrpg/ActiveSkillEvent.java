package com.talania.classrpg;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Fired when an active skill is successfully triggered.
 */
public final class ActiveSkillEvent {
    private final UUID playerId;
    private final ClassType classType;
    private final ActiveSkill skill;
    private final long timestamp;
    private final Ref<EntityStore> ref;
    private final Store<EntityStore> store;

    public ActiveSkillEvent(UUID playerId,
                            ClassType classType,
                            ActiveSkill skill,
                            long timestamp,
                            Ref<EntityStore> ref,
                            Store<EntityStore> store) {
        this.playerId = playerId;
        this.classType = classType;
        this.skill = skill;
        this.timestamp = timestamp;
        this.ref = ref;
        this.store = store;
    }

    public UUID playerId() {
        return playerId;
    }

    public ClassType classType() {
        return classType;
    }

    public ActiveSkill skill() {
        return skill;
    }

    public long timestamp() {
        return timestamp;
    }

    public Ref<EntityStore> ref() {
        return ref;
    }

    public Store<EntityStore> store() {
        return store;
    }
}
