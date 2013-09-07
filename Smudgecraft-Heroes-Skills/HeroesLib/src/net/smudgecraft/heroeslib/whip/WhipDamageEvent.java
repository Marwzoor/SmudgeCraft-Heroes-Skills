package net.smudgecraft.heroeslib.whip;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WhipDamageEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private final Player attacker;
	private final LivingEntity target;
	private double damage;
	private boolean cancel;
	
	public WhipDamageEvent(Player attacker, LivingEntity target, double damage)
	{
		this.attacker = attacker;
		this.target = target;
		this.damage = damage;
	}
	
	public Player getAttacker()
	{
		return attacker;
	}
	
	public LivingEntity getTarget()
	{
		return target;
	}
	
	public double getDamage()
	{
		return damage;
	}
	
	public void setDamage(double damage)
	{
		this.damage = damage;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
	
	public HandlerList getHandlers()
	{
		return handlers;
	}

	@Override
	public boolean isCancelled()
	{
		return cancel;
	}

	@Override
	public void setCancelled(boolean value)
	{
		cancel = value;
	}
}
