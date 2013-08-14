package me.marwzoor.skillmoribund;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillMoribund extends PassiveSkill
{
	public SkillMoribund(Heroes instance)
	{
		super(instance, "Moribund");
		setDescription("Your depleted health percentage boosts your damage with %1 of that amount. (Passive)");
		setIdentifiers(new String[] { "skill ravingslice" });
		setEffectTypes(new EffectType[] { EffectType.BENEFICIAL } );
		setTypes(new SkillType[] { SkillType.BUFF });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this, plugin), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int percentage = SkillConfigManager.getUseSetting(hero, this, "percentage", 100, false);
			return super.getDescription().replace("%1", percentage + "%");
		}
		else
		{
			return super.getDescription().replace("%1", "X%");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("percentage", 100);
		return node;
	}
	
	public class SkillHeroListener implements Listener
	{
		public SkillMoribund skill;
		public Heroes plugin;
		
		public SkillHeroListener(SkillMoribund skill, Heroes plugin)
		{
			this.skill=skill;
			this.plugin=plugin;
		}
		
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(!(event.getDamager() instanceof Hero))
				return;
			
			Hero hero = (Hero) event.getDamager();
			
			if(!hero.hasAccessToSkill(skill))
				return;
			
			double percentage = (2-((hero.getPlayer().getHealth()/hero.getPlayer().getMaxHealth()) * 
					(((double)SkillConfigManager.getUseSetting(hero, skill, "percentage", 100, false))/100)));
			
			event.setDamage(event.getDamage()*percentage);
		}
	}
}
