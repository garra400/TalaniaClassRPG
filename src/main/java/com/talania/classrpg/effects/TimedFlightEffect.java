package com.talania.classrpg.effects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.entities.EntityAnimationEffect;
import com.talania.core.movement.TimedFlightUtil;

/**
 * Temporarily grants flight using TimedFlightUtil.
 */
public final class TimedFlightEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> ref;
    private final long endAt;
    private final TimedFlightUtil.State state = new TimedFlightUtil.State();
    private boolean started;

    public TimedFlightEffect(Ref<EntityStore> ref, long durationMs) {
        this.ref = ref;
        this.endAt = System.currentTimeMillis() + Math.max(0L, durationMs);
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        if (started || ref == null || store == null || !ref.isValid()) {
            return;
        }
        TimedFlightUtil.start(state, ref, store, Math.max(0L, endAt - nowMs));
        started = true;
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        if (ref == null || store == null || !ref.isValid()) {
            return;
        }
        TimedFlightUtil.tick(state, ref, store);
    }

    @Override
    public void stop(Store<EntityStore> store) {
        if (ref == null || store == null || !ref.isValid()) {
            return;
        }
        TimedFlightUtil.stop(state, ref, store);
    }

    @Override
    public boolean isFinished(long nowMs) {
        return nowMs >= endAt;
    }
}
