package com.talania.classrpg;

import com.talania.core.input.InputAction;

/**
 * Active skill slots mapped to action keys.
 */
public enum SkillSlot {
    E,
    R;

    public static SkillSlot fromInputAction(InputAction action) {
        if (action == null) {
            return null;
        }
        return switch (action) {
            case E -> E;
            case R -> R;
        };
    }
}
