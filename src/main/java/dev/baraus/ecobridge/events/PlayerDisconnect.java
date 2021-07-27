package dev.baraus.ecobridge.events;

import dev.baraus.ecobridge.Ecobridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDisconnect implements Listener {
	
	private Ecobridge ecobridge;
	
	public PlayerDisconnect(Ecobridge ecobridge) {
		this.ecobridge = ecobridge;
	}
	
	@EventHandler
	public void onDisconnect(final PlayerQuitEvent event) {
		Bukkit.getScheduler().runTaskLaterAsynchronously(ecobridge, new Runnable() {

			@Override
			public void run() {
				if (event.getPlayer() != null) {
					Player p = event.getPlayer();
					cleanup(p);
					ecobridge.getEcoDataHandler().onDataSaveFunction(p, true, "true", false);
				}
			}
			
		}, 1L);
	}
	
	private void cleanup(Player p) {
		if (ecobridge.syncCompleteTasks.containsKey(p) == true) {
			Bukkit.getScheduler().cancelTask(ecobridge.syncCompleteTasks.get(p));
			ecobridge.syncCompleteTasks.remove(p);
		}
	}

}
