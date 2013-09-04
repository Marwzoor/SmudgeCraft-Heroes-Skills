package net.smudgecraft.heroeslib.companions;

import java.util.ArrayList;
import java.util.List;


import net.smudgecraft.heroeslib.companions.companiontypes.Companion;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerManager 
{
	private List<CompanionPlayer> cplayers;
	
	public PlayerManager()
	{
		this.cplayers = new ArrayList<CompanionPlayer>();
	}
	
	public void addCompanionPlayer(CompanionPlayer cplayer)
	{
		this.cplayers.add(cplayer);
	}
	
	public void removeCompanionPlayer(CompanionPlayer cplayer)
	{
		this.cplayers.remove(cplayer);
	}
	
	public void removeCompanionPlayer(Player player)
	{
		CompanionPlayer toRemove = null;
		for(CompanionPlayer cplayer : cplayers)
		{
			if(cplayer.getPlayer().equals(player))
			{
				toRemove=cplayer;
				continue;
			}
		}
		
		cplayers.remove(toRemove);
	}
	
	public CompanionPlayer getCompanionPlayer(Player player)
	{
		for(CompanionPlayer cplayer : cplayers)
		{
			if(cplayer.getPlayer().equals(player))
				return cplayer;
		}
		return null;
	}
	
	public boolean hasOwner(LivingEntity livingEntity)
	{
		for(CompanionPlayer cplayer : cplayers)
		{
			for(Companion companion : cplayer.getCompanions())
			{
				if(companion.getLivingEntity().equals(livingEntity))
					return true;
			}
		}
		return false;
	}
	
	public Companion getCompanionByEntity(LivingEntity livingEntity)
	{
		for(CompanionPlayer cplayer : cplayers)
		{
			for(Companion companion : cplayer.getCompanions())
			{
				if(companion.getLivingEntity().equals(livingEntity))
					return companion;
			}
		}
		return null;
	}
	
	public List<CompanionPlayer> getCompanionPlayers()
	{
		return this.cplayers;
	}
	
	public boolean contains(Player player)
	{
		for(CompanionPlayer cplayer : cplayers)
		{
			if(cplayer.getPlayer().equals(player))
				return true;
		}
		return false;
	}
	
	public boolean contains(CompanionPlayer cplayer)
	{
		return this.cplayers.contains(cplayer);
	}
	
	public CompanionPlayer getCompanionPlayer(Companion companion)
	{
		for(CompanionPlayer cplayer : cplayers)
		{
			if(cplayer.getCompanions().contains(companion))
				return cplayer;
		}
		
		return null;
	}
}
