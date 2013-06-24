package me.marwzoor.skillsnipe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillSnipe extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillSnipe skill;
	
	public SkillSnipe(Heroes instance)
	{
		super(instance, "Snipe");
		plugin=instance;
		skill=this;
		setDescription("The next arrow you fire deals $1% more damage. Snipe lasts for $2 seconds or until you use the skill again.");
		setUsage("/skill snipe");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill snipe" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT});
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		double duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		double percentage = SkillConfigManager.getUseSetting(hero, skill, "percentage", Integer.valueOf(20), false);
		percentage += SkillConfigManager.getUseSetting(hero, skill, "percentage-increase", Integer.valueOf(1), false) * hero.getSkillLevel(skill);
		duration = duration/1000;
		desc = desc.replace("$1", percentage + "");
		desc = desc.replace("$2", duration + "");
		return desc;
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(30000));
		node.set("percentage", Integer.valueOf(20));
		node.set("percentage-increase", Integer.valueOf(1));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		int percentage = SkillConfigManager.getUseSetting(hero, skill, "percentage", Integer.valueOf(20), false);
		percentage += SkillConfigManager.getUseSetting(hero, skill, "percentage-increase", Integer.valueOf(1), false) * hero.getSkillLevel(skill);
		
		if(hero.hasEffect("Snipe"))
		{
			hero.removeEffect(hero.getEffect("Snipe"));
		}
		else
		{
		SnipeEffect sEffect = new SnipeEffect(skill, duration, percentage);
		
		hero.addEffect(sEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "Snipe" + ChatColor.GRAY + "! You cannot move but you deal more damage!", new Object());
		}
		return SkillResult.NORMAL;
	}
	
	public class SnipeEffect extends ExpirableEffect
	{
		private int percentage;
		public SnipeEffect(Skill skill, int duration, int percentage)
		{
			super(skill, "Snipe", duration);
			this.percentage=percentage;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
		}
		
		public void removeFromHero(Hero hero)
		{
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "Snipe" + ChatColor.GRAY + "!");
			hero.setCooldown("Snipe", SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, Integer.valueOf(20000), false));
			super.removeFromHero(hero);
		}
		
		public int getPercentage()
		{
			return this.percentage;
		}
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler(priority = EventPriority.HIGH)
		public void onPlayerMoveEvent(PlayerMoveEvent event)
		{
			Player player = event.getPlayer();
			Hero hero = plugin.getCharacterManager().getHero(player);
			if(hero.hasEffect("Snipe"))
			{
			Location from = event.getFrom();
			Location to = event.getTo();
			if(from.getBlockY()<to.getBlockY() || from.getBlockX()!=to.getBlockX() || from.getBlockZ()!=to.getBlockZ())
			{
				Messaging.send(event.getPlayer(), "You can't move while you are sniping!");
				SnipeEffect sEffect = (SnipeEffect) hero.getEffect("Snipe");
				hero.removeEffect(sEffect);
			}
			}
		}
		
		@EventHandler
		public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
		{
			if(event.getDamager() instanceof Arrow)
			{
				Arrow arrow = (Arrow) event.getDamager();
				if(arrow.getShooter() instanceof Player)
				{
					Player player = (Player) arrow.getShooter();
					Hero hero = plugin.getCharacterManager().getHero(player);
					
					if(hero.hasEffect("Snipe"))
					{
						SnipeEffect sEffect = (SnipeEffect) hero.getEffect("Snipe");
						double damage = event.getDamage();
						double percentage = sEffect.getPercentage();
						percentage=(percentage/100)+1;
						damage = damage*percentage;
						event.setDamage((int) damage);
					}
				}
			}
		}
	}
}
