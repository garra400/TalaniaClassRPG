package com.talania.classrpg;

import com.talania.core.profile.TalaniaPlayerProfile;
import com.talania.core.profile.TalaniaProfileRuntime;
import com.talania.core.progression.LevelProgress;
import com.talania.core.progression.LevelingCurve;
import com.talania.core.progression.LevelingResult;
import com.talania.core.progression.LevelingService;

import java.util.UUID;

/**
 * Handles class leveling and XP progression.
 */
public final class ClassProgressionService {
    private final TalaniaProfileRuntime profileRuntime;
    private final LevelingService levelingService;
    private final LevelingCurve curve;

    public ClassProgressionService(TalaniaProfileRuntime profileRuntime, LevelingCurve curve) {
        this.profileRuntime = profileRuntime;
        this.curve = curve;
        this.levelingService = new LevelingService();
    }

    public LevelProgress getProgress(UUID playerId, String classId) {
        TalaniaPlayerProfile profile = loadProfile(playerId);
        if (profile == null) {
            return null;
        }
        return profile.getOrCreateClassProgress(classId);
    }

    public int getLevel(UUID playerId, String classId) {
        LevelProgress progress = getProgress(playerId, classId);
        return progress == null ? 0 : progress.level();
    }

    public LevelingResult addXp(UUID playerId, String classId, long amount) {
        TalaniaPlayerProfile profile = loadProfile(playerId);
        if (profile == null || classId == null || classId.isBlank()) {
            return new LevelingResult(0, 0, 0, 0L, false);
        }
        LevelProgress progress = profile.getOrCreateClassProgress(classId);
        if (progress == null) {
            return new LevelingResult(0, 0, 0, 0L, false);
        }
        return levelingService.addXp(progress, amount, curve);
    }

    private TalaniaPlayerProfile loadProfile(UUID playerId) {
        if (playerId == null || profileRuntime == null) {
            return null;
        }
        return profileRuntime.load(playerId);
    }
}
