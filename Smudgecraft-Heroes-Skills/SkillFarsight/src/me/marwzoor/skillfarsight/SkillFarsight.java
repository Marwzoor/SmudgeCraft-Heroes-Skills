package me.marwzoor.skillfarsight;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillFarsight extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillFarsight skill;
	public static HashMap<Hero, Integer> schedules = new HashMap<Hero, Integer>();
	public static HashMap<Hero, Float> walkspeed = new HashMap<Hero, Float>(); 
	
	public SkillFarsight(Heroes instance)
	{
		super(instance, "Farsight");
		plugin=instance;
		skill=this;
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill farsight" });
		setDescription("You focus your sight to see farther.");
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(15000));
		return node;
	}
	
	public String getDescription(Hero hero)
	{
		return super.getDescription();
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		final Player player = hero.getPlayer();
		
		if(schedules.containsKey(hero) && walkspeed.containsKey(hero))
		{
			int schedule = schedules.get(hero);
			float wspeed = walkspeed.get(hero);
			
			if(player.getWalkSpeed()!=wspeed)
			{
				player.setWalkSpeed(wspeed);
				Bukkit.getScheduler().cancelTask(schedule);
				schedules.remove(hero);
				walkspeed.remove(hero);
			}
			Messaging.send(player, "You no longer have " + ChatColor.WHITE + "Farsight!");
			
			return SkillResult.NORMAL;
		}
		else
		{
			long duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 15000, false);
			duration = duration/1000;
			duration = duration*20;
			final float wspeed = player.getWalkSpeed();
			int id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					player.setWalkSpeed(wspeed);
					Messaging.send(player, "You no longer have " + ChatColor.WHITE + "Farsight!");
					if(walkspeed.containsKey(plugin.getCharacterManager().getHero(player)))
					{
						walkspeed.remove(plugin.getCharacterManager().getHero(player));
					}
					if(schedules.containsKey(plugin.getCharacterManager().getHero(player)))
					{
						schedules.remove(plugin.getCharacterManager().getHero(player));
					}
				}
			}, duration);
			player.setWalkSpeed(wspeed/10000);
			schedules.put(hero, id);
			walkspeed.put(hero, wspeed);
			Messaging.send(player, "You now have " + ChatColor.WHITE + "Farsight!");
			
			return SkillResult.NORMAL;
		}
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onPlayerLaunchArrowEvent(ProjectileLaunchEvent event)
		{
			if(event.getEntity() instanceof Arrow)
			{
				Arrow arrow = (Arrow) event.getEntity();
				if(arrow.getShooter() instanceof Player)
				{
					Player player = (Player) arrow.getShooter();
					Hero hero = plugin.getCharacterManager().getHero(player);
					if(walkspeed.containsKey(hero))
					{
						float wspeed = walkspeed.get(hero);
						if(player.getWalkSpeed()!=wspeed)
						{
							player.setWalkSpeed(wspeed);
							walkspeed.remove(hero);
							Messaging.send(player, "You no longer have " + ChatColor.WHITE + "Farsight!");
							if(schedules.containsKey(hero))
							{
								Bukkit.getScheduler().cancelTask(schedules.get(hero));
							}
						}
					}
				}
			}
		}
	}
}
