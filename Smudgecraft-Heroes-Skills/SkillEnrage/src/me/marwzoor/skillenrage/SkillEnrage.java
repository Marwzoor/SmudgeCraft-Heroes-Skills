package me.marwzoor.skillenrage;

import net.smudgecraft.companions.ComWolf;
import net.smudgecraft.companions.Companions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillEnrage extends ActiveSkill
{
	public static Heroes plugin;
	
	public SkillEnrage(Heroes instance)
	{
		super(instance, "Enrage");
		plugin=instance;
		setDescription("Your companion becomes enraged, doubling its movement speed. D:%1s");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill enrage" });
		setTypes(new SkillType[] { SkillType.BUFF });
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		if (hero.hasAccessToSkill(this)) {
			int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(15000), false) + SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(10), false) * hero.getSkillLevel(this)) / 1000;
			desc.replace("%1", duration + "");
			return desc;
		} else {
			return desc.replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(15000));
		node.set(SkillSetting.DURATION_INCREASE.node(), Integer.valueOf(10));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		final Player player = hero.getPlayer();
		
		if(Companions.cwolves.hasWolf(player))
		{
			final ComWolf cwolf = Companions.cwolves.getComWolf(player);
			
			int radius = SkillConfigManager.getUseSetting(hero, this, "radius", 30, false);
			
			if(!player.getWorld().equals(cwolf.getWolf().getWorld()) || cwolf.getWolf().getLocation().distance(player.getLocation())>radius)
			{
				Messaging.send(player, "Your companion is too far away from you!");
				return SkillResult.FAIL;
			}
			else
			{
				int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(15000), false);
				duration += SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(10), false) * hero.getSkillLevel(this);
				
				int dur = duration/1000;
				dur = dur*20;
				
				cwolf.addPotionEffect(PotionEffectType.SPEED, dur, 1);
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
						if(cwolf!=null)
						{
							if(player!=null)
							{
								Messaging.send(player, "Your companion no longer has a burst of speed!");
							}
						}
					}
				}, dur);
				
				Messaging.send(player, "Your companion gained a burst of speed!");
				
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
