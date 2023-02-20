package com.stealthyone.pigaitest.ai;

import com.stealthyone.pigaitest.CustomPigEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;

import java.util.EnumSet;

public class DiveAfterFishGoal extends Goal {

    private final CustomPigEntity mob;

    private Phase phase;

    public DiveAfterFishGoal(CustomPigEntity mob) {
        this.mob = mob;
        this.mob.getNavigation().setCanFloat(true);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean canUse() {
        final var mobData = mob.getCustomData();
        final var target = mobData.fishTarget;
        if (target == null) return false;

        phase = new DivePhase(target);
        mobData.fishTarget = null;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.phase == null) {
            return false;
        } else if (this.phase instanceof ReturnPhase p) {
            if (mob.getNavigation().isDone()) return false;
            return mob.distanceToSqr(p.summoner) > 1.0;
        } else {
            return true;
        }
    }

    @Override
    public void start() {
        final var phase = (DivePhase) this.phase;
        final var path = mob.getNavigation().createPath(phase.target.getX(), phase.target.getY(), phase.target.getZ(), 0);
        if (path == null) return; // ?

        var maxIdx = 0;
        for (var i = 0; i < path.getNodeCount(); ++i) {
            if (path.getNode(i).type == BlockPathTypes.WATER) {
                maxIdx = i;
                break;
            }
        }

        path.truncateNodes(maxIdx);
        mob.getNavigation().moveTo(path, 1.5);
    }

    @Override
    public void stop() {
        phase = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.phase instanceof DivePhase p) {
            if (!mob.getNavigation().isDone()) return;

            final var target = new Vec3(p.target.getX(), p.target.getY(), p.target.getZ());
            final var vel = target.subtract(mob.position()).normalize();

            mob.setDeltaMovement(vel.x, 0.4F, vel.z);

            final var summoner = mob.getSummoner();
            if (summoner == null) {
                this.phase = null;
                return;
            }
            this.phase = new ReturnPhase(summoner);
        }

        if (this.phase instanceof ReturnPhase p) {
            if (mob.isInWater() && mob.getFluidHeight(FluidTags.WATER) > mob.getFluidJumpThreshold()) {
                this.mob.getJumpControl().jump();
            }

            if (--p.pathRecalcCounter > 0) return;

            p.pathRecalcCounter = adjustedTickDelay(10);
            mob.getNavigation().moveTo(p.summoner, 1.0);
        }
    }

    private sealed static class Phase {
    }

    private final static class DivePhase extends Phase {

        public final Location target;

        DivePhase(Location target) {
            this.target = target;
        }

    }

    private final static class ReturnPhase extends Phase {

        final LivingEntity summoner;

        int pathRecalcCounter = 0;

        ReturnPhase(LivingEntity summoner) {
            this.summoner = summoner;
        }

    }

}
