package com.stealthyone.pigaitest;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PigAiPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            return true;
        }

        var location = player.getTargetBlock(null, 10).getRelative(BlockFace.UP).getLocation();
        var entity = new CustomPigEntity(player, location);

        ((CraftWorld) location.getWorld()).getHandle()
            .addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);

        player.sendMessage("Spawned custom pig");
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        CustomPigEntityData.alertPigsInArea(event.getPlayer().getLocation(), (data) -> {
            data.addBrokenBlock(event.getBlock());
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerFishEvent event) {
        if (!(event.getCaught() instanceof Item)) return;

        CustomPigEntityData.alertPigsInArea(event.getPlayer().getLocation(), (data) -> {
            data.fishTarget = event.getHook().getLocation();
        });
    }

}
