package net.smudgecraft.heroeslib;

import java.util.logging.Level;

import net.smudgecraft.heroeslib.companions.CompanionPlayer;
import net.smudgecraft.heroeslib.companions.Companions;
import net.smudgecraft.heroeslib.listeners.ArrowListener;

import org.bukkit.Bukkit;
import org.bukkit.Server;
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
		heroes = (Heroes) Bukkit.getPluginManager().getPlugin("Heroes");
		
		if(heroes==null)
		{
			Bukkit.getLogger().log(Level.SEVERE, "Could not find dependency: Heroes.jar! Disabling HeroesLib...");
			this.setEnabled(false);
			return;
		}
		
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
	
	public static Server getServ()
	{
		return Bukkit.getServer();
	}
}
