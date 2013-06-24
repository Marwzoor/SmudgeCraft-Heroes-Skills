package me.marwzoor.skillbowsense;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.HeroRegainManaEvent;
import com.herocraftonline.heroes.api.events.SkillUseEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillBowSense extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillBowSense skill;
	
	public SkillBowSense(Heroes instance)
	{
		super(instance, "BowSense");
		plugin=instance;
		skill=this;
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill bowsense" });
		setDescription("You focus your senses for %1 seconds, changing your mana regen to %2 but silencing you.");
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(20000));
		node.set("mana-regen", Double.valueOf(4));
		node.set("mana-regen-increase", Double.valueOf(0.1));
		return node;
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		double manareg = SkillConfigManager.getUseSetting(hero, skill, "mana-regen", Double.valueOf(4), false);
		manareg += SkillConfigManager.getUseSetting(hero, skill, "mana-regen-increase", Double.valueOf(0.1), false) * hero.getSkillLevel(skill);
		
		int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 20000, false);
		duration = duration/1000;
		
		return desc.replace("%1", duration + "").replace("%2", (int) manareg + "");
	}
	
	public class BowSenseEffect extends ExpirableEffect
	{
		private double manareg;
		
		public BowSenseEffect(Skill skill, int duration, double manareg)
		{
			super(skill, "BowSense", duration);
			this.manareg=manareg;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
			Messaging.send(hero.getPlayer(), "You are focusing your senses, regaining mana rapidly but you can't use skills!");
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You are no longer focusing your senses!");
		}
		
		public double getManareg()
		{
			return this.manareg;
		}
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 20000, false);
		
		Double manareg = SkillConfigManager.getUseSetting(hero, skill, "mana-regen", Double.valueOf(4), false);
		manareg += SkillConfigManager.getUseSetting(hero, skill, "mana-regen-increase", Double.valueOf(0.1), false) * hero.getSkillLevel(skill);
		
		BowSenseEffect BSEffect = new BowSenseEffect(skill, duration, manareg);
		
		hero.addEffect(BSEffect);
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onSkillUseEvent(SkillUseEvent event)
		{
			Hero hero = event.getHero();
			
			if(hero.hasEffect("BowSense"))
			{
				if(event.getSkill().equals(skill))
				{
					BowSenseEffect BSEffect = (BowSenseEffect) hero.getEffect("BowSense");
					
					hero.removeEffect(BSEffect);
				}
				else
				{
					Messaging.send(hero.getPlayer(), "You are focusing your senses and can't use skills!");
				}
				event.setCancelled(true);
			}
		}
		
		@EventHandler
		public void onHeroRegainManaEvent(HeroRegainManaEvent event)
		{
			if(event.getHero().hasEffect("BowSense"))
			{
				BowSenseEffect BSEffect = (BowSenseEffect) event.getHero().getEffect("BowSense");
				
				event.setAmount((int) BSEffect.getManareg());
			}
		}
	}
}
