package com.talania.classrpg;

/**
 * Definition for an active skill bound to an action slot.
 */
public record ActiveSkill(
        String id,
        String name,
        String description,
        SkillSlot slot,
        long cooldownMs
) {
    public long cooldownSeconds() {
        return cooldownMs / 1000L;
    }
}
