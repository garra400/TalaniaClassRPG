package com.talania.classrpg.ui;

import com.talania.classrpg.ClassType;
import com.talania.core.localization.T;
import com.talania.core.profile.TalaniaPlayerProfile;
import com.talania.core.profile.TalaniaProfileRuntime;
import com.talania.core.ui.UIFactory;
import com.talania.core.ui.UIFactory.UIComponent;
import com.talania.core.ui.UIFactory.ButtonBuilder;
import com.talania.core.ui.UIFactory.LabelBuilder;
import com.talania.core.ui.UIFactory.PanelBuilder;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.List;
import java.util.UUID;

/**
 * Class selection UI page for TalaniaClassRPG.
 * Inspired by Orbis_and_Dungeons UI system.
 *
 * Features:
 * - Paginated class list
 * - Class preview (name, tagline, strengths, weaknesses)
 * - Confirm and back buttons
 * - Translatable labels
 */
public class ClassSelectionPage {
    private final PlayerRef playerRef;
    private final List<ClassType> classList;
    private final int currentPage;
    private final String selectedClassId;
    private static final int CLASSES_PER_PAGE = 4;

    public ClassSelectionPage(PlayerRef playerRef, List<ClassType> classList, String selectedClassId, int currentPage) {
        this.playerRef = playerRef;
        this.classList = classList;
        this.selectedClassId = selectedClassId;
        this.currentPage = currentPage;
    }

    /**
     * Builds the UI for the class selection page.
     */
    public UIComponent build() {
        PanelBuilder root = UIFactory.panel()
            .size(950, 650)
            .backgroundColor("#1a1a1a");

        // Title
        root.add(UIFactory.label()
            .text(T.t("ui.class_selection.title"))
            .position(20, 20)
            .fontSize(24)
            .color("#ffffff")
            .build());

        // Class list panel (left)
        PanelBuilder classListPanel = UIFactory.panel()
            .position(20, 60)
            .size(300, 500)
            .backgroundColor("#0f0f0f");

        int start = currentPage * CLASSES_PER_PAGE;
        int end = Math.min(start + CLASSES_PER_PAGE, classList.size());
        for (int i = start; i < end; i++) {
            ClassType classType = classList.get(i);
            ButtonBuilder btn = UIFactory.button()
                .text(T.t("class." + classType.name().toLowerCase() + ".name"))
                .onClick(() -> onClassSelected(classType.name().toLowerCase()));
            classListPanel.add(btn.build());
        }

        root.add(classListPanel.build());

        // Class detail panel (right)
        PanelBuilder detailPanel = UIFactory.panel()
            .position(340, 60)
            .size(570, 500)
            .backgroundColor("#0f0f0f");

        // TODO: Add class preview (name, tagline, strengths, weaknesses)
        // ...

        root.add(detailPanel.build());

        // Navigation buttons
        root.add(UIFactory.button()
            .text(T.t("ui.class_selection.back"))
            .position(20, 580)
            .size(120, 40)
            .onClick(this::onBack)
            .build());
        root.add(UIFactory.button()
            .text(T.t("ui.class_selection.confirm"))
            .position(800, 580)
            .size(120, 40)
            .onClick(this::onConfirm)
            .build());

        // Pagination
        if (currentPage > 0) {
            root.add(UIFactory.button()
                .text("<")
                .position(150, 580)
                .size(40, 40)
                .onClick(this::onPrevPage)
                .build());
        }
        if ((currentPage + 1) * CLASSES_PER_PAGE < classList.size()) {
            root.add(UIFactory.button()
                .text(">")
                .position(200, 580)
                .size(40, 40)
                .onClick(this::onNextPage)
                .build());
        }

        return root.build();
    }

    private void onClassSelected(String classId) {
        // TODO: Handle class selection (update preview, etc.)
    }

    private void onBack() {
        // TODO: Return to previous page (e.g., race selection)
    }

    private void onConfirm() {
        // TODO: Confirm class selection and apply to player profile
    }

    private void onPrevPage() {
        // TODO: Go to previous page
    }

    private void onNextPage() {
        // TODO: Go to next page
    }
}
