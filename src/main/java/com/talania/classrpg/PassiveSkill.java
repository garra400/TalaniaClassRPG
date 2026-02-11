package com.talania.classrpg;

import java.util.Collections;
import java.util.List;

/**
 * Definition for a passive skill. Effects are described and may require
 * combat integration to activate.
 */
public record PassiveSkill(
        String id,
        String name,
        String description,
        List<String> notes
) {
    public PassiveSkill {
        notes = notes == null ? Collections.emptyList() : List.copyOf(notes);
    }
}
