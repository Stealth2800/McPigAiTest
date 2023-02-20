package com.stealthyone.pigaitest.ai;

import com.stealthyone.pigaitest.CustomPigEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SummonerFollowGoal extends Goal {

    protected final CustomPigEntity mob;

    protected LivingEntity activeSummoner;
    private int activeRecalcPathCounter;

    private final double startDistance;
    private final double endDistance;

    public SummonerFollowGoal(CustomPigEntity mob, double startDistance, double endDistance) {
        this.mob = mob;
        this.startDistance = startDistance;
        this.endDistance = endDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        var summoner = mob.getSummoner();
        if (summoner == null) return false;
        if (summoner.isSpectator()) return false;
        if (mob.distanceToSqr(summoner) < Math.pow(startDistance, 2.0)) return false;

        activeSummoner = summoner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        var navigation = mob.getNavigation();
        if (navigation.isDone()) return false;

        return mob.distanceToSqr(activeSummoner) > Math.pow(endDistance, 2.0);
    }

    @Override
    public void start() {
        activeRecalcPathCounter = 0;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        activeSummoner = null;
    }

    @Override
    public void tick() {
        mob.getLookControl().setLookAt(activeSummoner, 10.0F, mob.getMaxHeadXRot());
        if (--activeRecalcPathCounter > 0) return;

        activeRecalcPathCounter = adjustedTickDelay(10);
        mob.getNavigation().moveTo(activeSummoner, 1.0);
    }

}
