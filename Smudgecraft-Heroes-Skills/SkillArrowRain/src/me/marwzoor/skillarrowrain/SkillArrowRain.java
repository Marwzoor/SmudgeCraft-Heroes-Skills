package me.marwzoor.skillarrowrain;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.CharacterDamageManager.ProjectileType;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillArrowRain extends ActiveSkill
{
	public static Heroes plugin;
	
	public static HashMap<Hero, HashSet<Arrow>> arrows = new HashMap<Hero, HashSet<Arrow>>();
	
	public SkillArrowRain(Heroes instance)
	{
		super(instance, "ArrowRain");
		plugin=instance;
		setDescription("You make arrows rain from the sky at the target location! D:%1s CD:%2s M:%3");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill arrowrain" });
		setTypes(new SkillType[] { SkillType.DAMAGING });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this)) {
			String desc = super.getDescription();
			int duration = (SkillConfigManager.getUseSetting(hero,  this, SkillSetting.DURATION, Integer.valueOf(10000), false) + SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(10), false) * hero.getSkillLevel(this)) / 1000;
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, Integer.valueOf(0), false) / 1000;
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, Integer.valueOf(0), false);
			return desc.replace("%1", duration + "").replace("%2", cooldown + "").replace("%3", mana + "");
		} else {
			return super.getDescription().replace("%1", "X").replace("%2", "X").replace("%3", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(10000));
		node.set(SkillSetting.DURATION_INCREASE.node(), Integer.valueOf(10));
		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(0));
		node.set(SkillSetting.MANA.node(), Integer.valueOf(0));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		Player player = hero.getPlayer();
		
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(10000), false);
		duration += SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(10), false) * hero.getSkillLevel(this);
		
		duration = duration/1000;
		duration = duration*20;
		
		Block block = player.getTargetBlock(null, 40);
		Location loc = block.getLocation();
		
		Location loc1 = new Location(loc.getWorld(), loc.getBlockX()+7, loc.getWorld().getMaxHeight()-100, loc.getBlockZ()+7);
		Location loc2 = new Location(loc.getWorld(), loc.getBlockX()-7, loc.getWorld().getMaxHeight()-100, loc.getBlockZ()-7);
		
		final CuboidArea ca = new CuboidArea(loc1, loc2);
		final Hero her = hero;
		
		HashSet<Arrow> ars = new HashSet<Arrow>();
		arrows.put(hero, ars);
		
		final int id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			public void run()
			{
				Location loc = ca.getRandomLocation();
				Arrow arrow = loc.getWorld().spawn(loc, Arrow.class);
				arrow.setShooter(her.getPlayer());
				arrow.setVelocity(new Vector(0,-2,0));
				arrow.setBounce(false);
				arrow.setDamage(her.getHeroClass().getProjectileDamage(ProjectileType.ARROW));
				final Arrow ar = arrow;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
						ar.remove();
					}
				}, 20L*6);
				
				
				if(arrows.containsKey(her))
				{
				arrows.get(her).add(arrow);
				}
				else
				{
					HashSet<Arrow> ars = new HashSet<Arrow>();
					ars.add(arrow);
					arrows.put(her, ars);
				}
			}
		}, 0L, 2L);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				Bukkit.getScheduler().cancelTask(id);
				broadcast(her.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " +  her.getPlayer().getDisplayName() + ChatColor.GRAY + " is no longer calling down arrows from the sky!");		
			}
		}, duration);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				arrows.remove(her);
			}
		}, duration+(20L*5));
		broadcast(player.getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " +  player.getDisplayName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "ArrowRain" + ChatColor.GRAY + "!");		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onPlayerShotEvent(EntityDamageByEntityEvent event)
		{
			if(event.getDamager() instanceof Arrow)
			{
				Arrow arrow = (Arrow) event.getDamager();
				if(arrow.getShooter() instanceof Player)
				{
					Player player = (Player) arrow.getShooter();
					if(event.getEntity().equals(player))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}
}
