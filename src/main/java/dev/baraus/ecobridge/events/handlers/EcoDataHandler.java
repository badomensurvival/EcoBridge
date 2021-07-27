package dev.baraus.ecobridge.events.handlers;

import dev.baraus.ecobridge.Ecobridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class EcoDataHandler {
	
	private Ecobridge ecobridge;
	private Map<Player, Double> backupMoney = new HashMap<Player, Double>();
	private Map<Player, Double> balanceMap = new HashMap<Player, Double>();
	private Map<Player, Integer> runningTasks = new HashMap<Player, Integer>();
	private Set<Player> playersInSync = new HashSet<Player>();
	
	public EcoDataHandler(Ecobridge ecobridge) {
		this.ecobridge = ecobridge;
	}
	
	public void onShutDownDataSave() {
		Ecobridge.log.info("Saving online players data...");
		List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		
		for (Player p : onlinePlayers) {
			if (p.isOnline() == true) {
				onDataSaveFunction(p, true, "true", true);
			}
		}
		Ecobridge.log.info("Data save complete for " + onlinePlayers.size() + " players.");
	}
	
	public void updateBalanceMap() {
		List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		if (onlinePlayers.isEmpty() == false) {
			for (Player p : onlinePlayers) {
				if (playersInSync.contains(p) == true) {
					balanceMap.put(p, Ecobridge.vault.getBalance(p));
				}
			}
		}
	}
	
	public boolean isSyncComplete(Player p) {
		if (playersInSync.contains(p) == true) {
			return true;
		} else {
			return false;
		}
	}
	
	private void dataCleanup(Player p, Boolean isDisabling) {
		if (isDisabling == false) {
			playersInSync.remove(p);
			backupMoney.remove(p);
			balanceMap.remove(p);
			if (runningTasks.containsKey(p) == true) {
				Bukkit.getScheduler().cancelTask(runningTasks.get(p));
				runningTasks.remove(p);
			}
		}
	}
	
	private void setPlayerData(final Player p, String[] data, boolean cancelTask) {
		try {
			Double bal = Ecobridge.vault.getBalance(p);
			
			if (bal != null) {
				if (bal != 0.0) {
					Ecobridge.vault.withdrawPlayer(p, bal);
				}
				Double mysqlBal = Double.parseDouble(data[0]);
				Double localBal = Ecobridge.vault.getBalance(p);
				if (mysqlBal >= localBal) {
					Double finalBalance = mysqlBal - localBal;
					Ecobridge.vault.depositPlayer(p, finalBalance);
				} else if (mysqlBal < localBal) {
					Double finalBalance = localBal - mysqlBal;
					Ecobridge.vault.withdrawPlayer(p, finalBalance);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Double backupBalance = backupMoney.get(p);
			if (backupBalance != 0.0) {
				Double bal = Ecobridge.vault.getBalance(p);
				if (bal != null) {
					if (bal != 0.0) {
						Ecobridge.vault.withdrawPlayer(p, bal);
					}
					Double localBal = Ecobridge.vault.getBalance(p);
					if (backupBalance >= localBal) {
						Double finalBalance = backupBalance - localBal;
						Ecobridge.vault.depositPlayer(p, finalBalance);
					} else if (backupBalance < localBal) {
						Double finalBalance = localBal - backupBalance;
						Ecobridge.vault.depositPlayer(p, finalBalance);
					}
				}
			}
		}
		ecobridge.getEcoMysqlHandler().setSyncStatus(p, "false");
		playersInSync.add(p);
		backupMoney.remove(p);
		if (cancelTask == true) {
			int taskID = runningTasks.get(p);
			runningTasks.remove(p);
			Bukkit.getScheduler().cancelTask(taskID);
		}
	}
	
	public void onDataSaveFunction(Player p, Boolean datacleanup, String syncStatus, Boolean isDisabling) {
		boolean isPlayerInSync = playersInSync.contains(p);
		if (isDisabling == false) {
			if (datacleanup == true) {
				dataCleanup(p, isDisabling);
			}
			if (isPlayerInSync == true) {
				ecobridge.getEcoMysqlHandler().setData(p, Ecobridge.vault.getBalance(p), syncStatus);
			}
		} else {
			if (isPlayerInSync == true) {
				if (balanceMap.containsKey(p) == true) {
					ecobridge.getEcoMysqlHandler().setData(p, balanceMap.get(p), syncStatus);
				}
			}
		}
	}
	
	public void onJoinFunction(final Player p) {
		if (ecobridge.getEcoMysqlHandler().hasAccount(p) == true) {
			double balance = Ecobridge.vault.getBalance(p);
			backupMoney.put(p, balance);
			if (balance != 0.0) {
				Ecobridge.vault.withdrawPlayer(p, balance);
			}
			String[] data = ecobridge.getEcoMysqlHandler().getData(p);
			if (data[1].matches("true")) {
				setPlayerData(p, data, false);
			} else {
				final long taskStart = System.currentTimeMillis();
				BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(ecobridge, new Runnable() {

					@Override
					public void run() {
						if (p.isOnline() == true) {
							String[] data = ecobridge.getEcoMysqlHandler().getData(p);
							if (data[1].matches("true")) {
								setPlayerData(p, data, true);
							} else if (System.currentTimeMillis() - Long.parseLong(data[2]) >= 15 * 1000) {
								setPlayerData(p, data, true);
							}
						}
						if (System.currentTimeMillis() - taskStart >= 10 * 1000) {
							int taskID = runningTasks.get(p);
							runningTasks.remove(p);
							Bukkit.getScheduler().cancelTask(taskID);
						}
					}
					
				}, 20L, 20L);
				runningTasks.put(p, task.getTaskId());
			}
		} else {
			playersInSync.add(p);
		}
	}

}
