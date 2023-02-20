package com.stealthyone.pigaitest.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class LockoutGoal extends Goal {

    private final Mob mob;

    private final long intervalTicks;
    private final long durationTicks;

    private int lastTick;

    public LockoutGoal(Mob mob, long intervalTicks, long durationTicks) {
        this.mob = mob;
        this.intervalTicks = intervalTicks;
        this.durationTicks = durationTicks;

        lastTick = mob.tickCount;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        final var ret = mob.tickCount - lastTick > intervalTicks;
        if (ret) lastTick = mob.tickCount;
        return ret;
    }

    @Override
    public void start() {
        mob.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return mob.tickCount - lastTick <= durationTicks;
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(mob.position().add(0.0, 5.0, 0.0));
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

}
