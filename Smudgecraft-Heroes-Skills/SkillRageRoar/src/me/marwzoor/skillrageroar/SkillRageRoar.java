package me.marwzoor.skillrageroar;

import me.marwzoor.skillfury.SkillFury;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.metadata.FixedMetadataValue;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillRageRoar extends ActiveSkill
{
	public SkillRageRoar(Heroes instance)
	{
		super(instance, "RageRoar");
		setDescription("You roar at your enemies, increasing your fury by %1 and increasing your critical hit chance by %2. CD: %3 D: %4 C: %2");
		setUsage("/skill rageroar");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill rageroar" });
		setTypes(new SkillType[] { SkillType.BUFF });
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int fury = (int) (SkillConfigManager.getUseSetting(hero, this, "fury", 45, false) +
					(SkillConfigManager.getUseSetting(hero, this, "fury-increase", 0.25, false) * hero.getSkillLevel(this)));
			double critical = (SkillConfigManager.getUseSetting(hero, this, "critical", 0.1, false) +
					(SkillConfigManager.getUseSetting(hero, this, "critical-increase", 0.005, false) * hero.getSkillLevel(this)));
			int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000;
			
			return super.getDescription().replace("%1", fury + "%").replace("%2", critical + "").replace("%3", cooldown + "s").replace("%4", duration + "s");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "X%").replace("%3", "Xs").replace("%4", "Xs");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("fury", 45);
		node.set("fury-increase", 0.25);
		node.set("critical", 0.1);
		node.set("critical-increase", 0.005);
		node.set(SkillSetting.DURATION.node(), 10000);
		return node;
	}
	
	public SkillResult use(final Hero hero, String[] args)
	{
		if(!hero.isInCombat())
		{
			Messaging.send(hero.getPlayer(), "You have to be in combat to use this skill!");
			return SkillResult.FAIL;
		}
		
		int fury = (int) (SkillConfigManager.getUseSetting(hero, this, "fury", 45, false) +
				(SkillConfigManager.getUseSetting(hero, this, "fury-increase", 0.25, false) * hero.getSkillLevel(this)));
		double critical = (SkillConfigManager.getUseSetting(hero, this, "critical", 0.1, false) +
				(SkillConfigManager.getUseSetting(hero, this, "critical-increase", 0.005, false) * hero.getSkillLevel(this)));
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
		
		duration = ((duration/1000) * 20);
		
		hero.getPlayer().setMetadata("CriticalChance", new FixedMetadataValue(plugin, critical));
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				if(hero!=null && hero.getPlayer()!=null)
				{
					hero.getPlayer().removeMetadata("CriticalChance", plugin);
					Messaging.send(hero.getPlayer(), "You are no longer under the effects of " + ChatColor.WHITE + "RageRoar" + ChatColor.GRAY + "!");
				}
			}
		}, (long) duration);
		
		if(hero.getMana()+fury>=hero.getMaxMana())
		{
			hero.setMana(hero.getMaxMana());
			SkillFury.fullFury(hero);
		}
		else
		{
			hero.setMana(hero.getMana()+fury);
		}
		
		Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + "FURY " + createFuryBar(hero.getMana(), hero.getMaxMana()));
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "RageRoar" + ChatColor.GRAY + "! You have a greater chance of making " + ChatColor.WHITE + "Critical" + ChatColor.GRAY + "hits!");

		return SkillResult.NORMAL;
	}
	
	public static String createFuryBar(double fury, double maxFury) 
	{
	    StringBuilder furyBar = new StringBuilder(new StringBuilder().append(ChatColor.RED).append("[").append(ChatColor.DARK_RED).toString());
	    int percent = (int)(fury / maxFury * 100.0D);
	    int progress = percent / 2;
	    for (int i = 0; i < progress; i++) {
	      furyBar.append('|');
	    }
	    furyBar.append(ChatColor.DARK_GRAY);
	    for (int i = 0; i < 50 - progress; i++) {
	      furyBar.append('|');
	    }
	    furyBar.append(ChatColor.RED).append(']');
	    return new StringBuilder().append(furyBar).append(" - ").append(ChatColor.DARK_RED).append(percent).append("%").toString();
	}
}
