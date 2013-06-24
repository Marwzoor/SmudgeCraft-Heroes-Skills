package net.smudgecraft.companions.events;

import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ImbueArrowHitEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	private final ImbueEffect ie;
	private final LivingEntity le;
	
	public ImbueArrowHitEvent(LivingEntity le, ImbueEffect ie)
	{
		this.le=le;
		this.ie=ie;
	}
	
	public ImbueEffect getImbueEffect()
	{
		return this.ie;
	}
	
	public LivingEntity getShotEntity()
	{
		return this.le;
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
