package me.marwzoor.skillfocus;

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

public class SkillFocus extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillFocus skill;
	
	public SkillFocus(Heroes instance)
	{
		super(instance, "Focus");
		plugin=instance;
		skill=this;
		setDescription("You focus your senses for %1 seconds, changing your mana regen to %2 but silencing you.");
		setUsage("/skill focus");
		setIdentifiers(new String[] { "skill focus" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.SILENCABLE, SkillType.MANA });
		setArgumentRange(0,0);
		
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
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, Integer.valueOf(20000), false);
		double mreg = SkillConfigManager.getUseSetting(hero, skill, "mana-reg", Double.valueOf(4), false);
		mreg += SkillConfigManager.getUseSetting(hero, skill, "mana-regen-increase", Double.valueOf(0.1), false) * hero.getSkillLevel(skill);
		
		if(hero.hasEffect("Focus"))
		{
			hero.removeEffect(hero.getEffect("Focus"));
		}
		
		FocusEffect fEffect = new FocusEffect(skill, duration, mreg);
		
		SkillSilenceEffect sEffect = new SkillSilenceEffect(skill, duration);
		
		hero.addEffect(sEffect);
		
		hero.addEffect(fEffect);
				
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onHeroRegainManaEvent(HeroRegainManaEvent event)
		{
			Hero hero = event.getHero();
			
			if(hero.hasEffect("Focus"))
			{
				FocusEffect fEffect = (FocusEffect) hero.getEffect("Focus");
				
				event.setAmount((int) fEffect.getManareg());
			}
		}
		
		@EventHandler
		public void onHeroSkillUseEvent(SkillUseEvent event)
		{
			Hero hero = event.getHero();
			
			if(event.getHero().hasEffect("SkillSilence"))
			{
				if(event.getSkill().equals(skill))
				{
					FocusEffect fEffect = (FocusEffect) hero.getEffect("Focus");
					
					SkillSilenceEffect sEffect = (SkillSilenceEffect) hero.getEffect("SkillSilence");
					
					hero.removeEffect(fEffect);
					
					hero.removeEffect(sEffect);
				}
				else
				{
					Messaging.send(hero.getPlayer(), "You are focusing your senses and can't use skills!");
				}
				event.setCancelled(true);
			}
		}
	}
	
	public class FocusEffect extends ExpirableEffect
	{
		private double mreg;
		public FocusEffect(Skill skill, int duration, double mreg)
		{
			super(skill, "Focus", duration);
			this.mreg=mreg;
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
			return this.mreg;
		}
	}
	
	public class SkillSilenceEffect extends ExpirableEffect
	{
		public SkillSilenceEffect(Skill skill, int duration)
		{
			super(skill, "SkillSilence", duration);
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
		}
	}
}
