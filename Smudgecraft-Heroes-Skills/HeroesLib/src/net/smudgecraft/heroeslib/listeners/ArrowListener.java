package net.smudgecraft.heroeslib.listeners;

import net.smudgecraft.heroeslib.Companions;
import net.smudgecraft.heroeslib.commoneffects.ImbueEffect;
import net.smudgecraft.heroeslib.events.ImbueArrowHitEvent;
import net.smudgecraft.heroeslib.events.ImbueArrowLaunchEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;

public class ArrowListener implements Listener
{
	@EventHandler
	public void onPlayerHitArrowEvent(WeaponDamageEvent event)
	{
		if(event.getAttackerEntity() instanceof Arrow)
		{
			Arrow arrow = (Arrow) event.getAttackerEntity();
			
			if(arrow.getShooter() instanceof Player)
			{
				Player player = (Player) arrow.getShooter();
				Hero hero = Companions.heroes.getCharacterManager().getHero(player);
				if(hasImbueEffect(hero) && event.getEntity() != player)
				{
					ImbueEffect ie = getImbueEffect(hero);
					if(event.getEntity() instanceof LivingEntity)
					{
					ImbueArrowHitEvent iahe = new ImbueArrowHitEvent((LivingEntity) event.getEntity(), ie);
					Bukkit.getPluginManager().callEvent(iahe);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLaunchArrowEvent(ProjectileLaunchEvent event)
	{
		if(event.getEntity() instanceof Arrow)
		{
			Arrow arrow = (Arrow) event.getEntity();
			
			if(arrow.getShooter() instanceof Player)
			{
				Player player = (Player) arrow.getShooter();
				Hero hero = Companions.heroes.getCharacterManager().getHero(player);
				
				if(hasImbueEffect(hero))
				{
					ImbueEffect ie = getImbueEffect(hero);
					
					ImbueArrowLaunchEvent iale = new ImbueArrowLaunchEvent(arrow, ie);
					Bukkit.getPluginManager().callEvent(iale);
				}
			}
		}
	}
	
	public boolean hasImbueEffect(Hero hero)
	{
		for(Effect effect : hero.getEffects())
		{
			if(effect instanceof ImbueEffect)
				return true;
		}
		return false;
	}
	
	public ImbueEffect getImbueEffect(Hero hero)
	{
		for(Effect effect : hero.getEffects())
		{
			if(effect instanceof ImbueEffect)
			{
				return (ImbueEffect) effect;
			}
		}
		return null;
	}
}
