package com.stealthyone.pigaitest.ai;

import com.stealthyone.pigaitest.CustomPigEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.entity.ai.goal.Goal;
import org.bukkit.craftbukkit.v1_19_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.EnumSet;

public class BlockBreakGoal extends Goal {

    private final static double PACKET_RANGE = 12.0;
    private final static int PROGRESS_MAX = 9;

    private final CustomPigEntity mob;

    private final double breakSpeed;

    private double progressRaw;
    private int progressLast = -1;

    public BlockBreakGoal(CustomPigEntity mob, double breakSpeed) {
        this.mob = mob;
        this.breakSpeed = breakSpeed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        mob.getNavigation().stop();
        progressRaw = 0.0;
        progressLast = -1;
    }

    @Override
    public boolean canUse() {
        return mob.breakingBlock != null;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.breakingBlock != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        final var block = mob.breakingBlock;
        if (block == null) return;

        mob.getLookControl().setLookAt(block.getX(), block.getY(), block.getZ(), 10.0F, mob.getMaxHeadXRot());

        final var hardness = block.getType().getHardness();
        if (hardness == 0.0) {
            mob.breakingBlock.getBlock().breakNaturally();
            mob.breakingBlock = null;
            return;
        }

        this.progressRaw += (hardness * breakSpeed) * 0.05;
        var progressNew = (int) (progressRaw * PROGRESS_MAX);
        if (progressNew == progressLast) return;

        this.progressLast = progressNew;

        final var loc = block.getLocation();
        final var packet = new ClientboundBlockDestructionPacket(
            mob.getId(),
            new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),
            progressNew
        );
        final var packet2 = new ClientboundLevelParticlesPacket(
            new BlockParticleOption(ParticleTypes.BLOCK, ((CraftBlockState) block).getHandle()),
            false,
            block.getX() + 0.5,
            block.getY() + 0.5,
            block.getZ() + 0.5,
            0.2F,
            0.2F,
            0.2F,
            1.0F,
            8
        );

        final var world = mob.getLevel().getWorld();
        for (var nearby : world.getNearbyEntities(mob.getBukkitEntity().getLocation(), PACKET_RANGE, PACKET_RANGE, PACKET_RANGE)) {
            if (!(nearby instanceof Player player)) continue;

            final var con = ((CraftPlayer) player).getHandle().connection;
            con.send(packet);
            con.send(packet2);
        }

        if (progressNew <= PROGRESS_MAX) return;

        if (block.getBlock().breakNaturally()) {
            world.playSound(
                block.getLocation(),
                block.getBlockData().getSoundGroup().getBreakSound(),
                0.8F, 0.8F
            );
        }
        mob.breakingBlock = null;
    }

}
