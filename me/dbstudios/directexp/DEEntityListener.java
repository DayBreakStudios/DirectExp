package me.dbstudios.directexp;

import java.util.ArrayList;
import java.util.Hashtable;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class DEEntityListener implements Listener {
	private Hashtable<LivingEntity, Player> killList = new Hashtable<LivingEntity, Player>();
	private ArrayList<String> deathTracker = new ArrayList<String>();
	private DirectExp common;
	
	public DEEntityListener(DirectExp common) {
		this.common = common;
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent ev) {
		if (ev.getEntity() instanceof LivingEntity && ev.getDamager() instanceof Player) {			
			LivingEntity damaged = (LivingEntity)ev.getEntity();
			Player damager = (Player)ev.getDamager();

			if (damaged.getHealth() - ev.getDamage() < 1) {
				killList.put(damaged, damager);
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(final EntityDeathEvent ev) {		
		if (!deathTracker.contains(ev.getEntity().getUniqueId().toString())) {
			deathTracker.add(ev.getEntity().getUniqueId().toString());
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(common, new Runnable() {
				public void run() {
					Bukkit.getPluginManager().callEvent(new EntityDeathEvent(ev.getEntity(), ev.getDrops(), ev.getDroppedExp()));
				}
			}, 20L);
			
			ev.setDroppedExp(0);
		} else {
			if (ev.getEntity() instanceof LivingEntity && killList.containsKey((LivingEntity)ev.getEntity())) {
				int dropped = common.getDrop(ev.getEntity(), ev.getDroppedExp());
			
				Player killer = killList.get((LivingEntity)ev.getEntity());
				
				killer.giveExp(dropped);
			
				if (common.getMainConfig().getString("config.gain-message", null) != null) {
					killer.sendMessage(common.parse(common.getMainConfig().getString("config.gain-message"), dropped, common.getTargetName(ev.getEntity())));
				}
			
				ev.setDroppedExp(0);
			
				killList.remove((LivingEntity)ev.getEntity());
			}
			
			deathTracker.remove(ev.getEntity().getUniqueId().toString());
		
			if (common.getMainConfig().getBoolean("config.cancel-other-exp-drops", true))
				ev.setDroppedExp(0);
		}
	}
}
