package net.smudgecraft.heroeslib.companions;

import java.util.ArrayList;
import java.util.List;

import net.smudgecraft.heroeslib.HeroesLib.WolfType;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

public class ComWolves 
{
	protected List<ComWolf> cwolves;
	
	public ComWolves()
	{
		this.cwolves = new ArrayList<ComWolf>();
	}
	
	public boolean contains(ComWolf cwolf)
	{
		if(cwolves.contains(cwolf))
		{
			return true;
		}
		return false;
	}
	
	public boolean contains(Wolf wolf)
	{
		for(ComWolf cwolf : cwolves)
		{
			if(cwolf.getWolf().equals(wolf))
			{
				return true;
			}
		}
		return false;
	}
	
	public void addComWolf(ComWolf cwolf)
	{
		cwolves.add(cwolf);
	}
	
	public void removeComWolf(ComWolf cwolf)
	{
		cwolves.remove(cwolf);
	}
	
	public void removeComWolf(Wolf wolf)
	{
		for(ComWolf cwolf : cwolves)
		{
			if(cwolf.getWolf().equals(wolf))
			{
				cwolves.remove(cwolf);
				continue;
			}
		}
	}
	
	public void clear()
	{
		cwolves.clear();
	}
	
	public ComWolf getComWolf(Wolf wolf)
	{
		for(ComWolf cwolf : cwolves)
		{
			if(cwolf.getWolf().equals(wolf))
			{
				return cwolf;
			}
		}
		return null;
	}
	
	public ComWolf getComWolf(Player player)
	{
		for(ComWolf cwolf : cwolves)
		{
			if(cwolf.getWolfOwnerName().equals(player.getName()))
			{
				return cwolf;
			}
		}
		return null;
	}
	
	public boolean hasWolf(Player player)
	{
		for(ComWolf cwolf : cwolves)
		{
			if(cwolf.getWolfOwnerName().equals(player.getName()))
			{
				return true;
			}
		}
		return false;
	}
	
	public List<ComWolf> getComWolves()
	{
		return this.cwolves;
	}
	
	public List<Wolf> getWolves()
	{
		List<Wolf> wolflist = new ArrayList<Wolf>();
		for(ComWolf cwolf : cwolves)
		{
			wolflist.add(cwolf.getWolf());
		}
		return wolflist;
	}
	
	public void killWolves(WolfType wt)
	{		
		if(wt.equals(WolfType.COMWOLF))
		{
			for(Wolf wolf : getWolves())
			{
				wolf.remove();
				if(contains(wolf))
				{
					ComWolf cwolf = getComWolf(wolf);
					removeComWolf(cwolf);
				}
			}
		}
		else if(wt.equals(WolfType.WILDWOLF))
		{
		for(World w : Bukkit.getWorlds())
		{

			for(Entity e : w.getEntities())
			{
				if(e instanceof Wolf)
				{
					e.remove();
				}
			}
		}
		}
		else if(wt.equals(WolfType.TAMEDWOLF))
		{
			for(World w : Bukkit.getWorlds())
			{

				for(Entity e : w.getEntities())
				{
					if(e instanceof Wolf)
					{
						Wolf wolf = (Wolf) e;
						if(wolf.isTamed())
						{
							wolf.remove();
						}
					}
				}
			}
		}
	}
}
