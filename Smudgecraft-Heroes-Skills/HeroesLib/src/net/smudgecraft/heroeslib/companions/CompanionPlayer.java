package net.smudgecraft.heroeslib.companions;

import java.util.ArrayList;
import java.util.List;



import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
	
	public boolean hasCompanionOfType(EntityType entityType)
	{
		for(Companion c : companions)
		{
			if(c.getLivingEntity() != null && c.getLivingEntity().getType().equals(entityType))
				return true;
		}
		
		return false;
	}
	
	public boolean hasSpecificCompanion(LivingEntity livingEntity)
	{
		for(Companion c : companions)
		{
			if(c.getLivingEntity() != null && c.getLivingEntity().equals(livingEntity))
				return true;
		}
		
		return false;
	}
	
	public Companion getCompanion(LivingEntity livingEntity)
	{
		for(Companion c : companions)
		{
			if(c.getLivingEntity() != null && c.getLivingEntity().equals(livingEntity))
				return c;
		}
		
		return null;
	}
	
	public List<Companion> getCompanionsOfType(EntityType entityType)
	{
		List<Companion> comps = new ArrayList<Companion>();
		
		for(Companion c : companions)
		{
			if(c.getLivingEntity() != null && c.getLivingEntity().getType().equals(entityType))
				comps.add(c);
		}
		
		return comps;
	}
	
	public int getAmountOfType(EntityType entityType)
	{
		int count=0;
		
		for(Companion c : companions)
		{
			if(c.getLivingEntity() != null && c.getLivingEntity().getType().equals(entityType))
				++count;
		}
		
		return count;
	}
	
	public Companion getFirstCompanionOfType(EntityType entityType)
	{
		for(Companion c : companions)
		{
			if(c.getLivingEntity() != null && c.getLivingEntity().getType().equals(entityType))
				return c;
		}
		
		return null;
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
