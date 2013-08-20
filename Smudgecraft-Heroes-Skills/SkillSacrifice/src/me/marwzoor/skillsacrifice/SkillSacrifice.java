package me.marwzoor.skillsacrifice;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.CharacterDamageEvent;
import com.herocraftonline.heroes.api.events.SkillDamageEvent;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillSacrifice extends TargettedSkill
{
	public final HashMap<Player, String> players = new HashMap<Player, String>();
	public final HashMap<Player, Integer> schedules = new HashMap<Player, Integer>();
	
	public SkillSacrifice(Heroes instance)
	{
		super(instance, "Sacrifice");
		setDescription("Recieve the damage your target would recieve. Toggle-able.");
		setUsage("/skill sacrifice");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill sacrifice" });
		setTypes(new SkillType[] { SkillType.HARMFUL, SkillType.KNOWLEDGE });

		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		return super.getDescription();
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		return node;
	}
	
	public SkillResult use(final Hero hero, LivingEntity target, String[] args)
	{
		if(players.containsKey(hero.getPlayer()))
		{
			if(target==null || target.equals(hero.getPlayer()) || !(target instanceof Player))
			{
				Messaging.send(hero.getPlayer(), "You are no longer sacrificing yourself for " + ChatColor.DARK_RED + players.get(hero.getPlayer()) + ChatColor.GRAY + "!");
				players.remove(hero.getPlayer());
				
				if(schedules.containsKey(hero.getPlayer()))
				{
					Bukkit.getScheduler().cancelTask(schedules.get(hero.getPlayer()));
					schedules.remove(hero.getPlayer());
				}
			}
			else
			{
				Player tplayer = (Player) target;
								
				Messaging.send(hero.getPlayer(), "You now sacrifice yourself for " + ChatColor.DARK_RED + tplayer.getName() + ChatColor.GRAY +  " instead of " + ChatColor.DARK_RED + players.get(hero.getPlayer()) + ChatColor.GRAY + " and recieve their damage taken!");
				
				Messaging.send(tplayer, ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " is now recieving the damage you take!");
				
				players.put(hero.getPlayer(), tplayer.getName());
				
				if(schedules.containsKey(hero.getPlayer()))
				{
					Bukkit.getScheduler().cancelTask(schedules.get(hero.getPlayer().getName()));
				}
				
				int id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
						if(hero.getPlayer()!=null)
						{
							if(players.containsKey(hero.getPlayer()))
							{
								Messaging.send(hero.getPlayer(), "You are no longer sacrificing yourself for " + ChatColor.DARK_RED + players.get(hero.getPlayer()) + ChatColor.GRAY + "!");
								players.remove(hero.getPlayer());
							}
							
							if(schedules.containsKey(hero.getPlayer()))
							{
								schedules.remove(hero.getPlayer());
							}
						}
					}
				},20L*60*5);
				
				schedules.put(hero.getPlayer(), id);
			}
		}
		else
		{
			if(target==null)
			{
				return SkillResult.FAIL;
			}
			else if(target.equals(hero.getPlayer()))
			{
				return SkillResult.FAIL;
			}
			else if(!(target instanceof Player))
			{
				return SkillResult.INVALID_TARGET;
			}
			
			Player tplayer = (Player) target;
			
			Messaging.send(hero.getPlayer(), "You now sacrifice yourself for " + ChatColor.DARK_RED + tplayer.getName() + ChatColor.GRAY +  "! recieving all their damage taken!");
			
			Messaging.send(tplayer, ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " is now recieving the damage you take!");
			
			players.put(hero.getPlayer(), tplayer.getName());
			
			if(schedules.containsKey(hero.getPlayer()))
			{
				Bukkit.getScheduler().cancelTask(schedules.get(hero.getPlayer().getName()));
			}
			
			int id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					if(hero.getPlayer()!=null)
					{
						if(players.containsKey(hero.getPlayer()))
						{
							Messaging.send(hero.getPlayer(), "You are no longer sacrificing yourself for " + ChatColor.DARK_RED + players.get(hero.getPlayer()) + ChatColor.GRAY + "!");
							players.remove(hero.getPlayer());
						}
						
						if(schedules.containsKey(hero.getPlayer()))
						{
							schedules.remove(hero.getPlayer());
						}
					}
				}
			},20L*60*5);
			
			schedules.put(hero.getPlayer(), id);
		}
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onPlayerQuitEvent(PlayerQuitEvent event)
		{
			Player player = event.getPlayer();
			
			if(players.containsKey(player))
			{
				if(Bukkit.getOfflinePlayer(players.get(player)).isOnline())
				{
					Messaging.send(Bukkit.getPlayer(players.get(player)), ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + " has logged out and is no longer recieving the damage you take!");
				}
				
				players.remove(player);
				
				if(schedules.containsKey(player))
				{
					Bukkit.getScheduler().cancelTask(schedules.get(player));
					schedules.remove(player);
				}
			}
			else if(players.containsValue(player.getName()))
			{
				Player tempPlayer = null;
				
				for(Player p : players.keySet())
				{
					if(players.get(p).equals(player.getName()))
					{
						Messaging.send(p, ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + " has logged out and you are no longer recieving damage they take!");
						tempPlayer = p;
					}
				}
				
				if(tempPlayer!=null)
				{
					players.remove(tempPlayer);
					
					if(schedules.containsKey(tempPlayer))
					{
						Bukkit.getScheduler().cancelTask(schedules.get(tempPlayer));
						schedules.remove(tempPlayer);
					}
				}
			}
		}
		
		@EventHandler
		public void onCharacterDamageEvent(CharacterDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(event.getEntity() instanceof Player)
			{
				Player player = (Player) event.getEntity();
				
				if(players.containsValue(player.getName()))
				{
					for(Player p : players.keySet())
					{
						if(players.get(p).equals(player.getName()))
						{	
							double damage = event.getDamage();
							
							p.damage(damage);
							
							event.setCancelled(true);
							
							return;
						}
					}
				}
			}
		}
		
		@EventHandler
		public void onSkillDamageEvent(SkillDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(event.getEntity() instanceof Player)
			{
				Player player = (Player) event.getEntity();
				
				if(players.containsValue(player.getName()))
				{
					for(Player p : players.keySet())
					{
						if(players.get(p).equals(player.getName()))
						{	
							double damage = event.getDamage();
							
							Skill.damageEntity(p, event.getDamager().getEntity(), damage, DamageCause.MAGIC);
							
							event.setCancelled(true);
							
							return;
						}
					}
				}
			}
		}
		
		@EventHandler
		public void onWeaponDamage(WeaponDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(event.getEntity() instanceof Player)
			{
				Player player = (Player) event.getEntity();
				
				if(players.containsValue(player.getName()))
				{
					for(Player p : players.keySet())
					{
						if(players.get(p).equals(player.getName()))
						{	
							double damage = event.getDamage();
							
							if(event.getAttackerEntity() instanceof Projectile)
							{
								Skill.damageEntity(p, ((Projectile) event.getAttackerEntity()).getShooter(), damage, event.getCause());
							}
							else if(event.getAttackerEntity() instanceof LivingEntity)
							{
								Skill.damageEntity(p, (LivingEntity) event.getAttackerEntity(), damage, event.getCause());
							}
							
							event.setCancelled(true);
							
							return;
						}
					}
				}
			}
		}
		
		@EventHandler
		public void onPlayerDeathEvent(PlayerDeathEvent event)
		{
			Player player = event.getEntity();
			
			if(players.containsKey(player))
			{
				Player tplayer = Bukkit.getPlayer(players.get(player));
				
				if(tplayer!=null)
				{
					Messaging.send(tplayer, ChatColor.DARK_RED + player.getName() + ChatColor.GRAY + "died! They are no longer recieving your damage taken!");
					Messaging.send(player, "You died! You are no longer recieving the damage " + ChatColor.DARK_RED + players.get(player) + ChatColor.GRAY + " is taking!");

					players.remove(player);
					
					if(schedules.containsKey(player))
					{
						Bukkit.getScheduler().cancelTask(schedules.get(player));
						schedules.remove(player);
					}
				}
			}
		}
	}
}
