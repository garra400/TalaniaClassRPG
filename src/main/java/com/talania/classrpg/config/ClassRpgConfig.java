package com.talania.classrpg.config;

/**
 * Configuration for class RPG progression and skill balance.
 */
public class ClassRpgConfig {
    public int maxLevel = 100;
    public long baseXp = 100L;
    public long stepXp = 25L;
    public CombatXp combatXp = new CombatXp();
    public Skills skills = new Skills();

    public void ensureDefaults() {
        if (combatXp == null) {
            combatXp = new CombatXp();
        }
        if (skills == null) {
            skills = new Skills();
        }
        skills.ensureDefaults();
    }

    public static final class CombatXp {
        public long hitXp = 2L;
        public float damageToXp = 0.25f;
        public long maxHitXp = 25L;
        public long killXpNpc = 40L;
        public long killXpPlayer = 80L;
        public boolean awardOnNpc = true;
        public boolean awardOnPlayer = true;
    }

    public static final class Skills {
        public BladeBarrier bladeBarrier = new BladeBarrier();
        public ValdorsVerdict valdorsVerdict = new ValdorsVerdict();
        public EvasiveShot evasiveShot = new EvasiveShot();
        public RainOfVengeance rainOfVengeance = new RainOfVengeance();
        public ShadowStep shadowStep = new ShadowStep();
        public PhantomEdge phantomEdge = new PhantomEdge();
        public PrimalRoar primalRoar = new PrimalRoar();
        public UndyingRage undyingRage = new UndyingRage();
        public Radiance radiance = new Radiance();
        public Dawnbreaker dawnbreaker = new Dawnbreaker();
        public VoidRift voidRift = new VoidRift();
        public ArcaneTorrent arcaneTorrent = new ArcaneTorrent();
        public Flashbang flashbang = new Flashbang();
        public LeadStorm leadStorm = new LeadStorm();
        public Whirlwind whirlwind = new Whirlwind();
        public Thunderfall thunderfall = new Thunderfall();

        private void ensureDefaults() {
            if (bladeBarrier == null) bladeBarrier = new BladeBarrier();
            if (valdorsVerdict == null) valdorsVerdict = new ValdorsVerdict();
            if (evasiveShot == null) evasiveShot = new EvasiveShot();
            if (rainOfVengeance == null) rainOfVengeance = new RainOfVengeance();
            if (shadowStep == null) shadowStep = new ShadowStep();
            if (phantomEdge == null) phantomEdge = new PhantomEdge();
            if (primalRoar == null) primalRoar = new PrimalRoar();
            if (undyingRage == null) undyingRage = new UndyingRage();
            if (radiance == null) radiance = new Radiance();
            if (dawnbreaker == null) dawnbreaker = new Dawnbreaker();
            if (voidRift == null) voidRift = new VoidRift();
            if (arcaneTorrent == null) arcaneTorrent = new ArcaneTorrent();
            if (flashbang == null) flashbang = new Flashbang();
            if (leadStorm == null) leadStorm = new LeadStorm();
            if (whirlwind == null) whirlwind = new Whirlwind();
            if (thunderfall == null) thunderfall = new Thunderfall();
        }
    }

    public static final class BladeBarrier {
        public float attackSpeedMultiplier = 1.30f;
        public float armorAdd = 1.0f;
        public long durationMs = 3_000L;
    }

    public static final class ValdorsVerdict {
        public float damage = 18.0f;
        public double radius = 6.0;
        public float coneDegrees = 60.0f;
        public double knockback = 2.5;
    }

    public static final class EvasiveShot {
        public float backwardForce = 3.5f;
        public float upwardForce = 0.8f;
        public int arrowCount = 3;
        public float spreadDegrees = 10.0f;
        public String projectileId = "Arrow_FullCharge";
    }

    public static final class RainOfVengeance {
        public double targetRange = 18.0;
        public double fallbackForward = 12.0;
        public double radius = 4.0;
        public int arrows = 12;
        public long delayMs = 1_000L;
        public float slowMultiplier = 0.6f;
        public long slowDurationMs = 3_000L;
        public String slowEffectId = "";
        public long slowEffectDurationMs = 0L;
    }

    public static final class ShadowStep {
        public double blinkDistance = 8.0;
        public float armorAdd = 1.0f;
        public long armorDurationMs = 600L;
        public long invulnerableMs = 600L;
    }

    public static final class PhantomEdge {
        public long durationMs = 5_000L;
        public float speedMultiplier = 1.3f;
        public float executeThreshold = 0.30f;
        public float executeMultiplier = 2.5f;
        public String invisibilityEffectId = "";
        public long invisibilityDurationMs = 0L;
    }

    public static final class PrimalRoar {
        public double radius = 4.0;
        public long stunDurationMs = 1_000L;
        public float attackSpeedMultiplier = 1.25f;
        public long buffDurationMs = 5_000L;
        public String stunEffectId = "";
        public long stunEffectDurationMs = 0L;
    }

    public static final class UndyingRage {
        public long durationMs = 6_000L;
        public float armorAdd = 1.0f;
        public float attackSpeedMultiplier = 1.3f;
        public float moveSpeedMultiplier = 1.25f;
        public float healRatio = 0.25f;
        public boolean invulnerable = true;
    }

    public static final class Radiance {
        public long durationMs = 4_000L;
        public double radius = 4.0;
        public float healRatio = 0.05f;
        public float slowMultiplier = 0.7f;
        public long slowDurationMs = 3_000L;
        public String slowEffectId = "";
        public long slowEffectDurationMs = 0L;
    }

    public static final class Dawnbreaker {
        public double targetRange = 20.0;
        public double radius = 5.0;
        public float damage = 25.0f;
        public float shieldArmorAdd = 0.25f;
        public long shieldDurationMs = 4_000L;
        public String blindEffectId = "";
        public long blindDurationMs = 2_500L;
    }

    public static final class VoidRift {
        public double targetRange = 16.0;
        public double radius = 4.0;
        public double strength = 0.8;
        public long durationMs = 1_800L;
        public long tickMs = 100L;
    }

    public static final class ArcaneTorrent {
        public double targetRange = 18.0;
        public double fallbackForward = 12.0;
        public String projectileId = "Arrow_FullCharge";
        public long durationMs = 3_000L;
        public long tickMs = 200L;
        public float armorAdd = 1.0f;
        public long armorDurationMs = 3_000L;
        public boolean invulnerable = true;
    }

    public static final class Flashbang {
        public double targetRange = 12.0;
        public double radius = 3.5;
        public long stunDurationMs = 1_200L;
        public float armorMultiplier = 0.7f;
        public long armorDurationMs = 4_000L;
        public String blindEffectId = "";
        public long blindDurationMs = 2_000L;
    }

    public static final class LeadStorm {
        public double radius = 4.0;
        public float damage = 4.0f;
        public long durationMs = 3_000L;
        public long tickMs = 250L;
    }

    public static final class Whirlwind {
        public double radius = 3.5;
        public double knockback = 2.8;
        public float dodgeBonus = 0.35f;
        public long durationMs = 3_000L;
    }

    public static final class Thunderfall {
        public double targetRange = 18.0;
        public double radius = 4.0;
        public float damage = 20.0f;
    }
}
