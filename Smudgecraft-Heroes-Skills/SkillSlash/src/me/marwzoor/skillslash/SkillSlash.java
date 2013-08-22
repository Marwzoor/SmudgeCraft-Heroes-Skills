package me.marwzoor.skillslash;

import net.smudgecraft.heroeslib.commoneffects.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillSlash extends ActiveSkill
{
	public SkillSlash(Heroes instance)
	{
		super(instance, "Slash");
		setDescription("You slash your opponent, knocking them back and dealing $1% more damage.");
		setIdentifiers(new String[] { "skill slash" });
		setArgumentRange(0, 0);
		setTypes(new SkillType[] { SkillType.BUFF });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
		String desc = super.getDescription();
		double percentage = SkillConfigManager.getUseSetting(hero, this, "percentage", Double.valueOf(1.25), false);
		percentage = percentage-1;
		percentage = percentage*100;
		desc = desc.replace("$1", percentage + "");
		return desc;
		}
		else
		{
			return getDescription().replace("$1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(15000));
		node.set("percentage", Double.valueOf(1.25));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(15000), false);
		double percent = SkillConfigManager.getUseSetting(hero, this, "percentage", Double.valueOf(1.25), false);

		if(hero.hasEffect("Slash"))
		{
			hero.removeEffect(hero.getEffect("Slash"));
		}
		
		SlashEffect sEffect = new SlashEffect(this, duration, percent);
		
		hero.addEffect(sEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "Slash" + ChatColor.GRAY + "! The next opponent you hit will be knocked back!");
		
		return SkillResult.NORMAL;
	}
	
	public class SlashEffect extends ImbueEffect
	{
		private double percent;
		
		public SlashEffect(Skill skill, int duration, double percent)
		{
			super(skill, "Slash", duration);
			this.percent=percent;
		}
		
		public double getPercent()
		{
			return percent;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "Slash" + ChatColor.GRAY + "!", new Object());
		}
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(!event.getCause().equals(DamageCause.ENTITY_ATTACK))
				return;
			
			if(event.getDamager() instanceof Hero)
			{
				Hero hero = (Hero) event.getDamager();
				
				if(hero.hasEffect("Slash"))
				{
					if(event.getEntity() instanceof LivingEntity)
					{
						SlashEffect sEffect = (SlashEffect) hero.getEffect("Slash");
												
						double damage = event.getDamage()*sEffect.getPercent();
						
						event.setDamage(damage);
						
						LivingEntity le = (LivingEntity) event.getEntity();
						
						Vector vec = hero.getPlayer().getLocation().getDirection();
						
						le.setVelocity(new Vector(vec.getX()*3, 1, vec.getZ()*3));
						
						hero.removeEffect(sEffect);
					}
				}
			}
		}
	}
}
