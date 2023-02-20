package com.stealthyone.pigaitest.ai;

import com.stealthyone.pigaitest.CustomPigEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.EnumSet;

public class SummonerHurtTargetGoal extends TargetGoal {

    private final CustomPigEntity mob;

    private LivingEntity summonerLastHurt;
    private long summonerLastHurtTimestamp;

    public SummonerHurtTargetGoal(CustomPigEntity mob) {
        super(mob, false);

        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        var summoner = this.mob.getSummoner();
        if (summoner == null) return false;

        summonerLastHurt = summoner.getLastHurtMob();
        return summonerLastHurtTimestamp != summoner.getLastHurtMobTimestamp()
            && canAttack(summonerLastHurt, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        mob.setTarget(summonerLastHurt, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);

        var summoner = mob.getSummoner();
        if (summoner != null) summonerLastHurtTimestamp = summoner.getLastHurtMobTimestamp();

        super.start();
    }

}
