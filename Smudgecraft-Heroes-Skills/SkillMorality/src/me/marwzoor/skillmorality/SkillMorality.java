package me.marwzoor.skillmorality;

import net.smudgecraft.companions.util.ParticleEffects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.party.HeroParty;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillMorality extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillMorality skill;
	
	public SkillMorality(Heroes instance)
	{
		super(instance, "Morality");
		plugin=instance;
		skill=this;
		setDescription("You stab your opponent in their gut, stunning them for %s seconds, dealing %p% more damage.");
		setIdentifiers(new String[] { "skill morality", "skill morale" });
		setArgumentRange(0, 0);
		setTypes(new SkillType[] { SkillType.BUFF });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		double percentage = SkillConfigManager.getUseSetting(hero, skill, "percentage", Double.valueOf(1.25), false);
		percentage = percentage-1;
		percentage = percentage*100;
		desc = desc.replace("%p", percentage + "");
		return desc;
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("duration", Integer.valueOf(15000));
		node.set("percentage", Double.valueOf(1.25));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(15000), false);
		double percentage = SkillConfigManager.getUseSetting(hero, skill, "percentage", Double.valueOf(1.25), false);
		
		MoralityEffect mEffect = new MoralityEffect(skill, duration, percentage);
		
		if(hero.hasParty())
		{
			HeroParty party = hero.getParty();
			for(Hero m : party.getMembers())
			{
				if(m.hasEffect("Morality"))
				{
					MoralityEffect me = (MoralityEffect) m.getEffect("Morality");
					m.removeEffect(me);
					m.addEffect(mEffect);
					if(hero!=m)
					{
					Messaging.send(m.getPlayer(), hero.getPlayer().getDisplayName() + ChatColor.GRAY + " encouraged you to fight! You now deal more damage!");
					}
					else
					{
					Messaging.send(m.getPlayer(), "You encouraged your party members to fight! You now deal more damage!");
					}
				}
				else
				{
					m.addEffect(mEffect);
					if(hero!=m)
					{
					Messaging.send(m.getPlayer(), hero.getPlayer().getDisplayName() + ChatColor.GRAY + " encouraged you to fight! You now deal more damage!");
					}
					else
					{
					Messaging.send(m.getPlayer(), "You encouraged your party members to fight! You now deal more damage!");
					}
				}
				
				ParticleEffects pe = ParticleEffects.WITCH_MAGIC;
				try
				{
				Location loc = m.getPlayer().getEyeLocation();
				loc.setY(loc.getY()+0.7);
				pe.sendToLocation(loc, 0, 0, 0, 1, 50);
				}
				catch(Exception e)
				{
					
				}
			}
		}
		else
		{
			if(hero.hasEffect("Morality"))
			{
				MoralityEffect me = (MoralityEffect) hero.getEffect("Morality");
				hero.removeEffect(me);
				hero.addEffect(mEffect);
				Messaging.send(hero.getPlayer(), "You encouraged your party members to fight! You now deal more damage!");
			}
			else
			{
				hero.addEffect(mEffect);
				Messaging.send(hero.getPlayer(), "You encouraged your party members to fight! You now deal more damage!");
			}
			
			ParticleEffects pe = ParticleEffects.WITCH_MAGIC;
			try
			{
			Location loc = hero.getPlayer().getEyeLocation();
			loc.setY(loc.getY()+0.7);
			pe.sendToLocation(loc, 0, 0, 0, 1, 50);
			}
			catch(Exception e)
			{
				
			}
		}
		
		return SkillResult.NORMAL;
	}
	
	public class MoralityEffect extends ExpirableEffect
	{
		private double percentage;
		
		public MoralityEffect(Skill skill, int duration, double percentage)
		{
			super(skill, "Morality", duration);
			this.percentage=percentage;
		}
		
		public double getPercentage()
		{
			return this.percentage;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "Your morality is no longer increased.");
		}
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(event.getDamager() instanceof Hero)
			{
				Hero hero = (Hero) event.getDamager();
				if(hero.hasEffect("Morality"))
				{
					MoralityEffect me = (MoralityEffect) hero.getEffect("Morality");
					
					double damage = event.getDamage()*me.getPercentage();
					
					event.setDamage((int) damage);
				}
			}
		}
	}
}
