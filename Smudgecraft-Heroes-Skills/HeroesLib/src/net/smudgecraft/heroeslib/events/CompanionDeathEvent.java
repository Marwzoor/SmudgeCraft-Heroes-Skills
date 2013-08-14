package net.smudgecraft.heroeslib.events;

import net.smudgecraft.heroeslib.ComWolf;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CompanionDeathEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private final ComWolf cwolf;
	
	public CompanionDeathEvent(ComWolf cwolf)
	{
		this.cwolf=cwolf;
	}
	
	public ComWolf getComWolf()
	{
		return this.cwolf;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}
}
