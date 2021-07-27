package dev.baraus.ecobridge;

import java.io.File;

public class ConfigHandler {
	
	private Ecobridge ecobridge;
	
	public ConfigHandler(Ecobridge ecobridge) {
		this.ecobridge = ecobridge;
		loadConfig();
	}
	
	public void loadConfig() {
		File pluginFolder = new File("plugins" + System.getProperty("file.separator") + Ecobridge.pluginName);
		if (pluginFolder.exists() == false) {
    		pluginFolder.mkdir();
    	}
		File configFile = new File("plugins" + System.getProperty("file.separator") + Ecobridge.pluginName + System.getProperty("file.separator") + "config.yml");
		if (configFile.exists() == false) {
			Ecobridge.log.info("No config file found! Creating new one...");
			ecobridge.saveDefaultConfig();
		}
    	try {
    		Ecobridge.log.info("Loading the config file...");
    		ecobridge.getConfig().load(configFile);
    	} catch (Exception e) {
    		Ecobridge.log.severe("Could not load the config file! You need to regenerate the config! Error: " + e.getMessage());
			e.printStackTrace();
    	}
	}
	
	public String getString(String key) {
		if (!ecobridge.getConfig().contains(key)) {
			ecobridge.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Ecobridge.pluginName + " folder! (Try generating a new one by deleting the current)");
			return "errorCouldNotLocateInConfigYml:" + key;
		} else {
			return ecobridge.getConfig().getString(key);
		}
	}
	
	public Integer getInteger(String key) {
		if (!ecobridge.getConfig().contains(key)) {
			ecobridge.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Ecobridge.pluginName + " folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return ecobridge.getConfig().getInt(key);
		}
	}
	
	public Boolean getBoolean(String key) {
		if (!ecobridge.getConfig().contains(key)) {
			ecobridge.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Ecobridge.pluginName + " folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return ecobridge.getConfig().getBoolean(key);
		}
	}

}
