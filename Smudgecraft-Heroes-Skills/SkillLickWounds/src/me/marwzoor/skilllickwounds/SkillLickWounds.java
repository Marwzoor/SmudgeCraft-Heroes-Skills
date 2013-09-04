package me.marwzoor.skilllickwounds;

import net.smudgecraft.heroeslib.companions.Companion;
import net.smudgecraft.heroeslib.companions.Companions;

import org.bukkit.EntityEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;


public class SkillLickWounds extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillLickWounds skill;
	
	public SkillLickWounds(Heroes instance)
	{
		super(instance, "LickWounds");
		plugin=instance;
		skill=this;
		setDescription("Your companion licks its wounds, healing it for %1 health.");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill lickwounds" });
		setTypes(new SkillType[] { SkillType.BUFF });
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		double heal = SkillConfigManager.getUseSetting(hero, skill, "heal", Double.valueOf(50), false);
		heal += SkillConfigManager.getUseSetting(hero, skill, "heal-increase", Double.valueOf(1), false) * hero.getSkillLevel(skill);
		return desc.replace("%1", heal + "");
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("heal", Double.valueOf(50));
		node.set("heal-increase", Double.valueOf(1));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		Player player = hero.getPlayer();
		
		if(Companions.getPlayerManager().getCompanionPlayer(player).hasCompanionOfType(EntityType.WOLF))
		{
			Companion cwolf = Companions.getPlayerManager().getCompanionPlayer(player).getFirstCompanionOfType(EntityType.WOLF);
			
			if(cwolf==null)
				return SkillResult.FAIL;
			
			if(cwolf.getLivingEntity().getLocation().distance(player.getLocation())<25)
			{
				double heal = SkillConfigManager.getUseSetting(hero, skill, "heal", Double.valueOf(50), false);
				heal += SkillConfigManager.getUseSetting(hero, skill, "heal-increase", Double.valueOf(1), false) * hero.getSkillLevel(skill);
				
				cwolf.heal(heal);
				cwolf.getLivingEntity().playEffect(EntityEffect.WOLF_SHAKE);
				
				Messaging.send(player, "Your companion licks its wounds!");
				
				return SkillResult.NORMAL;
			}
			else
			{
				Messaging.send(player, "You are too far away from your companion for it to lick its wounds!");
				return SkillResult.FAIL;
			}
		}
		return SkillResult.FAIL;
	}
}
