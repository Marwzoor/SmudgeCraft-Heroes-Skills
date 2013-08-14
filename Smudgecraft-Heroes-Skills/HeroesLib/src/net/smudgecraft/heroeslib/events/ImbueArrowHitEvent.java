package net.smudgecraft.heroeslib.events;

import net.smudgecraft.heroeslib.commoneffects.ImbueEffect;

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
