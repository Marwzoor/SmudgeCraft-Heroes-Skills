package me.marwzoor.skillshield;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillShield extends PassiveSkill
{
	protected HashMap<String,Integer> bePlayers;
	
	public SkillShield(Heroes plugin)
	{
		super(plugin,"Shield");
		setDescription("A shield can be used to block incoming melee and ranged damage by X%.");
		setArgumentRange(0,0);
		setTypes(new SkillType[]{SkillType.BUFF});
		setIdentifiers(new String[]{"skill shield"});
		bePlayers = new HashMap<String,Integer>();
		
		Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
	}
	
	@Override
	public String getDescription(Hero hero)
	{
		String description = getDescription();
		double percent = SkillConfigManager.getUseSetting(hero, this, "percent", 0.5D, false);
		percent = percent*100;
		description = description.replace("X%", percent + "%");
		return description;
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("percent", Double.valueOf(0.5D));
		node.set("percent-increase", Double.valueOf(0.05D));
		node.set("shield-item", Integer.valueOf(36));
        return node;

	}
}

