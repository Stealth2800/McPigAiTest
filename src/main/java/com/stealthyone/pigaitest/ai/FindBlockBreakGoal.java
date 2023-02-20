package com.stealthyone.pigaitest.ai;

import com.stealthyone.pigaitest.CustomPigEntity;
import com.stealthyone.pigaitest.CustomPigEntityData;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

import java.util.ArrayList;
import java.util.Comparator;

public class FindBlockBreakGoal extends SummonerFollowGoal {

    private final static int SEARCH_RANGE = 4;

    private CustomPigEntityData.RecentlyBrokenBlock target;

    public FindBlockBreakGoal(CustomPigEntity mob) {
        super(mob, 0.0, 1.0);
    }

    @Override
    public boolean canUse() {
        var summoner = mob.getSummoner();
        if (summoner == null) return false;

        var nextTarget = mob.getCustomData().blockBreakQueue.poll();
        if (nextTarget == null) return false;

        target = nextTarget;
        activeSummoner = summoner;
        return true;
    }

    @Override
    public void stop() {
        super.stop();

        final var isTargetCrop = Tag.CROPS.isTagged(target.type());
        final var world = mob.getLevel().getWorld();
        final var startLoc = mob.getBukkitEntity().getLocation();
        startLoc.setY(target.location().getBlockY());

        final var eligibleBlocks = new ArrayList<Block>();
        for (var x = -SEARCH_RANGE; x < SEARCH_RANGE; ++x) {
            for (var z = -SEARCH_RANGE; z < SEARCH_RANGE; ++z) {
                var block = world.getBlockAt(startLoc.getBlockX() + x, startLoc.getBlockY(), startLoc.getBlockZ() + z);

                var isCrop = Tag.CROPS.isTagged(block.getType());
                if (
                    isCrop != isTargetCrop
                        || block.getType().isAir()
                        || !block.getType().isBlock()
                        || block.getType().getHardness() < 0.0
                ) continue;

                if (
                    isCrop
                        && block.getBlockData() instanceof Ageable ageable
                        && ageable.getAge() < ageable.getMaximumAge()
                ) continue;

                eligibleBlocks.add(block);
            }
        }
        eligibleBlocks.sort(Comparator.comparingDouble((b) -> b.getLocation().distanceSquared(startLoc)));

        if (!eligibleBlocks.isEmpty()) mob.breakingBlock = eligibleBlocks.get(0).getState();
        target = null;
    }

}
