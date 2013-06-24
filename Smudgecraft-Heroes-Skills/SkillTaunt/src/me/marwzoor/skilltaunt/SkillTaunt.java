package me.marwzoor.skilltaunt;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillTaunt extends ActiveSkill {
	
	public SkillTaunt(Heroes instance) {
		super(instance, "Taunt");
		setDescription("You force all nearby enemies to target you.");
		setIdentifiers(new String[] {
			"skill taunt"
		});
		setArgumentRange(0, 0);
		setTypes(new SkillType[] {
			SkillType.FORCE
		});
	}
		
	public String getDescription(Hero hero) {
		String desc = super.getDescription();
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(10), false);
		desc.replace("%r", radius + "");
		return desc;
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.RADIUS.node(), Integer.valueOf(10));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args) {
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(10), false);
		
		List<Entity> entities = hero.getPlayer().getNearbyEntities(radius, radius, radius);
		for(Entity entity : entities) {
			if ((entity instanceof Monster)) {
				((Monster) entity).setTarget(hero.getPlayer());
			}
		}
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "Taunt" + ChatColor.GRAY + "! Nearby enemies are focusing on you!");
		
		return SkillResult.NORMAL;
	}

}
