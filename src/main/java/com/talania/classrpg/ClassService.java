package com.talania.classrpg;

import com.talania.core.profile.TalaniaPlayerProfile;
import com.talania.core.profile.TalaniaProfileRuntime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Assigns classes to players and keeps profile state in sync.
 */
public final class ClassService {
    private final TalaniaProfileRuntime profileRuntime;
    private final Map<UUID, ClassType> assigned = new ConcurrentHashMap<>();

    public ClassService(TalaniaProfileRuntime profileRuntime) {
        this.profileRuntime = profileRuntime;
    }

    public ClassType getAssigned(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        ClassType cached = assigned.get(playerId);
        if (cached != null) {
            return cached;
        }
        TalaniaPlayerProfile profile = profileRuntime == null ? null : profileRuntime.get(playerId);
        if (profile == null || profile.classId() == null) {
            return null;
        }
        ClassType resolved = ClassType.fromId(profile.classId());
        if (resolved != null) {
            assigned.put(playerId, resolved);
        }
        return resolved;
    }

    public void setAssigned(UUID playerId, ClassType classType) {
        if (playerId == null || classType == null) {
            return;
        }
        assigned.put(playerId, classType);
        TalaniaPlayerProfile profile = profileRuntime == null ? null : profileRuntime.load(playerId);
        if (profile != null) {
            profile.setClassId(classType.id());
        }
    }

    public void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }
        assigned.remove(playerId);
        TalaniaPlayerProfile profile = profileRuntime == null ? null : profileRuntime.get(playerId);
        if (profile != null) {
            profile.setClassId(null);
        }
    }
}
