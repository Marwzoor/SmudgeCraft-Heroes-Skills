package net.smudgecraft.companions.events;

import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ImbueArrowLaunchEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private final ImbueEffect ie;
	private final Arrow ar;
	
	public ImbueArrowLaunchEvent(Arrow arrow, ImbueEffect ie)
	{
		this.ar=arrow;
		this.ie=ie;
	}
	
	public ImbueEffect getImbueEffect()
	{
		return this.ie;
	}
	
	public Player getPlayer()
	{
		return this.ie.getPlayer();
	}
	
	public Arrow getArrow()
	{
		return this.ar;
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
