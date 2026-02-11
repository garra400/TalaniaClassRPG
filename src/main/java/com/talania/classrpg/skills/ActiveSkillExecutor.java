package com.talania.classrpg.skills;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.talania.classrpg.ActiveSkill;
import com.talania.classrpg.ActiveSkillEvent;
import com.talania.classrpg.config.ClassRpgConfig;
import com.talania.classrpg.effects.ArcaneTorrentEffect;
import com.talania.classrpg.effects.LeadStormEffect;
import com.talania.classrpg.effects.MovementFreezeEffect;
import com.talania.classrpg.effects.MovementModifierEffect;
import com.talania.classrpg.effects.PullEffect;
import com.talania.classrpg.effects.RadianceEffect;
import com.talania.classrpg.effects.RainOfVengeanceEffect;
import com.talania.classrpg.effects.TimedFlightEffect;
import com.talania.classrpg.effects.TimedInvulnerabilityEffect;
import com.talania.classrpg.effects.TimedStatModifierEffect;
import com.talania.classrpg.state.ClassRpgState;
import com.talania.core.combat.targeting.AreaOfEffect;
import com.talania.core.combat.utils.AreaDamage;
import com.talania.core.entities.EntityAnimationManager;
import com.talania.core.hytale.effects.EntityEffectService;
import com.talania.core.hytale.teleport.TeleportUtil;
import com.talania.core.stats.StatModifier;
import com.talania.core.stats.StatType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;

import java.util.List;

/**
 * Executes active skill effects.
 */
public final class ActiveSkillExecutor {
    private static final String PROJECTILE_ARROW = "Arrow_FullCharge";

    private final ClassRpgState state;
    private final ClassRpgConfig.Skills skills;

    public ActiveSkillExecutor(ClassRpgState state, ClassRpgConfig.Skills skills) {
        this.state = state;
        this.skills = skills == null ? new ClassRpgConfig.Skills() : skills;
    }

    public void handle(ActiveSkillEvent event) {
        if (event == null) {
            return;
        }
        ActiveSkill skill = event.skill();
        if (skill == null) {
            return;
        }
        Ref<EntityStore> ref = event.ref();
        Store<EntityStore> store = event.store();
        if (ref == null || store == null || !ref.isValid()) {
            return;
        }
        String id = skill.id();
        long now = System.currentTimeMillis();

        switch (id) {
            case "swordmaster_blade_barrier" -> applyBladeBarrier(event.playerId(), store, now);
            case "swordmaster_valdors_verdict" -> applyValdorsVerdict(ref, store);
            case "archer_evasive_shot" -> applyEvasiveShot(ref, store);
            case "archer_rain_of_vengeance" -> applyRainOfVengeance(ref, store);
            case "assassin_shadow_step" -> applyShadowStep(ref, store, event.playerId(), now);
            case "assassin_phantom_edge" -> applyPhantomEdge(ref, store, event.playerId(), now);
            case "berserker_primal_roar" -> applyPrimalRoar(ref, store, event.playerId(), now);
            case "berserker_undying_rage" -> applyUndyingRage(ref, store, event.playerId(), now);
            case "paladin_radiance" -> applyRadiance(ref, store, now);
            case "paladin_dawnbreaker" -> applyDawnbreaker(ref, store, now);
            case "mage_void_rift" -> applyVoidRift(ref, store, now);
            case "mage_arcane_torrent" -> applyArcaneTorrent(ref, store, event.playerId(), now);
            case "gunslinger_flashbang" -> applyFlashbang(ref, store, now);
            case "gunslinger_lead_storm" -> applyLeadStorm(ref, store, now);
            case "spearman_whirlwind" -> applyWhirlwind(ref, store, event.playerId(), now);
            case "spearman_thunderfall" -> applyThunderfall(ref, store);
            default -> {
                // no-op
            }
        }
    }

    private void applyBladeBarrier(java.util.UUID playerId, Store<EntityStore> store, long now) {
        ClassRpgConfig.BladeBarrier config = skills.bladeBarrier;
        List<StatModifier> modifiers = List.of(
                StatModifier.temporary("skill:blade_barrier", StatType.ATTACK_SPEED,
                        config.attackSpeedMultiplier, StatModifier.Operation.MULTIPLY_TOTAL),
                StatModifier.temporary("skill:blade_barrier", StatType.ARMOR,
                        config.armorAdd, StatModifier.Operation.ADD)
        );
        EntityAnimationManager.get().add(new TimedStatModifierEffect(playerId, modifiers, config.durationMs), store, now);
    }

    private void applyValdorsVerdict(Ref<EntityStore> ref, Store<EntityStore> store) {
        ClassRpgConfig.ValdorsVerdict config = skills.valdorsVerdict;
        Transform look = TargetUtil.getLook(ref, store);
        if (look == null) {
            return;
        }
        Vector3d origin = new Vector3d(look.getPosition());
        Vector3d dir = new Vector3d(look.getDirection()).normalize();
        List<Ref<EntityStore>> targets = AreaOfEffect.collectSphere(ref, store, origin, config.radius, true, null);
        double coneThreshold = Math.cos(Math.toRadians(config.coneDegrees));
        for (Ref<EntityStore> target : targets) {
            if (target == null || !target.isValid()) {
                continue;
            }
            TransformComponent transform = store.getComponent(target, TransformComponent.getComponentType());
            if (transform == null) {
                continue;
            }
            Vector3d toTarget = new Vector3d(transform.getPosition()).subtract(origin);
            if (toTarget.length() < 0.001) {
                continue;
            }
            toTarget.normalize();
            double dot = dir.dot(toTarget);
            if (dot < coneThreshold) {
                continue;
            }
            Damage.Source source = new Damage.EntitySource(ref);
            Damage damage = new Damage(source, DamageCause.PHYSICAL, config.damage);
            DamageSystems.executeDamage(target, store, damage);
            applyKnockback(target, store, origin, config.knockback);
        }
    }

    private void applyEvasiveShot(Ref<EntityStore> ref, Store<EntityStore> store) {
        ClassRpgConfig.EvasiveShot config = skills.evasiveShot;
        Transform look = TargetUtil.getLook(ref, store);
        if (look == null) {
            return;
        }
        Vector3d back = new Vector3d(look.getDirection()).normalize().scale(-1.0);
        Velocity velocity = store.getComponent(ref, Velocity.getComponentType());
        if (velocity != null) {
            velocity.addForce(new Vector3d(back.x * config.backwardForce, config.upwardForce, back.z * config.backwardForce));
        }
        spawnArrowFan(ref, store, config.arrowCount, config.spreadDegrees, config.projectileId);
    }

    private void applyRainOfVengeance(Ref<EntityStore> ref, Store<EntityStore> store) {
        ClassRpgConfig.RainOfVengeance config = skills.rainOfVengeance;
        Vector3d target = TargetUtil.getTargetLocation(ref, blockId -> blockId != 0, config.targetRange, store);
        if (target == null) {
            Transform look = TargetUtil.getLook(ref, store);
            if (look != null) {
                target = new Vector3d(look.getPosition())
                        .add(new Vector3d(look.getDirection()).scale(config.fallbackForward));
            }
        }
        if (target == null) {
            return;
        }
        EntityAnimationManager.get().add(
                new RainOfVengeanceEffect(ref, target, config.delayMs, config.radius, config.arrows,
                        config.slowMultiplier, config.slowDurationMs, config.slowEffectId, config.slowEffectDurationMs),
                store,
                System.currentTimeMillis());
    }

    private void applyShadowStep(Ref<EntityStore> ref, Store<EntityStore> store, java.util.UUID playerId, long now) {
        ClassRpgConfig.ShadowStep config = skills.shadowStep;
        TeleportUtil.blink(ref, store, config.blinkDistance);
        List<StatModifier> modifiers = List.of(
                StatModifier.temporary("skill:shadow_step", StatType.ARMOR, config.armorAdd, StatModifier.Operation.ADD)
        );
        EntityAnimationManager.get().add(new TimedStatModifierEffect(playerId, modifiers, config.armorDurationMs), store, now);
        if (config.invulnerableMs > 0L) {
            EntityAnimationManager.get().add(new TimedInvulnerabilityEffect(ref, config.invulnerableMs), store, now);
        }
    }

    private void applyPhantomEdge(Ref<EntityStore> ref, Store<EntityStore> store, java.util.UUID playerId, long now) {
        ClassRpgConfig.PhantomEdge config = skills.phantomEdge;
        state.setPhantomEdge(playerId, config.durationMs);
        EntityAnimationManager.get().add(new MovementModifierEffect(ref, config.speedMultiplier, config.durationMs), store, now);
        long invisDuration = config.invisibilityDurationMs > 0L ? config.invisibilityDurationMs : config.durationMs;
        applyEffectAsset(ref, store, config.invisibilityEffectId, invisDuration);
    }

    private void applyPrimalRoar(Ref<EntityStore> ref, Store<EntityStore> store, java.util.UUID playerId, long now) {
        ClassRpgConfig.PrimalRoar config = skills.primalRoar;
        Vector3d origin = resolvePosition(store, ref);
        if (origin == null) {
            return;
        }
        List<Ref<EntityStore>> targets = AreaOfEffect.collectSphere(ref, store, origin, config.radius, true, null);
        for (Ref<EntityStore> target : targets) {
            if (target == null || !target.isValid() || target.equals(ref)) {
                continue;
            }
            if (config.stunDurationMs > 0L) {
                EntityAnimationManager.get().add(new MovementFreezeEffect(target, config.stunDurationMs), store, now);
            }
            long stunEffectDuration = config.stunEffectDurationMs > 0L ? config.stunEffectDurationMs : config.stunDurationMs;
            applyEffectAsset(target, store, config.stunEffectId, stunEffectDuration);
        }
        List<StatModifier> modifiers = List.of(
                StatModifier.temporary("skill:primal_roar", StatType.ATTACK_SPEED,
                        config.attackSpeedMultiplier, StatModifier.Operation.MULTIPLY_TOTAL)
        );
        EntityAnimationManager.get().add(new TimedStatModifierEffect(playerId, modifiers, config.buffDurationMs), store, now);
    }

    private void applyUndyingRage(Ref<EntityStore> ref, Store<EntityStore> store, java.util.UUID playerId, long now) {
        ClassRpgConfig.UndyingRage config = skills.undyingRage;
        state.startRage(playerId, config.durationMs);
        List<StatModifier> modifiers = List.of(
                StatModifier.temporary("skill:undying_rage", StatType.ARMOR, config.armorAdd, StatModifier.Operation.ADD),
                StatModifier.temporary("skill:undying_rage", StatType.ATTACK_SPEED,
                        config.attackSpeedMultiplier, StatModifier.Operation.MULTIPLY_TOTAL)
        );
        EntityAnimationManager.get().add(new TimedStatModifierEffect(playerId, modifiers, config.durationMs), store, now);
        EntityAnimationManager.get().add(new MovementModifierEffect(ref, config.moveSpeedMultiplier, config.durationMs), store, now);
        EntityAnimationManager.get().add(new RageEndEffect(ref, playerId, state, config.durationMs, config.healRatio), store, now);
        if (config.invulnerable) {
            EntityAnimationManager.get().add(new TimedInvulnerabilityEffect(ref, config.durationMs), store, now);
        }
    }

    private void applyRadiance(Ref<EntityStore> ref, Store<EntityStore> store, long now) {
        ClassRpgConfig.Radiance config = skills.radiance;
        double radius = config.radius;
        EntityAnimationManager.get().add(
                new RadianceEffect(ref, config.durationMs,
                        new com.talania.core.combat.utils.AreaHealing.Settings().radius(radius).healRatio(config.healRatio),
                        radius,
                        config.slowMultiplier,
                        config.slowDurationMs,
                        config.slowEffectId,
                        config.slowEffectDurationMs),
                store,
                now);
    }

    private void applyDawnbreaker(Ref<EntityStore> ref, Store<EntityStore> store, long now) {
        ClassRpgConfig.Dawnbreaker config = skills.dawnbreaker;
        Vector3d target = TargetUtil.getTargetLocation(ref, blockId -> blockId != 0, config.targetRange, store);
        if (target == null) {
            target = resolvePosition(store, ref);
        }
        if (target == null) {
            return;
        }
        AreaDamage.damageSphere(ref, store, target, config.radius, DamageCause.ENVIRONMENT, config.damage, true, null);
        List<Ref<EntityStore>> players = AreaOfEffect.collectSphere(ref, store, target, config.radius, true, null);
        for (Ref<EntityStore> playerRef : players) {
            if (playerRef == null || !playerRef.isValid()) {
                continue;
            }
            Player player = store.getComponent(playerRef, Player.getComponentType());
            if (player == null) {
                continue;
            }
            java.util.UUID targetId = resolveUuid(store, playerRef);
            if (targetId == null) {
                continue;
            }
            List<StatModifier> shield = List.of(
                    StatModifier.temporary("skill:dawnbreaker", StatType.ARMOR,
                            config.shieldArmorAdd, StatModifier.Operation.ADD)
            );
            EntityAnimationManager.get().add(new TimedStatModifierEffect(targetId, shield, config.shieldDurationMs), store, now);
        }
        if (config.blindDurationMs > 0L) {
            for (Ref<EntityStore> targetRef : players) {
                if (targetRef == null || !targetRef.isValid() || targetRef.equals(ref)) {
                    continue;
                }
                applyEffectAsset(targetRef, store, config.blindEffectId, config.blindDurationMs);
            }
        }
    }

    private void applyVoidRift(Ref<EntityStore> ref, Store<EntityStore> store, long now) {
        ClassRpgConfig.VoidRift config = skills.voidRift;
        Vector3d target = TargetUtil.getTargetLocation(ref, blockId -> blockId != 0, config.targetRange, store);
        if (target == null) {
            target = resolvePosition(store, ref);
        }
        if (target == null) {
            return;
        }
        EntityAnimationManager.get().add(
                new PullEffect(ref, target, config.radius, config.strength, config.durationMs, config.tickMs),
                store,
                now);
    }

    private void applyArcaneTorrent(Ref<EntityStore> ref, Store<EntityStore> store, java.util.UUID playerId, long now) {
        ClassRpgConfig.ArcaneTorrent config = skills.arcaneTorrent;
        Vector3d target = TargetUtil.getTargetLocation(ref, blockId -> blockId != 0, config.targetRange, store);
        if (target == null) {
            Transform look = TargetUtil.getLook(ref, store);
            if (look != null) {
                target = new Vector3d(look.getPosition())
                        .add(new Vector3d(look.getDirection()).scale(config.fallbackForward));
            }
        }
        if (target == null) {
            return;
        }
        List<StatModifier> modifiers = List.of(
                StatModifier.temporary("skill:arcane_torrent", StatType.ARMOR,
                        config.armorAdd, StatModifier.Operation.ADD)
        );
        EntityAnimationManager.get().add(new TimedStatModifierEffect(playerId, modifiers, config.armorDurationMs), store, now);
        EntityAnimationManager.get().add(new TimedFlightEffect(ref, config.durationMs), store, now);
        EntityAnimationManager.get().add(new MovementFreezeEffect(ref, config.durationMs), store, now);
        String projectileId = config.projectileId == null || config.projectileId.isBlank()
                ? PROJECTILE_ARROW
                : config.projectileId;
        EntityAnimationManager.get().add(
                new ArcaneTorrentEffect(ref, target, projectileId, config.durationMs, config.tickMs),
                store,
                now);
        if (config.invulnerable) {
            EntityAnimationManager.get().add(new TimedInvulnerabilityEffect(ref, config.durationMs), store, now);
        }
    }

    private void applyFlashbang(Ref<EntityStore> ref, Store<EntityStore> store, long now) {
        ClassRpgConfig.Flashbang config = skills.flashbang;
        Vector3d target = TargetUtil.getTargetLocation(ref, blockId -> blockId != 0, config.targetRange, store);
        if (target == null) {
            target = resolvePosition(store, ref);
        }
        if (target == null) {
            return;
        }
        List<Ref<EntityStore>> targets = AreaOfEffect.collectSphere(ref, store, target, config.radius, true, null);
        for (Ref<EntityStore> targetRef : targets) {
            if (targetRef == null || !targetRef.isValid() || targetRef.equals(ref)) {
                continue;
            }
            if (config.stunDurationMs > 0L) {
                EntityAnimationManager.get().add(new MovementFreezeEffect(targetRef, config.stunDurationMs), store, now);
            }
            java.util.UUID targetId = resolveUuid(store, targetRef);
            if (targetId != null) {
                List<StatModifier> armorBreak = List.of(
                        StatModifier.temporary("skill:flashbang", StatType.ARMOR,
                                config.armorMultiplier, StatModifier.Operation.MULTIPLY_TOTAL)
                );
                EntityAnimationManager.get().add(new TimedStatModifierEffect(targetId, armorBreak, config.armorDurationMs), store, now);
            }
            applyEffectAsset(targetRef, store, config.blindEffectId, config.blindDurationMs);
        }
    }

    private void applyLeadStorm(Ref<EntityStore> ref, Store<EntityStore> store, long now) {
        ClassRpgConfig.LeadStorm config = skills.leadStorm;
        EntityAnimationManager.get().add(
                new LeadStormEffect(ref, config.radius, config.damage, config.durationMs, config.tickMs),
                store,
                now);
    }

    private void applyWhirlwind(Ref<EntityStore> ref, Store<EntityStore> store, java.util.UUID playerId, long now) {
        ClassRpgConfig.Whirlwind config = skills.whirlwind;
        Vector3d origin = resolvePosition(store, ref);
        if (origin == null) {
            return;
        }
        List<Ref<EntityStore>> targets = AreaOfEffect.collectSphere(ref, store, origin, config.radius, true, null);
        for (Ref<EntityStore> target : targets) {
            if (target == null || !target.isValid() || target.equals(ref)) {
                continue;
            }
            applyKnockback(target, store, origin, config.knockback);
        }
        List<StatModifier> modifiers = List.of(
                StatModifier.temporary("skill:whirlwind", StatType.DODGE_CHANCE,
                        config.dodgeBonus, StatModifier.Operation.ADD)
        );
        EntityAnimationManager.get().add(new TimedStatModifierEffect(playerId, modifiers, config.durationMs), store, now);
    }

    private void applyThunderfall(Ref<EntityStore> ref, Store<EntityStore> store) {
        ClassRpgConfig.Thunderfall config = skills.thunderfall;
        Vector3d target = TargetUtil.getTargetLocation(ref, blockId -> blockId != 0, config.targetRange, store);
        if (target == null) {
            return;
        }
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        Vector3f rotation = transform == null ? new Vector3f(0.0f, 0.0f, 0.0f) : new Vector3f(transform.getRotation());
        store.addComponent(ref, com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType(),
                new com.hypixel.hytale.server.core.modules.entity.teleport.Teleport(target, rotation));
        AreaDamage.damageSphere(ref, store, target, config.radius, DamageCause.ENVIRONMENT, config.damage, true, null);
    }

    private static void applyEffectAsset(Ref<EntityStore> target, Store<EntityStore> store,
                                         String effectId, long durationMs) {
        if (durationMs <= 0L) {
            return;
        }
        EntityEffectService.apply(target, store, effectId, durationMs, OverlapBehavior.OVERWRITE);
    }

    private static void applyKnockback(Ref<EntityStore> target, Store<EntityStore> store, Vector3d origin, double strength) {
        Velocity velocity = store.getComponent(target, Velocity.getComponentType());
        TransformComponent transform = store.getComponent(target, TransformComponent.getComponentType());
        if (velocity == null || transform == null) {
            return;
        }
        Vector3d delta = new Vector3d(transform.getPosition()).subtract(origin);
        if (delta.length() < 0.001) {
            return;
        }
        delta.normalize();
        velocity.addForce(delta.scale(strength));
    }

    private static Vector3d resolvePosition(Store<EntityStore> store, Ref<EntityStore> ref) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        return transform == null ? null : new Vector3d(transform.getPosition());
    }

    private static java.util.UUID resolveUuid(Store<EntityStore> store, Ref<EntityStore> ref) {
        UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
        return uuid == null ? null : uuid.getUuid();
    }

    private static void spawnArrowFan(Ref<EntityStore> ref, Store<EntityStore> store, int count,
                                      float spreadDegrees, String projectileId) {
        Transform look = TargetUtil.getLook(ref, store);
        if (look == null) {
            return;
        }
        Vector3d origin = new Vector3d(look.getPosition());
        origin.y += 1.4;
        Vector3f baseRot = new Vector3f(look.getRotation());
        float start = -spreadDegrees * (count - 1) / 2.0f;
        for (int i = 0; i < count; i++) {
            float yawOffset = start + (spreadDegrees * i);
            Vector3f rotation = new Vector3f(baseRot.getX(), baseRot.getY() + (float) Math.toRadians(yawOffset), baseRot.getZ());
            String resolvedProjectile = projectileId == null || projectileId.isBlank() ? PROJECTILE_ARROW : projectileId;
            spawnProjectile(store, ref, origin, rotation, resolvedProjectile);
        }
    }

    private static boolean spawnProjectile(Store<EntityStore> store, Ref<EntityStore> sourceRef,
                                           Vector3d origin, Vector3f rotation, String projectileId) {
        if (store == null || sourceRef == null || origin == null || rotation == null || projectileId == null) {
            return false;
        }
        TimeResource timeResource = store.getResource(TimeResource.getResourceType());
        if (timeResource == null) {
            return false;
        }
        Holder<EntityStore> holder = ProjectileComponent.assembleDefaultProjectile(timeResource, projectileId, origin, rotation);
        ProjectileComponent projectileComponent = holder.getComponent(ProjectileComponent.getComponentType());
        if (projectileComponent == null) {
            return false;
        }
        if (projectileComponent.getProjectile() == null) {
            projectileComponent.initialize();
            if (projectileComponent.getProjectile() == null) {
                return false;
            }
        }
        UUIDComponent uuid = store.getComponent(sourceRef, UUIDComponent.getComponentType());
        if (uuid == null) {
            return false;
        }
        projectileComponent.shoot(holder, uuid.getUuid(), origin.getX(), origin.getY(), origin.getZ(),
                rotation.getYaw(), rotation.getPitch());
        store.addEntity(holder, AddReason.SPAWN);
        return true;
    }

    /**
     * Effect to heal a portion of damage dealt when Undying Rage ends.
     */
    private static final class RageEndEffect implements com.talania.core.entities.EntityAnimationEffect {
        private final Ref<EntityStore> ref;
        private final java.util.UUID playerId;
        private final ClassRpgState state;
        private final long endAt;
        private final float healRatio;

        private RageEndEffect(Ref<EntityStore> ref, java.util.UUID playerId, ClassRpgState state,
                              long durationMs, float healRatio) {
            this.ref = ref;
            this.playerId = playerId;
            this.state = state;
            this.endAt = System.currentTimeMillis() + Math.max(0L, durationMs);
            this.healRatio = Math.max(0.0f, healRatio);
        }

        @Override
        public void start(Store<EntityStore> store, long nowMs) {
            // no-op
        }

        @Override
        public void tick(Store<EntityStore> store, long nowMs) {
            // no-op
        }

        @Override
        public void stop(Store<EntityStore> store) {
            if (store == null || ref == null || !ref.isValid()) {
                return;
            }
            ClassRpgState.RageState rageState = state.endRage(playerId);
            if (rageState == null) {
                return;
            }
            double healAmount = rageState.damageAccumulated() * healRatio;
            if (healAmount <= 0.0) {
                return;
            }
            com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap statMap =
                    store.getComponent(ref, com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.getComponentType());
            if (statMap == null) {
                return;
            }
            statMap.addStatValue(com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth(), (float) healAmount);
        }

        @Override
        public boolean isFinished(long nowMs) {
            return nowMs >= endAt;
        }
    }
}
