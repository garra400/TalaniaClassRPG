package com.talania.classrpg;

import com.talania.classrpg.config.ClassRpgConfig;
import com.talania.classrpg.combat.CombatXpService;
import com.talania.classrpg.skills.ActiveSkillExecutor;
import com.talania.classrpg.state.ClassRpgState;
import com.talania.core.abilities.AbilityCooldownService;
import com.talania.core.config.ConfigManager;
import com.talania.core.events.EventBus;
import com.talania.core.input.InputActionEvent;
import com.talania.core.events.entity.NpcDeathEvent;
import com.talania.core.events.entity.PlayerDeathEvent;
import com.talania.core.profile.TalaniaProfileRuntime;
import com.talania.core.progression.LevelingCurve;
import com.talania.core.progression.LinearLevelingCurve;
import com.talania.core.runtime.TalaniaCoreRuntime;

import java.nio.file.Path;

/**
 * Runtime wiring for TalaniaClassRPG services.
 */
public final class ClassRpgRuntime {
    private final TalaniaProfileRuntime profileRuntime;
    private final ClassService classService;
    private final ClassProgressionService progressionService;
    private final ActiveSkillService activeSkillService;
    private final ActiveSkillExecutor activeSkillExecutor;
    private final ClassRpgState state;
    private final CombatXpService combatXpService;
    private final ClassRpgConfig config;

    public ClassRpgRuntime(Path dataDirectory) {
        TalaniaCoreRuntime coreRuntime = TalaniaCoreRuntime.get();
        this.profileRuntime = coreRuntime != null
                ? coreRuntime.profileRuntime()
                : new TalaniaProfileRuntime(dataDirectory);

        ConfigManager.initialize(dataDirectory);
        ClassRpgConfig loadedConfig = ConfigManager.load("class_rpg_config.json", ClassRpgConfig.class, ClassRpgConfig.class);
        this.config = loadedConfig == null ? new ClassRpgConfig() : loadedConfig;
        this.config.ensureDefaults();
        int maxLevel = config.maxLevel;
        long baseXp = config.baseXp;
        long stepXp = config.stepXp;
        LevelingCurve curve = new LinearLevelingCurve(maxLevel, baseXp, stepXp);
        this.classService = new ClassService(profileRuntime);
        this.progressionService = new ClassProgressionService(profileRuntime, curve);
        this.activeSkillService = new ActiveSkillService(classService, new AbilityCooldownService());
        this.state = new ClassRpgState();
        this.combatXpService = new CombatXpService(classService, progressionService, config.combatXp);
        this.activeSkillExecutor = new ActiveSkillExecutor(state, config.skills);

        EventBus.subscribe(InputActionEvent.class, activeSkillService::handleInputAction);
        EventBus.subscribe(ActiveSkillEvent.class, activeSkillExecutor::handle);
        EventBus.subscribe(NpcDeathEvent.class, combatXpService::handleNpcDeath);
        EventBus.subscribe(PlayerDeathEvent.class, combatXpService::handlePlayerDeath);
    }

    public TalaniaProfileRuntime profileRuntime() {
        return profileRuntime;
    }

    public ClassService classService() {
        return classService;
    }

    public ClassProgressionService progressionService() {
        return progressionService;
    }

    public ActiveSkillService activeSkillService() {
        return activeSkillService;
    }

    public ActiveSkillExecutor activeSkillExecutor() {
        return activeSkillExecutor;
    }

    public ClassRpgState state() {
        return state;
    }

    public CombatXpService combatXpService() {
        return combatXpService;
    }

    public ClassRpgConfig config() {
        return config;
    }
}
