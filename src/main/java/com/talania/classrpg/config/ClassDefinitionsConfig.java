package com.talania.classrpg.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional class metadata definitions for UI display.
 */
public final class ClassDefinitionsConfig {
    public List<ClassDefinition> classes = new ArrayList<>();

    public void ensureDefaults() {
        if (classes == null) {
            classes = new ArrayList<>();
        }
    }

    public ClassDefinition getDefinition(String classId) {
        if (classId == null || classes == null) {
            return null;
        }
        for (ClassDefinition def : classes) {
            if (def != null && def.id != null && def.id.equalsIgnoreCase(classId)) {
                return def;
            }
        }
        return null;
    }

    public static final class ClassDefinition {
        public String id;
        public String name;
        public String tagline;
        public List<String> strengths;
        public List<String> weaknesses;
    }
}
