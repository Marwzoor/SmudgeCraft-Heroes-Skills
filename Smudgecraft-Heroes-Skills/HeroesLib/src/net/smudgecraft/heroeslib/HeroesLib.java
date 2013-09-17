package net.smudgecraft.heroeslib;

import java.util.logging.Level;

import net.smudgecraft.heroeslib.companions.CompanionPlayer;
import net.smudgecraft.heroeslib.companions.Companions;
import net.smudgecraft.heroeslib.listeners.ArrowListener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.herocraftonline.heroes.Heroes;

public class HeroesLib extends JavaPlugin
{
	public static HeroesLib plugin;
	public static Heroes heroes;
	
	@Override
	public void onEnable()
	{
		plugin=this;
		
		if(!Bukkit.getPluginManager().isPluginEnabled("Heroes"))
		{
			Bukkit.getLogger().log(Level.SEVERE, "Could not find dependency: Heroes.jar! Disabling HeroesLib...");
			this.setEnabled(false);
			return;
		}
		
		if(!Bukkit.getPluginManager().isPluginEnabled("Factions"))
		{
			Bukkit.getLogger().log(Level.SEVERE, "Could not find dependency: Factions.jar! Disabling HeroesLib...");
			this.setEnabled(false);
			return;
		}
		
		if(!Bukkit.getPluginManager().isPluginEnabled("mcore"))
		{
			Bukkit.getLogger().log(Level.SEVERE, "Could not find dependency: MCore.jar! Disabling HeroesLib...");
			this.setEnabled(false);
			return;
		}
		
		heroes = (Heroes) Bukkit.getPluginManager().getPlugin("Heroes");
		
		Companions.init();
		Bukkit.getPluginManager().registerEvents(new ArrowListener(), plugin);
	}
	
	@Override
	public void onDisable()
	{
		Companions.getCompanionStorageManager().saveCompanionPlayers(Companions.getPlayerManager().getCompanionPlayers());
		
		for(CompanionPlayer cplayer : Companions.getPlayerManager().getCompanionPlayers())
		{
			cplayer.killCompanions();
		}
		
		Companions.getPlayerManager().getCompanionPlayers().clear();
	}
}
