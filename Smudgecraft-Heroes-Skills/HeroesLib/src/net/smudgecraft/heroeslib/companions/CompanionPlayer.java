package net.smudgecraft.heroeslib.companions;

import java.util.ArrayList;
import java.util.List;


import net.smudgecraft.heroeslib.companions.companiontypes.Companion;

import org.bukkit.entity.Player;

public class CompanionPlayer 
{
	private final Player player;
	private List<Companion> companions;
	
	public CompanionPlayer(Player player)
	{
		this.player=player;
		companions = new ArrayList<Companion>();
	}
	
	public Player getPlayer()
	{
		return this.player;
	}
	
	public List<Companion> getCompanions()
	{
		return this.companions;
	}
	
	public void addCompanion(Companion companion)
	{
		this.companions.add(companion);
	}
	
	public void removeCompanion(Companion companion)
	{
		this.companions.remove(companion);
	}
	
	public void killCompanions()
	{
		
		if(!companions.isEmpty())
		{
			for(Companion c : companions)
			{
				c.getLivingEntity().remove();
			}
			
			companions.clear();
		}
	}
	
	public boolean hasCompanion()
	{
		return !this.companions.isEmpty();
	}
}
