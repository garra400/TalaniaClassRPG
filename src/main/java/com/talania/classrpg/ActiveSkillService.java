package com.talania.classrpg;

import com.talania.core.abilities.AbilityCooldownService;
import com.talania.core.events.EventBus;
import com.talania.core.input.InputActionEvent;

import java.util.UUID;

/**
 * Handles active skill triggers from input events.
 */
public final class ActiveSkillService {
    private final ClassService classService;
    private final AbilityCooldownService cooldownService;

    public ActiveSkillService(ClassService classService, AbilityCooldownService cooldownService) {
        this.classService = classService;
        this.cooldownService = cooldownService == null ? new AbilityCooldownService() : cooldownService;
    }

    public void handleInputAction(InputActionEvent event) {
        if (event == null) {
            return;
        }
        UUID playerId = event.playerId();
        if (playerId == null) {
            return;
        }
        ClassType classType = classService == null ? null : classService.getAssigned(playerId);
        if (classType == null) {
            return;
        }
        SkillSlot slot = SkillSlot.fromInputAction(event.action());
        if (slot == null) {
            return;
        }
        ActiveSkill skill = classType.getActiveSkill(slot);
        if (skill == null) {
            return;
        }

        boolean activated;
        if (event.ref() != null && event.store() != null) {
            activated = cooldownService.tryActivate(playerId, skill.id(), skill.cooldownMs(), event.ref(), event.store());
        } else {
            activated = cooldownService.tryActivate(playerId, skill.id(), skill.cooldownMs(), null, null, null);
        }
        if (!activated) {
            return;
        }

        EventBus.publish(new ActiveSkillEvent(playerId, classType, skill, event.timestamp(), event.ref(), event.store()));
    }
}
