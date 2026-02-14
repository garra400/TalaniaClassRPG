package com.talania.classrpg.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.classrpg.ActiveSkill;
import com.talania.classrpg.ClassService;
import com.talania.classrpg.ClassType;
import com.talania.classrpg.SkillSlot;
import com.talania.classrpg.config.ClassDefinitionsConfig;
import com.talania.classrpg.config.ClassRpgConfig;
import com.talania.core.localization.T;
import com.talania.core.profile.TalaniaPlayerProfile;
import com.talania.core.profile.TalaniaProfileRuntime;
import com.talania.core.progression.LevelProgress;
import com.talania.core.progression.LevelingCurve;
import com.talania.core.progression.LinearLevelingCurve;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * UI page that displays player RPG/class information.
 */
public final class ClassProfilePage extends InteractiveCustomUIPage<ClassProfilePage.ProfileEventData> {

    public static class ProfileEventData {
        public String action;

        public static final BuilderCodec<ProfileEventData> CODEC =
                BuilderCodec.builder(ProfileEventData.class, ProfileEventData::new)
                        .append(
                                new KeyedCodec<>("Action", Codec.STRING),
                                (ProfileEventData o, String v) -> o.action = v,
                                (ProfileEventData o) -> o.action
                        )
                        .add()
                        .build();
    }

    private final ClassService classService;
    private final TalaniaProfileRuntime profileRuntime;
    private final ClassRpgConfig config;
    private final ClassDefinitionsConfig classDefinitions;

    public ClassProfilePage(@Nonnull PlayerRef playerRef,
                            ClassService classService,
                            TalaniaProfileRuntime profileRuntime,
                            ClassRpgConfig config,
                            ClassDefinitionsConfig classDefinitions) {
        super(playerRef, CustomPageLifetime.CantClose, ProfileEventData.CODEC);
        this.classService = classService;
        this.profileRuntime = profileRuntime;
        this.config = config == null ? new ClassRpgConfig() : config;
        this.classDefinitions = classDefinitions;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder evt,
                      @Nonnull Store<EntityStore> store) {
        cmd.append("Pages/class_profile.ui");

        cmd.set("#TitleText.Text", T.t("ui.class_profile.title"));
        cmd.set("#InfoHeader.Text", T.t("ui.class_profile.basic"));
        cmd.set("#SkillsHeader.Text", T.t("ui.class_profile.skills"));
        cmd.set("#ActiveSkillsHeader.Text", T.t("ui.class_profile.active_skills"));
        cmd.set("#PassiveSkillsHeader.Text", T.t("ui.class_profile.passive_skills"));
        cmd.set("#StrengthsHeader.Text", T.t("ui.class_profile.strengths"));
        cmd.set("#WeaknessesHeader.Text", T.t("ui.class_profile.weaknesses"));
        cmd.set("#CloseButton.Text", T.t("ui.class_profile.close"));
        cmd.set("#PlayerLabel.Text", T.t("ui.class_profile.player"));
        cmd.set("#ClassLabel.Text", T.t("ui.class_profile.class"));
        cmd.set("#ClassIdLabel.Text", T.t("ui.class_profile.class_id"));
        cmd.set("#DeityLabel.Text", T.t("ui.class_profile.deity"));
        cmd.set("#GameplayLabel.Text", T.t("ui.class_profile.gameplay"));
        cmd.set("#LevelLabel.Text", T.t("ui.class_profile.level"));
        cmd.set("#XpLabel.Text", T.t("ui.class_profile.xp"));
        cmd.set("#MaxLevelLabel.Text", T.t("ui.class_profile.max_level"));

        Player player = store.getComponent(ref, Player.getComponentType());
        String playerName = player != null ? player.getDisplayName() : playerRef.getUsername();
        cmd.set("#PlayerValue.Text", playerName != null ? playerName : "-");

        TalaniaPlayerProfile profile = profileRuntime != null ? profileRuntime.load(playerRef.getUuid()) : null;
        String classId = profile != null ? profile.classId() : null;
        ClassType classType = ClassType.fromId(classId);
        if (classType == null && classService != null) {
            ClassType assigned = classService.getAssigned(playerRef.getUuid());
            if (assigned != null) {
                classType = assigned;
                classId = assigned.id();
            }
        }

        if (classType == null) {
            cmd.set("#ClassValue.Text", T.t("ui.class_profile.no_class"));
            cmd.set("#ClassIdValue.Text", "-");
            cmd.set("#DeityValue.Text", "-");
            cmd.set("#GameplayValue.Text", "-");
            cmd.set("#LevelValue.Text", "0");
            cmd.set("#XpValue.Text", "0 / 0");
            cmd.set("#MaxLevelValue.Text", String.valueOf(config.maxLevel));
            clearLists(cmd);
        } else {
            String className = resolveClassName(classType);
            cmd.set("#ClassValue.Text", className);
            cmd.set("#ClassIdValue.Text", classType.id());
            cmd.set("#DeityValue.Text", classType.deity() != null ? classType.deity() : "-");
            cmd.set("#GameplayValue.Text", classType.gameplay() != null ? classType.gameplay() : "-");

            LevelProgress progress = profile != null ? profile.getClassProgress(classType.id()) : null;
            int level = progress != null ? progress.level() : 0;
            long xp = progress != null ? progress.xp() : 0L;
            LevelingCurve curve = new LinearLevelingCurve(config.maxLevel, config.baseXp, config.stepXp);
            long xpNext = curve.xpForNextLevel(level);
            if (level >= curve.maxLevel()) {
                cmd.set("#LevelValue.Text", String.valueOf(curve.maxLevel()));
                cmd.set("#XpValue.Text", xp + " / MAX");
            } else {
                cmd.set("#LevelValue.Text", String.valueOf(level));
                cmd.set("#XpValue.Text", xp + " / " + xpNext);
            }
            cmd.set("#MaxLevelValue.Text", String.valueOf(curve.maxLevel()));

            fillLists(cmd, classType);
        }

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
                new EventData().append("Action", "close"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull ProfileEventData data) {
        if (data == null || data.action == null) {
            return;
        }
        if ("close".equals(data.action)) {
            this.close();
        }
    }

    private void clearLists(UICommandBuilder cmd) {
        cmd.clear("#ActiveSkillsList");
        cmd.clear("#PassiveSkillsList");
        cmd.clear("#StrengthsList");
        cmd.clear("#WeaknessesList");
    }

    private void fillLists(UICommandBuilder cmd, ClassType classType) {
        cmd.clear("#ActiveSkillsList");
        cmd.clear("#PassiveSkillsList");
        cmd.clear("#StrengthsList");
        cmd.clear("#WeaknessesList");

        List<String> activeLines = new ArrayList<>();
        ActiveSkill eSkill = classType.getActiveSkill(SkillSlot.E);
        ActiveSkill rSkill = classType.getActiveSkill(SkillSlot.R);
        if (eSkill != null) {
            activeLines.add(formatActiveSkill("E", eSkill));
        }
        if (rSkill != null) {
            activeLines.add(formatActiveSkill("R", rSkill));
        }
        if (activeLines.isEmpty()) {
            activeLines.add("-");
        }
        appendLines(cmd, "#ActiveSkillsList", activeLines, "#dddddd");

        List<String> passiveLines = new ArrayList<>();
        classType.passiveSkills().forEach(passive -> passiveLines.add(passive.name() + ": " + passive.description()));
        if (passiveLines.isEmpty()) {
            passiveLines.add("-");
        }
        appendLines(cmd, "#PassiveSkillsList", passiveLines, "#cccccc");

        ClassDefinitionsConfig.ClassDefinition def = classDefinitions != null
                ? classDefinitions.getDefinition(classType.id())
                : null;
        List<String> strengths = def != null && def.strengths != null ? def.strengths : List.of();
        List<String> weaknesses = def != null && def.weaknesses != null ? def.weaknesses : List.of();
        if (strengths.isEmpty()) strengths = List.of("-");
        if (weaknesses.isEmpty()) weaknesses = List.of("-");
        appendLines(cmd, "#StrengthsList", strengths, "#88ff88");
        appendLines(cmd, "#WeaknessesList", weaknesses, "#ff8888");
    }

    private static String formatActiveSkill(String slot, ActiveSkill skill) {
        String name = skill.name() != null ? skill.name() : skill.id();
        long cd = Math.max(0L, skill.cooldownSeconds());
        String desc = skill.description() != null ? skill.description() : "";
        if (!desc.isBlank()) {
            return slot + ": " + name + " (" + cd + "s) - " + desc;
        }
        return slot + ": " + name + " (" + cd + "s)";
    }

    private static void appendLines(UICommandBuilder cmd, String containerId, List<String> lines, String color) {
        for (String line : lines) {
            String safe = escapeUiText(line);
            cmd.appendInline(containerId, String.format(
                    "Label { Text: \"%s\"; Style: (FontSize: 12, TextColor: %s); Padding: (Bottom: 4); }",
                    safe, color));
        }
    }

    private String resolveClassName(ClassType classType) {
        String key = "class." + classType.id() + ".name";
        if (T.has(key)) {
            return T.t(key);
        }
        if (classDefinitions != null) {
            ClassDefinitionsConfig.ClassDefinition def = classDefinitions.getDefinition(classType.id());
            if (def != null && def.name != null && !def.name.isBlank()) {
                return def.name;
            }
        }
        return classType.displayName();
    }

    private static String escapeUiText(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ");
    }
}
