package com.stealthyone.pigaitest;

import com.stealthyone.pigaitest.ai.BlockBreakGoal;
import com.stealthyone.pigaitest.ai.DiveAfterFishGoal;
import com.stealthyone.pigaitest.ai.FindBlockBreakGoal;
import com.stealthyone.pigaitest.ai.LockoutGoal;
import com.stealthyone.pigaitest.ai.SummonerFollowGoal;
import com.stealthyone.pigaitest.ai.SummonerHurtTargetGoal;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class CustomPigEntity extends Pig {

    private final UUID summonerUuid;

    public BlockState breakingBlock;

    public CustomPigEntity(org.bukkit.entity.Player summoner, Location location) {
        super(EntityType.PIG, ((CraftWorld) location.getWorld()).getHandle());

        summonerUuid = summoner.getUniqueId();
        setPos(location.getX(), location.getY(), location.getZ());
        setCustomName(Component.literal("Custom Pig"));
        setCustomNameVisible(true);

        getBukkitEntity().setMetadata(
            CustomPigEntityData.META_KEY,
            new FixedMetadataValue(JavaPlugin.getPlugin(PigAiPlugin.class), new CustomPigEntityData())
        );

        var attributes = this.getAttributes();
        try {
            // This... isn't ideal.
            //final var field = AttributeMap.class.getDeclaredField("attributes");
            final var field = Arrays.stream(AttributeMap.class.getDeclaredFields())
                .filter((f) -> f.getType() == Map.class)
                .findFirst()
                .get();
            field.setAccessible(true);

            var rawAttributes = (Map<Attribute, AttributeInstance>) field.get(attributes);
            rawAttributes.put(
                Attributes.ATTACK_DAMAGE,
                new AttributeInstance(Attributes.ATTACK_DAMAGE, (inst) -> inst.setBaseValue(2.0))
            );
            rawAttributes.put(
                Attributes.ATTACK_KNOCKBACK,
                new AttributeInstance(Attributes.ATTACK_KNOCKBACK, (inst) -> inst.setBaseValue(2.0))
            );
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    protected void registerGoals() {
        // Combat
        goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.4F));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));

        // Block breaking
        goalSelector.addGoal(3, new BlockBreakGoal(this, 0.5));
        goalSelector.addGoal(4, new FindBlockBreakGoal(this));

        // Fishing
        goalSelector.addGoal(5, new DiveAfterFishGoal(this));

        // Follow + look around + wander
        goalSelector.addGoal(9, new SummonerFollowGoal(this, 7.0, 3.0));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(12, new WaterAvoidingRandomStrollGoal(this, 1.0));

        // Lockout
        goalSelector.addGoal(13, new LockoutGoal(this, 5 * 20L, 2 * 20L));

        targetSelector.addGoal(1, new SummonerHurtTargetGoal(this));
    }

    public LivingEntity getSummoner() {
        return level.getPlayerByUUID(summonerUuid);
    }

    public CustomPigEntityData getCustomData() {
        return (CustomPigEntityData) getBukkitEntity().getMetadata(CustomPigEntityData.META_KEY).get(0).value();
    }

}
