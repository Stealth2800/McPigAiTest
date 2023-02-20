package com.stealthyone.pigaitest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

public class CustomPigEntityData {

    public final static double SUMMONER_ATTENTION_RANGE = 20.0;

    public final static String META_KEY = "customPigData";
    public final static int BLOCK_BREAK_THRESHOLD = 10;

    private int brokenBlocksFarming = 0;
    private int brokenBlocksMining = 0;

    public final Queue<RecentlyBrokenBlock> blockBreakQueue = new ArrayDeque<>();

    public Location fishTarget;

    public void addBrokenBlock(Block block) {
        var isCrop = Tag.CROPS.isTagged(block.getType());
        if (isCrop) {
            if (
                block.getBlockData() instanceof Ageable ageable
                    && ageable.getAge() < ageable.getMaximumAge()
            ) return;

            if (++brokenBlocksFarming < BLOCK_BREAK_THRESHOLD) {
                return;
            }
            brokenBlocksFarming = 0;
        } else {
            if (++brokenBlocksMining < BLOCK_BREAK_THRESHOLD) {
                return;
            }
            brokenBlocksMining = 0;
        }
        blockBreakQueue.add(new RecentlyBrokenBlock(block.getType(), block.getLocation()));
    }

    public static void alertPigsInArea(Location origin, Consumer<CustomPigEntityData> consumer) {
        var radius = SUMMONER_ATTENTION_RANGE;
        for (var nearby : origin.getWorld().getNearbyEntities(origin, radius, radius, radius)) {
            var meta = nearby.getMetadata(CustomPigEntityData.META_KEY);
            if (meta.isEmpty()) continue;

            var nearbyData = (CustomPigEntityData) meta.get(0).value();
            consumer.accept(nearbyData);
        }
    }

    public record RecentlyBrokenBlock(Material type, Location location) {
    }

}
