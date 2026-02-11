package com.talania.classrpg.combat;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.talania.classrpg.ClassService;
import com.talania.classrpg.ClassType;
import com.talania.classrpg.config.ClassRpgConfig;
import com.talania.classrpg.effects.MovementModifierEffect;
import com.talania.classrpg.effects.PoisonEffect;
import com.talania.classrpg.state.ClassRpgState;
import com.talania.core.combat.targeting.AreaOfEffect;
import com.talania.core.combat.utils.AreaDamage;
import com.talania.core.entities.EntityAnimationManager;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Applies class passive effects during combat.
 */
public final class ClassRpgCombatSystem extends DamageEventSystem {
    private static final Query<EntityStore> QUERY = AllLegacyLivingEntityTypesQuery.INSTANCE;

    private final ClassService classService;
    private final ClassRpgState state;
    private final CombatXpService xpService;
    private final ClassRpgConfig.Skills skills;

    public ClassRpgCombatSystem(ClassService classService, ClassRpgState state,
                                CombatXpService xpService, ClassRpgConfig.Skills skills) {
        this.classService = classService;
        this.state = state;
        this.xpService = xpService;
        this.skills = skills == null ? new ClassRpgConfig.Skills() : skills;
    }

    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(
                new SystemGroupDependency<>(Order.AFTER, DamageModule.get().getGatherDamageGroup()),
                new SystemGroupDependency<>(Order.BEFORE, DamageModule.get().getInspectDamageGroup())
        );
    }

    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
                       CommandBuffer<EntityStore> commandBuffer, Damage damage) {
        if (damage == null || damage.isCancelled()) {
            return;
        }

        Ref<EntityStore> targetRef = chunk.getReferenceTo(index);
        if (targetRef == null || !targetRef.isValid()) {
            return;
        }

        if (!(damage.getSource() instanceof Damage.EntitySource entitySource)) {
            return;
        }

        Ref<EntityStore> attackerRef = entitySource.getRef();
        if (attackerRef == null || !attackerRef.isValid()) {
            return;
        }

        UUID attackerId = resolveUuid(attackerRef, commandBuffer);
        UUID targetId = resolveUuid(targetRef, commandBuffer);
        if (attackerId == null || targetId == null) {
            return;
        }

        Player attacker = EntityUtils.getEntity(attackerRef, commandBuffer) instanceof Player player ? player : null;
        Player targetPlayer = EntityUtils.getEntity(targetRef, commandBuffer) instanceof Player player ? player : null;

        ClassType attackerClass = classService.getAssigned(attackerId);
        ClassType targetClass = classService.getAssigned(targetId);

        // Counter-Guard trigger on successful block
        if (Boolean.TRUE.equals(damage.getIfPresentMetaObject(Damage.BLOCKED))
                && targetClass == ClassType.SWORDMASTER) {
            state.setCounterGuard(targetId, 5_000L);
        }

        // Apply passives based on attacker class
        if (attackerClass != null) {
            switch (attackerClass) {
                case SWORDMASTER -> applySwordmasterPassives(attackerId, attackerRef, store, damage);
                case ASSASSIN -> applyAssassinPassives(attackerId, attackerRef, targetRef, store, damage);
                case BERSERKER -> applyBerserkerPassives(attackerRef, targetRef, store, damage);
                case ARCHER -> applyArcherPassives(attackerRef, targetRef, store, damage);
                case MACE_BEARER -> applyMacePassives(attackerRef, targetRef, store, damage);
                default -> {
                    // no-op
                }
            }
        }

        // Phantom Edge execute
        if (attackerClass == ClassType.ASSASSIN && state.consumePhantomEdge(attackerId)) {
            ClassRpgConfig.PhantomEdge phantomEdge = skills.phantomEdge;
            if (targetHealthPercent(store, targetRef) <= phantomEdge.executeThreshold) {
                damage.setAmount(damage.getAmount() * phantomEdge.executeMultiplier);
            }
        }

        // Undying Rage damage tracking
        ClassRpgState.RageState rageState = state.getRage(attackerId);
        if (rageState != null) {
            rageState.addDamage(damage.getAmount());
        }

        if (xpService != null && attackerClass != null && damage.getAmount() > 0.0f) {
            xpService.awardHitXp(attackerId, attackerClass, targetPlayer != null, damage.getAmount());
        }
    }

    private void applySwordmasterPassives(UUID attackerId, Ref<EntityStore> attackerRef, Store<EntityStore> store,
                                          Damage damage) {
        if (state.consumeCounterGuard(attackerId)) {
            damage.setAmount(damage.getAmount() * 1.30f);
        }
        if (isDuelistSituation(attackerRef, store)) {
            damage.setAmount(damage.getAmount() * 1.15f);
        }
    }

    private void applyAssassinPassives(UUID attackerId, Ref<EntityStore> attackerRef, Ref<EntityStore> targetRef,
                                       Store<EntityStore> store, Damage damage) {
        if (isBackstab(attackerRef, targetRef, store)) {
            double multiplier = 1.5 + (Math.random() * 0.5);
            damage.setAmount((float) (damage.getAmount() * multiplier));
        }
        if (isDagger(attackerRef, store)) {
            ClassRpgState.HitStreak streak = state.getOrCreateVenomStreak(attackerId);
            long now = System.currentTimeMillis();
            streak.registerHit(resolveUuid(targetRef, store), now);
            if (streak.hits() >= 3) {
                streak.reset();
                EntityAnimationManager.get().add(new PoisonEffect(targetRef, attackerRef, 2.0f, 5_000L, 1_000L), store, now);
            }
        }
    }

    private void applyBerserkerPassives(Ref<EntityStore> attackerRef, Ref<EntityStore> targetRef,
                                        Store<EntityStore> store, Damage damage) {
        float attackerHp = healthPercent(store, attackerRef);
        if (attackerHp >= 0.0f) {
            float missing = 1.0f - attackerHp;
            float multiplier = 1.0f + (missing * 0.5f);
            damage.setAmount(damage.getAmount() * multiplier);
        }
        float targetHp = healthPercent(store, targetRef);
        if (targetHp >= 0.0f && targetHp < 0.30f) {
            damage.setAmount(damage.getAmount() * 2.0f);
        }
    }

    private void applyArcherPassives(Ref<EntityStore> attackerRef, Ref<EntityStore> targetRef,
                                     Store<EntityStore> store, Damage damage) {
        if (isBow(attackerRef, store)) {
            float distance = distanceBetween(attackerRef, targetRef, store);
            if (distance > 0.0f) {
                float multiplier = 1.0f + Math.min(distance * 0.01f, 1.5f);
                damage.setAmount(damage.getAmount() * multiplier);
            }
            if (isCrouching(attackerRef, store)) {
                damage.setAmount(damage.getAmount() * 1.60f);
                EntityAnimationManager.get().add(new MovementModifierEffect(attackerRef, 0.75f, 600L), store, System.currentTimeMillis());
            }
        }
    }

    private void applyMacePassives(Ref<EntityStore> attackerRef, Ref<EntityStore> targetRef,
                                   Store<EntityStore> store, Damage damage) {
        if (!isMace(attackerRef, store)) {
            return;
        }
        UUID targetId = resolveUuid(targetRef, store);
        if (targetId != null) {
            float armor = StatsManager.getStat(targetId, StatType.ARMOR);
            if (armor > 0.0f && armor < 0.999f) {
                float factor = (1.0f - armor * 0.75f) / (1.0f - armor);
                damage.setAmount(damage.getAmount() * factor);
            }
        }
        // Splash damage
        TransformComponent transform = store.getComponent(targetRef, TransformComponent.getComponentType());
        if (transform != null) {
            AreaDamage.damageSphere(attackerRef, store, transform.getPosition(), 2.0, DamageCause.PHYSICAL,
                    damage.getAmount() * 0.30f, true, ref -> !ref.equals(targetRef));
        }
    }

    private boolean isDuelistSituation(Ref<EntityStore> attackerRef, Store<EntityStore> store) {
        TransformComponent transform = store.getComponent(attackerRef, TransformComponent.getComponentType());
        if (transform == null) {
            return false;
        }
        Vector3d origin = new Vector3d(transform.getPosition());
        List<Ref<EntityStore>> nearby = AreaOfEffect.collectSphere(attackerRef, store, origin, 5.0, true, null);
        return nearby.size() <= 1;
    }

    private boolean isBackstab(Ref<EntityStore> attackerRef, Ref<EntityStore> targetRef, Store<EntityStore> store) {
        Transform look = TargetUtil.getLook(targetRef, store);
        TransformComponent targetTransform = store.getComponent(targetRef, TransformComponent.getComponentType());
        TransformComponent attackerTransform = store.getComponent(attackerRef, TransformComponent.getComponentType());
        if (look == null || targetTransform == null || attackerTransform == null) {
            return false;
        }
        Vector3d targetDir = new Vector3d(look.getDirection()).normalize();
        Vector3d toAttacker = new Vector3d(attackerTransform.getPosition()).subtract(targetTransform.getPosition());
        if (toAttacker.length() < 0.001) {
            return false;
        }
        toAttacker.normalize();
        double dot = targetDir.dot(toAttacker);
        return dot < -0.5;
    }

    private boolean isDagger(Ref<EntityStore> attackerRef, Store<EntityStore> store) {
        String family = weaponFamily(attackerRef, store);
        if (family != null && family.toLowerCase().contains("dagger")) {
            return true;
        }
        String itemId = weaponId(attackerRef, store);
        return itemId != null && itemId.toLowerCase().contains("dagger");
    }

    private boolean isMace(Ref<EntityStore> attackerRef, Store<EntityStore> store) {
        String family = weaponFamily(attackerRef, store);
        if (family != null) {
            String lower = family.toLowerCase();
            if (lower.contains("mace") || lower.contains("hammer")) {
                return true;
            }
        }
        String itemId = weaponId(attackerRef, store);
        if (itemId == null) {
            return false;
        }
        String lower = itemId.toLowerCase();
        return lower.contains("mace") || lower.contains("hammer");
    }

    private boolean isBow(Ref<EntityStore> attackerRef, Store<EntityStore> store) {
        String family = weaponFamily(attackerRef, store);
        if (family != null && family.toLowerCase().contains("bow")) {
            return true;
        }
        String itemId = weaponId(attackerRef, store);
        return itemId != null && itemId.toLowerCase().contains("bow");
    }

    private String weaponFamily(Ref<EntityStore> attackerRef, Store<EntityStore> store) {
        ItemStack stack = weaponInHand(attackerRef, store);
        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            return null;
        }
        AssetExtraInfo.Data data = stack.getItem().getData();
        if (data == null || data.getRawTags() == null) {
            return null;
        }
        String[] family = data.getRawTags().get("Family");
        if (family != null && family.length > 0) {
            return family[0];
        }
        return null;
    }

    private String weaponId(Ref<EntityStore> attackerRef, Store<EntityStore> store) {
        ItemStack stack = weaponInHand(attackerRef, store);
        if (stack == null || stack.isEmpty() || stack.getItem() == null) {
            return null;
        }
        return stack.getItem().getId();
    }

    private ItemStack weaponInHand(Ref<EntityStore> attackerRef, Store<EntityStore> store) {
        Player player = store.getComponent(attackerRef, Player.getComponentType());
        if (player == null || player.getInventory() == null) {
            return null;
        }
        return player.getInventory().getItemInHand();
    }

    private float healthPercent(Store<EntityStore> store, Ref<EntityStore> ref) {
        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) {
            return -1.0f;
        }
        EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
        if (health == null || health.getMax() <= 0.0f) {
            return -1.0f;
        }
        return health.get() / health.getMax();
    }

    private float targetHealthPercent(Store<EntityStore> store, Ref<EntityStore> ref) {
        return healthPercent(store, ref);
    }

    private float distanceBetween(Ref<EntityStore> a, Ref<EntityStore> b, Store<EntityStore> store) {
        TransformComponent ta = store.getComponent(a, TransformComponent.getComponentType());
        TransformComponent tb = store.getComponent(b, TransformComponent.getComponentType());
        if (ta == null || tb == null) {
            return 0.0f;
        }
        Vector3d delta = new Vector3d(ta.getPosition()).subtract(tb.getPosition());
        return (float) delta.length();
    }

    private boolean isCrouching(Ref<EntityStore> ref, Store<EntityStore> store) {
        MovementStatesComponent movementStatesComponent = store.getComponent(ref, MovementStatesComponent.getComponentType());
        if (movementStatesComponent == null) {
            return false;
        }
        com.hypixel.hytale.protocol.MovementStates states = movementStatesComponent.getMovementStates();
        return states != null && states.crouching;
    }

    private UUID resolveUuid(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
        return uuid == null ? null : uuid.getUuid();
    }

    private UUID resolveUuid(Ref<EntityStore> ref, CommandBuffer<EntityStore> accessor) {
        UUIDComponent uuid = accessor.getComponent(ref, UUIDComponent.getComponentType());
        return uuid == null ? null : uuid.getUuid();
    }
}
