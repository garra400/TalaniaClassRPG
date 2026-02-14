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
import com.talania.core.localization.T;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class selection UI page for TalaniaClassRPG.
 * Inspired by Orbis_and_Dungeons UI system.
 */
public class ClassSelectionPage extends InteractiveCustomUIPage<ClassSelectionPage.ClassEventData> {

    public static class ClassEventData {
        public String action;
        public String classId;

        public static final BuilderCodec<ClassEventData> CODEC =
                BuilderCodec.builder(ClassEventData.class, ClassEventData::new)
                        .append(
                                new KeyedCodec<>("Action", Codec.STRING),
                                (ClassEventData o, String v) -> o.action = v,
                                (ClassEventData o) -> o.action
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("ClassId", Codec.STRING),
                                (ClassEventData o, String v) -> o.classId = v,
                                (ClassEventData o) -> o.classId
                        )
                        .add()
                        .build();
    }

    private static final int CLASSES_PER_PAGE = 4;

    private final ClassService classService;
    private final ClassDefinitionsConfig classDefinitions;
    private final List<ClassType> allClasses;
    private final String selectedClassId;
    private final int currentPage;

    public ClassSelectionPage(@Nonnull PlayerRef playerRef,
                              ClassService classService,
                              ClassDefinitionsConfig classDefinitions,
                              String selectedClassId,
                              int page) {
        super(playerRef, CustomPageLifetime.CantClose, ClassEventData.CODEC);
        this.classService = classService;
        this.classDefinitions = classDefinitions;
        this.selectedClassId = selectedClassId;
        this.currentPage = Math.max(0, page);
        this.allClasses = Arrays.asList(ClassType.values());
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder evt,
                      @Nonnull Store<EntityStore> store) {
        cmd.append("Pages/class_selection.ui");

        cmd.set("#TitleText.Text", T.t("ui.class_selection.title"));
        cmd.set("#StrengthsHeader.Text", T.t("ui.class_selection.strengths"));
        cmd.set("#WeaknessesHeader.Text", T.t("ui.class_selection.weaknesses"));
        cmd.set("#ConfirmSelection.Text", T.t("ui.class_selection.confirm"));
        cmd.set("#BackToRace.Text", T.t("ui.class_selection.back"));
        cmd.set("#PrevPageButton.Text", T.t("ui.class_selection.previous"));
        cmd.set("#NextPageButton.Text", T.t("ui.class_selection.next"));

        String effectiveClassId = resolveSelectedClassId();
        applyClassToUI(cmd, effectiveClassId);
        buildClassButtons(cmd, evt, effectiveClassId);

        int totalPages = Math.max(1, (allClasses.size() + CLASSES_PER_PAGE - 1) / CLASSES_PER_PAGE);
        String pageLabel = T.t("ui.page");
        cmd.set("#PageInfo.Text", pageLabel + " " + (currentPage + 1) + " / " + totalPages);
        cmd.set("#PrevPageButton.Visible", currentPage > 0);
        cmd.set("#NextPageButton.Visible", currentPage < totalPages - 1);

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#PrevPageButton",
                new EventData().append("Action", "prevpage"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#NextPageButton",
                new EventData().append("Action", "nextpage"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#ConfirmSelection",
                new EventData().append("Action", "confirm"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#BackToRace",
                new EventData().append("Action", "back"));
    }

    private void buildClassButtons(UICommandBuilder cmd, UIEventBuilder evt, String effectiveClassId) {
        cmd.clear("#ClassListPanel");
        cmd.appendInline("#ClassListPanel", "Group #ClassButtons { LayoutMode: Top; }");

        int start = currentPage * CLASSES_PER_PAGE;
        int end = Math.min(start + CLASSES_PER_PAGE, allClasses.size());

        for (int i = start; i < end; i++) {
            ClassType classType = allClasses.get(i);
            String classId = classType.id();
            ClassDefinitionsConfig.ClassDefinition definition = definitionFor(classType);
            String className = resolveClassName(classType, definition);
            String classTagline = resolveClassTagline(classType, definition);

            int btnIndex = i - start;
            String buttonId = "#ClassButton" + btnIndex;
            String selectedStyle = classId.equalsIgnoreCase(effectiveClassId)
                    ? "Style: $C.@SelectionCardStyleSelected;"
                    : "Style: $C.@SelectionCardStyle;";

            cmd.appendInline("#ClassButtons", String.format("""
                    Button %s {
                      Anchor: (Height: 60);
                      LayoutMode: Top;
                      Padding: (Full: 8);
                      %s
                      Label {
                        Text: "%s";
                        Anchor: (Height: 20);
                        Style: (FontSize: 14, RenderBold: true, TextColor: #ffffff);
                      }
                      Label {
                        Text: "%s";
                        Anchor: (Height: 16);
                        Style: (FontSize: 11, TextColor: #aaaaaa);
                      }
                    }
                    """, buttonId, selectedStyle, className.toUpperCase(), classTagline));

            if (i < end - 1) {
                cmd.appendInline("#ClassButtons", "Group { Anchor: (Height: 6); }");
            }

            evt.addEventBinding(CustomUIEventBindingType.Activating,
                    buttonId,
                    new EventData().append("Action", "select").append("ClassId", classId));
        }
    }

    private void applyClassToUI(UICommandBuilder cmd, String classId) {
        ClassType classType = classId == null ? null : ClassType.fromId(classId);
        if (classType == null && !allClasses.isEmpty()) {
            classType = allClasses.get(0);
        }
        if (classType == null) {
            return;
        }

        ClassDefinitionsConfig.ClassDefinition definition = definitionFor(classType);
        String className = resolveClassName(classType, definition);
        String classTagline = resolveClassTagline(classType, definition);
        cmd.set("#SelectedClassName.Text", className);
        cmd.set("#SelectedClassTagline.Text", classTagline);

        List<String> strengths = resolveStrengths(classType, definition);
        List<String> weaknesses = resolveWeaknesses(definition);

        for (int i = 0; i < 3; i++) {
            String text = i < strengths.size() ? "- " + strengths.get(i) : "";
            cmd.set("#PositiveLine" + (i + 1) + ".Text", text);
        }

        for (int i = 0; i < 2; i++) {
            String text = i < weaknesses.size() ? "- " + weaknesses.get(i) : "";
            cmd.set("#NegativeLine" + (i + 1) + ".Text", text);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull ClassEventData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || data == null || data.action == null) {
            return;
        }

        if ("select".equals(data.action)) {
            if (data.classId != null && ClassType.fromId(data.classId) != null) {
                player.getPageManager().openCustomPage(ref, store,
                        new ClassSelectionPage(playerRef, classService, classDefinitions, data.classId, currentPage));
            }
            return;
        }

        if ("prevpage".equals(data.action)) {
            if (currentPage > 0) {
                player.getPageManager().openCustomPage(ref, store,
                        new ClassSelectionPage(playerRef, classService, classDefinitions, selectedClassId, currentPage - 1));
            }
            return;
        }

        if ("nextpage".equals(data.action)) {
            int totalPages = Math.max(1, (allClasses.size() + CLASSES_PER_PAGE - 1) / CLASSES_PER_PAGE);
            if (currentPage < totalPages - 1) {
                player.getPageManager().openCustomPage(ref, store,
                        new ClassSelectionPage(playerRef, classService, classDefinitions, selectedClassId, currentPage + 1));
            }
            return;
        }

        if ("back".equals(data.action)) {
            this.close();
            return;
        }

        if ("confirm".equals(data.action)) {
            applySelection();
            this.close();
        }
    }

    private void applySelection() {
        if (classService == null) {
            return;
        }
        String effectiveClassId = resolveSelectedClassId();
        ClassType classType = ClassType.fromId(effectiveClassId);
        if (classType == null) {
            return;
        }
        classService.setAssigned(playerRef.getUuid(), classType);
    }

    private String resolveSelectedClassId() {
        if (selectedClassId != null && ClassType.fromId(selectedClassId) != null) {
            return selectedClassId;
        }
        if (!allClasses.isEmpty()) {
            return allClasses.get(0).id();
        }
        return null;
    }

    private ClassDefinitionsConfig.ClassDefinition definitionFor(ClassType classType) {
        if (classDefinitions == null || classType == null) {
            return null;
        }
        return classDefinitions.getDefinition(classType.id());
    }

    private static String resolveClassName(ClassType classType, ClassDefinitionsConfig.ClassDefinition definition) {
        String key = "class." + classType.id() + ".name";
        if (T.has(key)) {
            return T.t(key);
        }
        if (definition != null && definition.name != null && !definition.name.isBlank()) {
            return definition.name;
        }
        return classType.displayName();
    }

    private static String resolveClassTagline(ClassType classType, ClassDefinitionsConfig.ClassDefinition definition) {
        String key = "class." + classType.id() + ".tagline";
        if (T.has(key)) {
            return T.t(key);
        }
        if (definition != null && definition.tagline != null && !definition.tagline.isBlank()) {
            return definition.tagline;
        }
        return classType.gameplay() == null ? "" : classType.gameplay();
    }

    private static List<String> resolveStrengths(ClassType classType,
                                                 ClassDefinitionsConfig.ClassDefinition definition) {
        if (definition != null && definition.strengths != null && !definition.strengths.isEmpty()) {
            return definition.strengths;
        }
        List<String> strengths = new ArrayList<>();
        ActiveSkill eSkill = classType.getActiveSkill(SkillSlot.E);
        ActiveSkill rSkill = classType.getActiveSkill(SkillSlot.R);
        if (eSkill != null) {
            strengths.add("E: " + eSkill.name());
        }
        if (rSkill != null) {
            strengths.add("R: " + rSkill.name());
        }
        classType.passiveSkills().stream()
                .limit(Math.max(0, 3L - strengths.size()))
                .forEach(passive -> strengths.add(passive.name()));
        if (strengths.isEmpty() && classType.gameplay() != null && !classType.gameplay().isBlank()) {
            strengths.add(classType.gameplay());
        }
        return strengths;
    }

    private static List<String> resolveWeaknesses(ClassDefinitionsConfig.ClassDefinition definition) {
        if (definition != null && definition.weaknesses != null) {
            return definition.weaknesses;
        }
        return List.of();
    }
}
