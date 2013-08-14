package net.smudgecraft.heroeslib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import net.smudgecraft.heroeslib.companions.ComWolf;
import net.smudgecraft.heroeslib.events.CompanionDeathEvent;

import com.herocraftonline.heroes.characters.skill.Skill;

public class CompanionListener implements Listener
{
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Arrow)
		{
			Arrow arrow = (Arrow) event.getDamager();
			
			if(arrow.getShooter() instanceof Player)
			{
				Player player = (Player) arrow.getShooter();
				
				if(HeroesLib.cwolves.hasWolf(player))
				{
					ComWolf cwolf = HeroesLib.cwolves.getComWolf(player);
						
					if(event.getEntity() instanceof LivingEntity)
					{
						LivingEntity target = (LivingEntity) event.getEntity();
							
						cwolf.attack(target);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerTameEntityEvent(EntityTameEvent event)
	{
		if(event.getEntity() instanceof Wolf)
		{
			if(event.getOwner() instanceof  Player)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		
		if(HeroesLib.cwolves.hasWolf(player))
		{
			ComWolf cwolf = HeroesLib.cwolves.getComWolf(player);
			
			if(player.getWorld()!=cwolf.getWolf().getWorld())
			{
				cwolf.getWolf().teleport(player);
				cwolf.getWolf().setSitting(false);
			}
			else
			{
				if(cwolf.getWolf().getLocation().distance(player.getLocation())>50)
				{
					cwolf.getWolf().teleport(player);
					cwolf.getWolf().setSitting(false);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onWolfDamageEvent(EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Wolf)
		{
			Wolf wolf = (Wolf) event.getDamager();
			if(HeroesLib.cwolves.contains(wolf))
			{
				ComWolf cwolf = HeroesLib.cwolves.getComWolf(wolf);
				if(cwolf.hasCustomDamage())
				{
					if(cwolf.hasOnlineOwner() && event.getEntity() instanceof LivingEntity)
					{
						Player player = cwolf.getWolfOwner();
						
						Skill.damageEntity((LivingEntity) event.getEntity(), player, (double) cwolf.getDamage(), DamageCause.MAGIC);
						event.setCancelled(true);
					}
					else if(event.getEntity() instanceof LivingEntity)
					{
						Skill.damageEntity((LivingEntity) event.getEntity(), cwolf.getWolf(), (double) cwolf.getDamage(), DamageCause.MAGIC);
						event.setCancelled(true);
					}
					else
					{
						event.setDamage((double) cwolf.getDamage());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event)
	{
		if(event.getEntity() instanceof Wolf)
		{
			Wolf wolf = (Wolf) event.getEntity();
			
			if(HeroesLib.cwolves.contains(wolf))
			{
				ComWolf cwolf = HeroesLib.cwolves.getComWolf(wolf);
				
				HeroesLib.saveWolf(cwolf);
				
				HeroesLib.cwolves.removeComWolf(cwolf);
				
				CompanionDeathEvent cde = new CompanionDeathEvent(cwolf);
				
				Bukkit.getPluginManager().callEvent(cde);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
	{
		if(event.getRightClicked() instanceof Wolf)
		{
			Player player = event.getPlayer();
			if(isFood(player.getItemInHand()))
			{
				player.sendMessage(ChatColor.RED + "Slapping your wolf with meat won't heal his wounds!");
				event.setCancelled(true);
			}
		}
	}
	
	public boolean isFood(ItemStack is)
	{
		int[] ids = new int[]{365,366,367,364,363};
		
		for(int id : ids)
		{
			if(is.getTypeId()==id)
				return true;
		}
		return false;
	}
}
