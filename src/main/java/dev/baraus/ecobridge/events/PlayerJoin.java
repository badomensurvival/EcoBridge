package dev.baraus.ecobridge.events;

import dev.baraus.ecobridge.Ecobridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

public class PlayerJoin implements Listener {
	
	private Ecobridge ecobridge;
	
	public PlayerJoin(Ecobridge ecobridge) {
		this.ecobridge = ecobridge;
	}
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLaterAsynchronously(ecobridge, new Runnable() {

			@Override
			public void run() {
				if (event.getPlayer() != null) {
					if (event.getPlayer().isOnline() == true) {
						Player p = event.getPlayer();
						ecobridge.getEcoDataHandler().onJoinFunction(p);
						syncCompleteTask(p);
					}
				}
			}
			
		}, 5L);
	}
	
	private void syncCompleteTask(final Player p) {
		if (p != null) {
			if (p.isOnline() == true) {
				final long startTime = System.currentTimeMillis();
				BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(ecobridge, new Runnable() {

					@Override
					public void run() {
						if (p.isOnline() == true) {
							if (ecobridge.getEcoDataHandler().isSyncComplete(p)) {
								if (ecobridge.syncCompleteTasks.containsKey(p) == true) {
									int taskID = ecobridge.syncCompleteTasks.get(p);
									ecobridge.syncCompleteTasks.remove(p);
									Bukkit.getScheduler().cancelTask(taskID);
								}
							} else {
								if (System.currentTimeMillis() - startTime >= 10 * 1000) {
									if (ecobridge.syncCompleteTasks.containsKey(p) == true) {
										int taskID = ecobridge.syncCompleteTasks.get(p);
										ecobridge.syncCompleteTasks.remove(p);
										Bukkit.getScheduler().cancelTask(taskID);
									}
								}
							}
						}
					}
					
				}, 5L, 20L);
				ecobridge.syncCompleteTasks.put(p, task.getTaskId());
			}
		}
	}

}
