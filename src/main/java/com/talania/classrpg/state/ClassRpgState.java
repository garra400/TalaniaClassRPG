package com.talania.classrpg.state;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime state for class passives and active skill flags.
 */
public final class ClassRpgState {
    private final Map<UUID, Long> counterGuardUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> phantomEdgeUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> phantomEdgeArmed = new ConcurrentHashMap<>();
    private final Map<UUID, RageState> rageStates = new ConcurrentHashMap<>();
    private final Map<UUID, HitStreak> venomStreaks = new ConcurrentHashMap<>();

    public void setCounterGuard(UUID playerId, long durationMs) {
        if (playerId == null) {
            return;
        }
        counterGuardUntil.put(playerId, System.currentTimeMillis() + Math.max(0L, durationMs));
    }

    public boolean consumeCounterGuard(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        Long until = counterGuardUntil.get(playerId);
        if (until == null || System.currentTimeMillis() > until) {
            counterGuardUntil.remove(playerId);
            return false;
        }
        counterGuardUntil.remove(playerId);
        return true;
    }

    public void setPhantomEdge(UUID playerId, long durationMs) {
        if (playerId == null) {
            return;
        }
        phantomEdgeUntil.put(playerId, System.currentTimeMillis() + Math.max(0L, durationMs));
        phantomEdgeArmed.put(playerId, Boolean.TRUE);
    }

    public boolean isPhantomEdgeActive(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        Long until = phantomEdgeUntil.get(playerId);
        if (until == null) {
            return false;
        }
        if (System.currentTimeMillis() > until) {
            phantomEdgeUntil.remove(playerId);
            phantomEdgeArmed.remove(playerId);
            return false;
        }
        return true;
    }

    public boolean consumePhantomEdge(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        if (!isPhantomEdgeActive(playerId)) {
            return false;
        }
        Boolean armed = phantomEdgeArmed.get(playerId);
        if (armed == null || !armed) {
            return false;
        }
        phantomEdgeArmed.put(playerId, Boolean.FALSE);
        return true;
    }

    public void startRage(UUID playerId, long durationMs) {
        if (playerId == null) {
            return;
        }
        RageState state = new RageState(System.currentTimeMillis() + Math.max(0L, durationMs));
        rageStates.put(playerId, state);
    }

    public RageState getRage(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        RageState state = rageStates.get(playerId);
        if (state == null) {
            return null;
        }
        if (System.currentTimeMillis() > state.activeUntil) {
            rageStates.remove(playerId);
            return null;
        }
        return state;
    }

    public RageState endRage(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return rageStates.remove(playerId);
    }

    public HitStreak getVenomStreak(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return venomStreaks.get(playerId);
    }

    public HitStreak getOrCreateVenomStreak(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return venomStreaks.computeIfAbsent(playerId, ignored -> new HitStreak());
    }

    public void clearVenomStreak(UUID playerId) {
        if (playerId == null) {
            return;
        }
        venomStreaks.remove(playerId);
    }

    public static final class RageState {
        private final long activeUntil;
        private double damageAccumulated;

        private RageState(long activeUntil) {
            this.activeUntil = activeUntil;
        }

        public long activeUntil() {
            return activeUntil;
        }

        public double damageAccumulated() {
            return damageAccumulated;
        }

        public void addDamage(double amount) {
            if (amount > 0.0) {
                damageAccumulated += amount;
            }
        }
    }

    public static final class HitStreak {
        private UUID targetId;
        private int hits;
        private long lastHitAt;

        public void registerHit(UUID targetId, long nowMs) {
            if (targetId == null) {
                reset();
                return;
            }
            if (this.targetId == null || !this.targetId.equals(targetId)) {
                this.targetId = targetId;
                this.hits = 1;
                this.lastHitAt = nowMs;
                return;
            }
            if (nowMs - lastHitAt > 4_000L) {
                this.hits = 1;
            } else {
                this.hits++;
            }
            this.lastHitAt = nowMs;
        }

        public int hits() {
            return hits;
        }

        public UUID targetId() {
            return targetId;
        }

        public void reset() {
            this.targetId = null;
            this.hits = 0;
            this.lastHitAt = 0L;
        }
    }
}
