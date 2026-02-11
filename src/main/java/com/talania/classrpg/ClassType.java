package com.talania.classrpg;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Canonical class definitions based on .docx design docs.
 */
public enum ClassType {
    SWORDMASTER(
            "swordmaster",
            "Swordmaster",
            "Valdor",
            "Balanced and controlled",
            activeSkills(
                    skill("swordmaster_blade_barrier", "Blade Barrier",
                            "Increases attack speed by 30% for 3 seconds and blocks incoming frontal damage.",
                            SkillSlot.E, seconds(15)),
                    skill("swordmaster_valdors_verdict", "Valdor's Verdict",
                            "Charge the sword and unleash a wide wave that knocks enemies back.",
                            SkillSlot.R, seconds(55))
            ),
            List.of(
                    passive("swordmaster_counter_guard", "Counter-Guard",
                            "After a successful block, the next hit deals 30% more damage.",
                            List.of("Requires block detection.")),
                    passive("swordmaster_duelist", "Duelist",
                            "When fighting 1v1, sword attacks are 15% faster and armor penetration increases.",
                            List.of("Requires nearby-enemy checks and armor penetration support."))
            )
    ),
    ARCHER(
            "archer",
            "Archer",
            "Sylvaris",
            "Long-range and precise",
            activeSkills(
                    skill("archer_evasive_shot", "Evasive Shot",
                            "Backflip and fire 3 arrows in a forward fan. Knocks enemies back.",
                            SkillSlot.E, seconds(9)),
                    skill("archer_rain_of_vengeance", "Rain of Vengeance",
                            "After 1 second, arrows rain on a marked area, damaging and slowing by 40%.",
                            SkillSlot.R, seconds(60))
            ),
            List.of(
                    passive("archer_longshot", "Longshot",
                            "Arrow damage increases by 1% per block traveled.",
                            List.of("Requires distance tracking on projectiles.")),
                    passive("archer_hawkeye", "Hawkeye",
                            "Fully drawn bows slow movement, but headshots deal +60% critical damage.",
                            List.of("Requires headshot detection and draw state."))
            )
    ),
    ASSASSIN(
            "assassin",
            "Assassin",
            "Nocturne",
            "Fast and opportunistic",
            activeSkills(
                    skill("assassin_shadow_step", "Shadow Step",
                            "Dash 8 blocks forward, passing through enemies and taking no damage during the dash.",
                            SkillSlot.E, seconds(7)),
                    skill("assassin_phantom_edge", "Phantom Edge",
                            "Become invisible and gain speed for 5 seconds. First strike executes targets below 30% HP.",
                            SkillSlot.R, seconds(90))
            ),
            List.of(
                    passive("assassin_backstab", "Backstab",
                            "Hits from behind always crit for 150%-200% damage.",
                            List.of("Requires attack angle checks.")),
                    passive("assassin_venomous", "Venomous",
                            "Every dagger hit stacks. After 3 consecutive hits, poison for 5 seconds.",
                            List.of("Requires hit streak tracking and poison application."))
            )
    ),
    BERSERKER(
            "berserker",
            "Berserker",
            "Gor'thul",
            "Risky and savage",
            activeSkills(
                    skill("berserker_primal_roar", "Primal Roar",
                            "Stun nearby enemies for 1 second and increase attack speed for 5 seconds.",
                            SkillSlot.E, seconds(16)),
                    skill("berserker_undying_rage", "Undying Rage",
                            "Become immortal for 6 seconds. Attack and movement speed increase. Heal for a portion of damage dealt when it ends.",
                            SkillSlot.R, seconds(120))
            ),
            List.of(
                    passive("berserker_bloodlust", "Bloodlust",
                            "Damage increases by half the missing HP percentage.",
                            List.of("Requires current HP percentage.")),
                    passive("berserker_executioner", "Executioner",
                            "Deal double damage to enemies below 30% HP.",
                            List.of("Requires target HP checks."))
            )
    ),
    PALADIN(
            "paladin",
            "Paladin",
            "Solara",
            "Holy and protective",
            activeSkills(
                    skill("paladin_radiance", "Radiance",
                            "Plunge the sword into the ground, healing allies and slowing enemies nearby.",
                            SkillSlot.E, seconds(18)),
                    skill("paladin_dawnbreaker", "Dawnbreaker",
                            "Call down a pillar of sunlight that blinds and burns enemies, granting shields to allies.",
                            SkillSlot.R, seconds(100))
            ),
            Collections.emptyList()
    ),
    MAGE(
            "mage",
            "Mage",
            "Arcanis",
            "Arcane and controlling",
            activeSkills(
                    skill("mage_void_rift", "Void Rift",
                            "Open a small rift that pulls enemies toward the center.",
                            SkillSlot.E, seconds(14)),
                    skill("mage_arcane_torrent", "Arcane Torrent",
                            "Levitates for 3 seconds while raining magic missiles. Invulnerable during the cast.",
                            SkillSlot.R, seconds(75))
            ),
            Collections.emptyList()
    ),
    GUNSLINGER(
            "gunslinger",
            "Gunslinger",
            "Zentheros",
            "Explosive and relentless",
            activeSkills(
                    skill("gunslinger_flashbang", "Flashbang",
                            "Throw a bomb that stuns enemies and breaks armor for increased damage.",
                            SkillSlot.E, seconds(12)),
                    skill("gunslinger_lead_storm", "Lead Storm",
                            "Spin and fire in all directions, damaging nearby enemies.",
                            SkillSlot.R, seconds(50))
            ),
            Collections.emptyList()
    ),
    SPEARMAN(
            "spearman",
            "Spearman",
            "Halcyon",
            "Agile and steadfast",
            activeSkills(
                    skill("spearman_whirlwind", "Whirlwind",
                            "Spin the spear to knock back enemies and deflect projectiles.",
                            SkillSlot.E, seconds(13)),
                    skill("spearman_thunderfall", "Thunderfall",
                            "Throw the spear, call lightning where it lands, and teleport to it.",
                            SkillSlot.R, seconds(65))
            ),
            Collections.emptyList()
    ),
    MACE_BEARER(
            "mace_bearer",
            "Mace Bearer",
            "Unknown",
            "Heavy and armor-piercing",
            Collections.emptyMap(),
            List.of(
                    passive("mace_armor_breaker", "Armor Breaker",
                            "Mace hits ignore 25% of the opponent's armor.",
                            List.of("Requires armor penetration support.")),
                    passive("mace_splash_damage", "Splash Damage",
                            "Normal attacks deal 30% splash damage within 2 blocks of the target.",
                            List.of("Requires area damage handling."))
            )
    );

    private final String id;
    private final String displayName;
    private final String deity;
    private final String gameplay;
    private final Map<SkillSlot, ActiveSkill> activeSkills;
    private final List<PassiveSkill> passiveSkills;

    ClassType(String id,
              String displayName,
              String deity,
              String gameplay,
              Map<SkillSlot, ActiveSkill> activeSkills,
              List<PassiveSkill> passiveSkills) {
        this.id = id;
        this.displayName = displayName;
        this.deity = deity;
        this.gameplay = gameplay;
        this.activeSkills = activeSkills == null ? Collections.emptyMap() : Collections.unmodifiableMap(activeSkills);
        this.passiveSkills = passiveSkills == null ? Collections.emptyList() : List.copyOf(passiveSkills);
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String deity() {
        return deity;
    }

    public String gameplay() {
        return gameplay;
    }

    public ActiveSkill getActiveSkill(SkillSlot slot) {
        return slot == null ? null : activeSkills.get(slot);
    }

    public Map<SkillSlot, ActiveSkill> activeSkills() {
        return activeSkills;
    }

    public List<PassiveSkill> passiveSkills() {
        return passiveSkills;
    }

    public static ClassType fromId(String id) {
        if (id == null) {
            return null;
        }
        String normalized = id.trim().toLowerCase();
        if (normalized.equals("swordsman") || normalized.equals("swordman")) {
            normalized = "swordmaster";
        }
        if (normalized.equals("macebearer") || normalized.equals("mace bearer")) {
            normalized = "mace_bearer";
        }
        for (ClassType type : values()) {
            if (type.id.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return null;
    }

    private static Map<SkillSlot, ActiveSkill> activeSkills(ActiveSkill eSkill, ActiveSkill rSkill) {
        Map<SkillSlot, ActiveSkill> map = new EnumMap<>(SkillSlot.class);
        if (eSkill != null) {
            map.put(SkillSlot.E, eSkill);
        }
        if (rSkill != null) {
            map.put(SkillSlot.R, rSkill);
        }
        return map;
    }

    private static ActiveSkill skill(String id, String name, String description, SkillSlot slot, long cooldownMs) {
        return new ActiveSkill(id, name, description, slot, cooldownMs);
    }

    private static PassiveSkill passive(String id, String name, String description, List<String> notes) {
        return new PassiveSkill(id, name, description, notes);
    }

    private static long seconds(long seconds) {
        return seconds * 1000L;
    }
}
