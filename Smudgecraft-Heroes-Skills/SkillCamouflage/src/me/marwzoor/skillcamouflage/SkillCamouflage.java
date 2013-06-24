package me.marwzoor.skillcamouflage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillCamouflage extends PassiveSkill
{
	public static Heroes plugin;
	public static SkillCamouflage skill;
	public static List<Player> sneaking = new ArrayList<Player>();
	
	public static HashMap<Hero, HashSet<Arrow>> arrows = new HashMap<Hero, HashSet<Arrow>>();
	
	public SkillCamouflage(Heroes instance)
	{
		super(instance, "Camouflage");
		plugin=instance;
		skill=this;
		setDescription("When you are sneaking on leaves you blend into the environment, making you invisable");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill arrowrain" });
		setTypes(new SkillType[] { SkillType.BUFF });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		return desc;
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		return node;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event)
		{
			Player player = event.getPlayer();
			Hero hero = plugin.getCharacterManager().getHero(player);
			
			if(hero.hasAccessToSkill(skill))
			{
				if(event.isSneaking())
				{
				Location loc1 = player.getLocation();
				Location loc2 = new Location(loc1.getWorld(), loc1.getBlockX(), loc1.getBlockY()-1, loc1.getBlockZ());
				Block block = loc2.getBlock();
				
				if(block.getType().equals(Material.LEAVES))
				{
					for(Player p : player.getWorld().getPlayers())
					{
						p.hidePlayer(player);
					}
					sneaking.add(player);
					
					player.sendMessage(ChatColor.GRAY + "You are now" + ChatColor.WHITE + " Camouflaged" + ChatColor.GRAY + "!");
				}
				}
				else
				{
					if(sneaking.contains(player))
					{
						for(Player p : player.getWorld().getPlayers())
						{
							p.showPlayer(player);
						}
						sneaking.remove(player);
						player.sendMessage(ChatColor.GRAY + "You are no longer" + ChatColor.WHITE + " Camouflaged" + ChatColor.GRAY + "!");
					}
				}
				
			}
		}
		
		@EventHandler
		public void onPlayerMoveEvent(PlayerMoveEvent event)
		{
			Player player = event.getPlayer();
			Hero hero = plugin.getCharacterManager().getHero(player);
			
			if(hero.hasAccessToSkill(skill))
			{
			if(sneaking.contains(player))
			{
				Location loc1 = event.getTo();
				Location loc2 = new Location(loc1.getWorld(), loc1.getBlockX(), loc1.getBlockY()-1, loc1.getBlockZ());
				
				Block block = loc2.getBlock();
				
				if(!block.getType().equals(Material.LEAVES) || player.isSneaking()==false)
				{
					for(Player p : player.getWorld().getPlayers())
					{
						p.showPlayer(player);
					}
					sneaking.remove(player);
					player.sendMessage(ChatColor.GRAY + "You are no longer" + ChatColor.WHITE + " Camouflaged" + ChatColor.GRAY + "!");
				}
			}
			else
			{
				Location loc1 = event.getTo();
				Location loc2 = new Location(loc1.getWorld(), loc1.getBlockX(), loc1.getBlockY()-1, loc1.getBlockZ());
				
				Block block = loc2.getBlock();
				
				if(block.getType().equals(Material.LEAVES) && player.isSneaking()==true)
				{
					for(Player p : player.getWorld().getPlayers())
					{
						p.hidePlayer(player);
					}
					sneaking.add(player);
					
					player.sendMessage(ChatColor.GRAY + "You are now" + ChatColor.WHITE + " Camouflaged" + ChatColor.GRAY + "!");
				}
			}
			}
		}
		
		@EventHandler
		public void onPlayerTeleportEvent(PlayerTeleportEvent event)
		{
			Player player = event.getPlayer();
			Hero hero = plugin.getCharacterManager().getHero(player);
			
			if(hero.hasAccessToSkill(skill))
			{
			if(sneaking.contains(player))
			{
				Location loc1 = event.getTo();
				Location loc2 = new Location(loc1.getWorld(), loc1.getBlockX(), loc1.getBlockY()-1, loc1.getBlockZ());
				
				Block block = loc2.getBlock();
				
				if(!block.getType().equals(Material.LEAVES) || player.isSneaking()==false)
				{
					for(Player p : event.getFrom().getWorld().getPlayers())
					{
						p.showPlayer(player);
					}
					sneaking.remove(player);
					player.sendMessage(ChatColor.GRAY + "You are no longer" + ChatColor.WHITE + " Camouflaged" + ChatColor.GRAY + "!");
				}
			}
			else
			{
				Location loc1 = event.getTo();
				Location loc2 = new Location(loc1.getWorld(), loc1.getBlockX(), loc1.getBlockY()-1, loc1.getBlockZ());
				
				Block block = loc2.getBlock();
				
				if(block.getType().equals(Material.LEAVES) && player.isSneaking()==true)
				{
					for(Player p : event.getTo().getWorld().getPlayers())
					{
						p.hidePlayer(player);
					}
					sneaking.add(player);
					
					player.sendMessage(ChatColor.GRAY + "You are now" + ChatColor.WHITE + " Camouflaged" + ChatColor.GRAY + "!");
				}
			}
			}
		}
		
		@EventHandler
		public void onPlayerQuitEvent(PlayerQuitEvent event)
		{
			Player player = event.getPlayer();
			
			if(sneaking.contains(player))
			{
				for(Player p : Bukkit.getOnlinePlayers())
				{
					player.showPlayer(p);
				}
				sneaking.remove(player);
			}
		}
		
		@EventHandler
		public void onEntityTargetEvent(EntityTargetEvent event)
		{
			if(event.getTarget() instanceof Player)
			{
				Player player = (Player) event.getTarget();
				
				if(sneaking.contains(player))
				{
					event.setCancelled(true);
				}
			}
		}
	}
}
