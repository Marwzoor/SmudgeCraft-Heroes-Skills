package net.smudgecraft.heroeslib.companions.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import net.smudgecraft.heroeslib.companions.Companion;
import net.smudgecraft.heroeslib.companions.CompanionPlayer;
import net.smudgecraft.heroeslib.companions.Companions;

import com.herocraftonline.heroes.api.events.WeaponDamageEvent;

public class PlayerListener implements Listener
{
	@EventHandler
	public void onPlayerTameEntityEvent(EntityTameEvent event)
	{
		if(event.getEntity() instanceof Wolf)
		{
			if(event.getOwner() instanceof Player)
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	{
		CompanionPlayer cplayer = Companions.getCompanionStorageManager().loadPlayer(event.getPlayer());
		
		Companions.getPlayerManager().addCompanionPlayer(cplayer);
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event)
	{
		if(Companions.getPlayerManager().contains(event.getPlayer()))
		{
			CompanionPlayer cplayer = Companions.getPlayerManager().getCompanionPlayer(event.getPlayer());
			
			Companions.getCompanionStorageManager().savePlayer(cplayer);
			cplayer.killCompanions();
			Companions.getPlayerManager().removeCompanionPlayer(cplayer);
		}
	}
	
	@EventHandler
	public void onWeaponDamageEvent(WeaponDamageEvent event)
	{
		if(!(event.getAttackerEntity() instanceof LivingEntity))
			return;
		
		if(!Companions.getPlayerManager().hasOwner((LivingEntity) event.getAttackerEntity()))
			return;
		
		Companion companion = (Companion) Companions.getPlayerManager().getCompanionByEntity((LivingEntity) event.getAttackerEntity());
		
		if(!companion.hasCustomDamage())
			return;
		
		if(event.getEntity() instanceof Player)
		{
			if(Companions.getPlayerManager().getCompanionPlayer(companion).getPlayer().equals(event.getEntity()))
			{
				event.setCancelled(true);
				return;
			}
		}
		
		event.setDamage(companion.getDamage());
	}
	
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event)
	{
		if(Companions.getPlayerManager().hasOwner(event.getEntity()))
		{
			Companion comp = Companions.getPlayerManager().getCompanionByEntity(event.getEntity());
			
			comp.save();
				
			Companions.getPlayerManager().getCompanionPlayer(comp).removeCompanion(comp);
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
	
	@EventHandler
	public void onEntityTargetEvent(EntityTargetEvent event)
	{
		if(event.isCancelled())
			return;
		
		if(!(event.getEntity() instanceof LivingEntity))
			return;
		
		if(!(event.getTarget() instanceof Player))
			return;
		
		if(!Companions.getPlayerManager().hasOwner((LivingEntity) event.getEntity()))
			return;
		
		if(Companions.getPlayerManager().getCompanionByEntity((LivingEntity) event.getEntity()).hasOnlineOwner() && Companions.getPlayerManager().getCompanionByEntity((LivingEntity) event.getEntity()).getOwner().equals(event.getTarget()))
		{
			event.setCancelled(true);
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
