package net.smudgecraft.heroeslib.companions;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import net.smudgecraft.heroeslib.HeroesLib;
import net.smudgecraft.heroeslib.companions.companiontypes.Companion;
import net.smudgecraft.heroeslib.companions.companiontypes.SerializableCompanion;

import org.bukkit.entity.Player;

public class CompanionStorageManager 
{
	private HeroesLib plugin;
	
	public CompanionStorageManager(HeroesLib plugin)
	{
		this.plugin=plugin;
		
		File folder = new File(plugin.getDataFolder() + "/players");
		
		if(!folder.exists())
			folder.mkdir();
	}
	
	public void savePlayers(List<Player> players)
	{
		for(Player p : players)
		{
			if(Companions.getPlayerManager().contains(p))
			{
				for(Companion c : Companions.getPlayerManager().getCompanionPlayer(p).getCompanions())
				{
					c.save();
				}
			}
		}
	}
	
	public void saveCompanionPlayers(List<CompanionPlayer> players)
	{
		for(CompanionPlayer cplayer : players)
		{
			for(Companion c : cplayer.getCompanions())
			{
				c.save();
			}
		}
	}
	
	public void savePlayer(Player player)
	{
		if(Companions.getPlayerManager().contains(player))
		{
			for(Companion c : Companions.getPlayerManager().getCompanionPlayer(player).getCompanions())
			{
				c.save();
			}
		}
	}
	
	public void savePlayer(CompanionPlayer cplayer)
	{
		for(Companion c : cplayer.getCompanions())
		{
			c.save();
		}
	}
	
	public CompanionPlayer loadPlayer(Player player)
	{
		boolean newPlayer = false;
		
	    File folder = new File(plugin.getDataFolder() + "/players/" + Character.toLowerCase(player.getName().charAt(0)));
		 
	    if(!folder.exists())
	    	folder.mkdir();
		  
		File playerFolder = new File(folder + "/" + player.getName());
		  
		File companionFolder = new File(playerFolder + "/Companions");
		
		if(!playerFolder.exists())
		{
			playerFolder.mkdir();
			newPlayer = true;
		}
		
		if(!companionFolder.exists())
		{
			companionFolder.mkdir();
			newPlayer = true;
		}
		
		if(newPlayer)
		{
			return new CompanionPlayer(player);
		}
			
		CompanionPlayer cplayer = new CompanionPlayer(player);
		
		File[] companionFiles = companionFolder.listFiles();
		
		for(File f : companionFiles)
		{
			try 
			{
				ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
				zis.getNextEntry();
				
				ObjectInputStream ois = new ObjectInputStream(zis);
				
				SerializableCompanion sc = (SerializableCompanion) ois.readObject();
				
				cplayer.addCompanion(sc.unserialize());
				
				ois.close();
				
				f.delete();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		return cplayer;
	}
}
