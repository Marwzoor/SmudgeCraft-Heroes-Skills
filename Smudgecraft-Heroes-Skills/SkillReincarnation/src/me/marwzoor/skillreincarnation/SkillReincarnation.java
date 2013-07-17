package me.marwzoor.skillreincarnation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.CharacterDamageEvent;
import com.herocraftonline.heroes.api.events.SkillDamageEvent;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillReincarnation extends PassiveSkill
{
	public static SkillReincarnation skill;
	public static Heroes plugin;
	
	public SkillReincarnation(Heroes instance)
	{
		super(instance, "Reincarnation");
		plugin=instance;
		skill=this;
		setDescription("When you die, you have a $1% to be revived with full health. (Passive)");
		setTypes(new SkillType[] { SkillType.HEAL });

		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		if(hero.hasAccessToSkill(skill))
		{
		int chance = (int) ((SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.2D, false) + (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL, 0.005D, false) * hero.getSkillLevel(skill))) * 100);
		return desc.replace("$1", chance + "");
		}
		else
		{
			return desc.replace("$1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.CHANCE.node(), Double.valueOf(0.2));
		node.set(SkillSetting.CHANCE_LEVEL.node(), Double.valueOf(0.005));
		return node;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onCharacterDamageEvent(CharacterDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(event.getEntity() instanceof Player)
			{
				Hero hero = plugin.getCharacterManager().getHero((Player) event.getEntity());
				
				if(hero.hasAccessToSkill(skill))
				{
					if(event.getDamage()>=hero.getPlayer().getHealth())
					{
						double chance = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.2D, false) + (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL, 0.005D, false) * hero.getSkillLevel(skill));
						
						if(Math.random()<=chance)
						{
							hero.getPlayer().setHealth(hero.getPlayer().getMaxHealth());
						
							broadcast(hero.getPlayer().getLocation(), hero.getPlayer().getDisplayName() + ChatColor.GRAY + " cheated death and " + ChatColor.WHITE + "Reincarnated " + ChatColor.GRAY + "with full health!");
						
							event.setCancelled(true);
						}
					}
				}
			}
		}
		
		@EventHandler
		public void onSkillDamageEvent(SkillDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(event.getEntity() instanceof Player)
			{
				Hero hero = plugin.getCharacterManager().getHero((Player) event.getEntity());
				
				if(hero.hasAccessToSkill(skill))
				{
					if(event.getDamage()>=hero.getPlayer().getHealth())
					{
						double chance = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.2D, false) + (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL, 0.005D, false) * hero.getSkillLevel(skill));
						
						if(Math.random()<=chance)
						{
							hero.getPlayer().setHealth(hero.getPlayer().getMaxHealth());
						
							broadcast(hero.getPlayer().getLocation(), hero.getPlayer().getDisplayName() + ChatColor.GRAY + " cheated death and " + ChatColor.WHITE + "Reincarnated " + ChatColor.GRAY + "with full health!");
						
							event.setCancelled(true);
						}
					}
				}
			}
		}
		
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(event.getEntity() instanceof Player)
			{
				Hero hero = plugin.getCharacterManager().getHero((Player) event.getEntity());
				
				if(hero.hasAccessToSkill(skill))
				{
					if(event.getDamage()>=hero.getPlayer().getHealth())
					{
						double chance = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.2D, false) + (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL, 0.005D, false) * hero.getSkillLevel(skill));
						
						if(Math.random()<=chance)
						{
							hero.getPlayer().setHealth(hero.getPlayer().getMaxHealth());
						
							broadcast(hero.getPlayer().getLocation(), hero.getPlayer().getDisplayName() + ChatColor.GRAY + " cheated death and " + ChatColor.WHITE + "Reincarnated " + ChatColor.GRAY + "with full health!");
						
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}
}
