package me.marwzoor.skillhowl;

import net.smudgecraft.companions.ComWolf;
import net.smudgecraft.companions.Companions;
import net.smudgecraft.companions.util.ParticleEffects;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillHowl extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillHowl skill;
	
	public SkillHowl(Heroes instance)
	{
		super(instance, "Howl");
		plugin=instance;
		skill=this;
		setDescription("Your companion howls, boosting attack damage to %1%");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill howl" });
		setTypes(new SkillType[] { SkillType.BUFF });
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		double damageperc = SkillConfigManager.getUseSetting(hero, skill, "damage-percent", Double.valueOf(1.25), false);
		damageperc = SkillConfigManager.getUseSetting(hero, skill, "damage-percent-increase", Double.valueOf(0.01), false) * hero.getSkillLevel(skill);
		damageperc = damageperc*100;
		return desc.replace("%1", damageperc + "");
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("damage-percent", Double.valueOf(1.25));
		node.set("damage-percent-increase", Double.valueOf(0.01));
		node.set("duration", Integer.valueOf(10000));
		node.set("duration-increase", Integer.valueOf(10));
		return super.getDefaultConfig();
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		final Player player = hero.getPlayer();
		
		if(Companions.cwolves.hasWolf(player))
		{
			final ComWolf cwolf = Companions.cwolves.getComWolf(player);
			
			int radius = SkillConfigManager.getUseSetting(hero, skill, "radius", Integer.valueOf(30), false);
			
			if(!player.getWorld().equals(cwolf.getWolf().getWorld()) || cwolf.getWolf().getLocation().distance(player.getLocation())>radius)
			{
				Messaging.send(player, "Your companion is too far away from you!");
				return SkillResult.FAIL;
			}
			else
			{
				final int standarddamage = cwolf.getDamage();
				
				double damageperc = SkillConfigManager.getUseSetting(hero, skill, "damage-percent", Double.valueOf(1.25), false);
				damageperc += SkillConfigManager.getUseSetting(hero, skill, "damage-percent-increase", Double.valueOf(0.01), false) * hero.getSkillLevel(skill);
				
				int duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(10000), false);
				duration += SkillConfigManager.getUseSetting(hero, skill, "duration-increase", Integer.valueOf(10), false) * hero.getSkillLevel(skill);
				
				double damage = cwolf.getDamage() * damageperc;
				
				long dur = duration/1000;
				dur = dur*20;
				
				cwolf.setDamage((int) damage);
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
						if(cwolf!=null)
						{
							cwolf.setDamage(standarddamage);
							if(player!=null)
							{
								Messaging.send(player, "Your companion no longer has boosted attackpower!");
							}
						}
					}
				}, dur);
				
				Messaging.send(player, "Your companion has boosted attackpower!");
				
				ParticleEffects pe = ParticleEffects.LAVA;
				
				try {
					pe.sendToLocation(cwolf.getWolf().getEyeLocation(), 0, 0, 0, 1, 10);
				} catch (Exception e) {
				}
				
				cwolf.getWolf().getWorld().playSound(cwolf.getLocation(), Sound.WOLF_HOWL, 10F, 1);
				return SkillResult.NORMAL;
			}
		}
		else
		{
			Messaging.send(player, "You don't have any companion to boost!", new Object());
			return SkillResult.FAIL;
		}
	}
}
