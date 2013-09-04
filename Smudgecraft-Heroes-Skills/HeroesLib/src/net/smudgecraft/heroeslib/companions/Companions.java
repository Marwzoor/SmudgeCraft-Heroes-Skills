package net.smudgecraft.heroeslib.companions;

import net.smudgecraft.heroeslib.HeroesLib;
import net.smudgecraft.heroeslib.companions.listeners.PlayerListener;

import org.bukkit.Bukkit;

public class Companions 
{
	private static PlayerManager playerManager;
	private static CompanionStorageManager companionStorageManager;
	
	public static void init()
	{		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), HeroesLib.plugin);
		
		playerManager = new PlayerManager();
		companionStorageManager = new CompanionStorageManager(HeroesLib.plugin);
	}
	
	
	public static PlayerManager getPlayerManager()
	{
		return playerManager;
	}
	
	public static CompanionStorageManager getCompanionStorageManager()
	{
		return companionStorageManager;
	}
}
