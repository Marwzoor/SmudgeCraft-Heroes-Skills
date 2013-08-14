package me.marwzoor.skillagility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillAgility extends ActiveSkill
{
	public SkillAgility(Heroes instance)
	{
		super(instance, "Agility");
		setDescription("Allows you to climb walls and jump higher for %1 seconds. D: %2 M: %3 CD: %4");
		setUsage("/skill agility");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill agility" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT });
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int duration = ((SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 30000, false) +
					(SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(this)))/1000);
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000);
			
			return super.getDescription().replace("%1", duration + "").replace("%2", duration + "s").replace("%3", mana + "").replace("%4", cooldown + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "Xs").replace("%3", "X").replace("%4", "Xs");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 30000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 100);
		node.set(SkillSetting.MANA.node(), 0);
		node.set(SkillSetting.COOLDOWN.node(), 0);
		node.set("jump-amplifier", 5);
		return node;
	}
	
	public SkillResult use(final Hero hero, String[] args)
	{
		int jumpamp = SkillConfigManager.getUseSetting(hero, this, "jump-amplifier", 5, false);
		if(jumpamp>10)
			jumpamp=10;
		int duration = ((SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 30000, false) +
				(SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(this)))/1000)*20;
		PotionEffect pe = PotionEffectType.JUMP.createEffect(duration, jumpamp);
		
		hero.getPlayer().addPotionEffect(pe);
		this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "Agility!");
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				if(hero!=null && hero.getPlayer()!=null && hero.getPlayer().isOnline())
				{
					Messaging.send(hero.getPlayer(), "You are no longer under the effect of " + ChatColor.WHITE + "Agility" + ChatColor.GRAY + "!");
				}
			}
		}, duration);
		
		return SkillResult.NORMAL;
	}
}
