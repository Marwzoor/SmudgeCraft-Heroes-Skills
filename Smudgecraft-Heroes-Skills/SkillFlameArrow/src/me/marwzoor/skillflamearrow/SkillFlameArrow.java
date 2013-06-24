package me.marwzoor.skillflamearrow;

import net.smudgecraft.companions.events.ImbueArrowHitEvent;
import net.smudgecraft.companions.events.ImbueArrowLaunchEvent;
import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillFlameArrow extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillFlameArrow skill;
	
	public SkillFlameArrow(Heroes instance)
	{
		super(instance, "FlameArrow");
		plugin=instance;
		skill=this;
		setDescription("The next arrow you fire sets your opponent on fire for $1 seconds. You have $2 seconds to fire your arrow.");
		setUsage("/skill flamearrow");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill flamearrow" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT});
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		double duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		double fireduration = SkillConfigManager.getUseSetting(hero, skill, "fire-duration", Integer.valueOf(6000), false);
		fireduration += SkillConfigManager.getUseSetting(hero, skill, "fire-duration-increase", Integer.valueOf(10), false) * hero.getSkillLevel(skill);
		fireduration = fireduration/1000;
		duration = duration/1000;
		desc = desc.replace("$1", fireduration + "");
		desc = desc.replace("$2", duration + "");
		return desc;
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(30000));
		node.set("fire-duration", Integer.valueOf(2000));
		node.set("fire-duration-increase", Integer.valueOf(10));
		node.set("fire-damage", Integer.valueOf(20));
		node.set("fire-damage-increase", Integer.valueOf(1));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		int flameduration = SkillConfigManager.getUseSetting(hero, skill, "fire-duration", Integer.valueOf(6000), false);
		flameduration += SkillConfigManager.getUseSetting(hero, skill, "fire-duration-increase", Integer.valueOf(10), false) * hero.getSkillLevel(skill);
		
		int firedamage = SkillConfigManager.getUseSetting(hero, skill, "fire-damage", Integer.valueOf(20), false);
		firedamage += SkillConfigManager.getUseSetting(hero, skill, "fire-damage-increase", Integer.valueOf(1), false) * hero.getSkillLevel(skill);
		
		if(hero.hasEffect("FlameArrow"))
		{
			hero.removeEffect(hero.getEffect("FlameArrow"));
		}
		
		FlameArrowEffect fEffect = new FlameArrowEffect(skill, duration, hero.getPlayer(), flameduration, firedamage);
		
		hero.addEffect(fEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "FlameArrow" + ChatColor.GRAY + "! Your next arrow will set fire to your opponent!", new Object());
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onEntityShot(ImbueArrowHitEvent event)
		{
			if(event.getImbueEffect() instanceof FlameArrowEffect)
			{
				FlameArrowEffect fEffect = (FlameArrowEffect) event.getImbueEffect();
				
				FireDamageTickEffect fde = new FireDamageTickEffect(skill, 2000, fEffect.getFlameDuration(), fEffect.getFlameDamage(), fEffect.getPlayer());
				
				CharacterTemplate ct = plugin.getCharacterManager().getCharacter(event.getShotEntity());
				
				ct.addEffect(fde);
				
				double ticks = fEffect.getFlameDuration();
				ticks = ticks/1000;
				ticks = ticks*20;
				event.getShotEntity().setFireTicks((int) ticks);
				
				if(fEffect.getPlayer()!=null)
				{
					Hero hero = plugin.getCharacterManager().getHero(fEffect.getPlayer());
					
					hero.removeEffect(fEffect);
				}
			}
		}
		
		@EventHandler
		public void onImbueArrowShotEvent(ImbueArrowLaunchEvent event)
		{
			if(event.getImbueEffect() instanceof FlameArrowEffect)
			{
				event.getArrow().setFireTicks(20*20);
			}
		}
		
		@EventHandler
		public void onEntityDamageEvent(EntityDamageEvent event)
		{
			if(event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK))
			{
				if(event.getEntity() instanceof LivingEntity)
				{
					LivingEntity le = (LivingEntity) event.getEntity();
					CharacterTemplate ct = plugin.getCharacterManager().getCharacter(le);
					
					if(ct.hasEffect("FireDamageTick"))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	public class FireDamageTickEffect extends PeriodicDamageEffect
	{
		public FireDamageTickEffect(Skill skill, int period, int duration, int tickDamage, Player applier) 
		{
			super(skill, "FireDamageTick", period, duration, tickDamage, applier);
		}
		
		public void applyToHero(Hero hero)
		{
			skill.broadcast(hero.getPlayer().getLocation(), hero.getPlayer().getDisplayName() + ChatColor.GRAY + " is on fire!");
			super.applyToHero(hero);
		}
		
		public void removeFromHero(Hero hero)
		{
			skill.broadcast(hero.getPlayer().getLocation(), hero.getPlayer().getDisplayName() + ChatColor.GRAY  + " is no longer on fire!");
			super.removeFromHero(hero);
		}
		
		public void applyToMonster(Monster monster)
		{
			skill.broadcast(monster.getEntity().getLocation(), ChatColor.WHITE + monster.getEntity().getType().getName() + ChatColor.GRAY + " is on fire!");
			super.applyToMonster(monster);
		}
		
		public void removeFromMonster(Monster monster)
		{
			skill.broadcast(monster.getEntity().getLocation(), ChatColor.WHITE + monster.getEntity().getType().getName() + ChatColor.GRAY + " is no longer on fire!");
			super.removeFromMonster(monster);
		}
		
	}
	
	public class FlameArrowEffect extends ImbueEffect
	{
		private int damage;
		private int flameduration;
		
		public FlameArrowEffect(Skill skill, int duration, Player player, int flameduration, int damage)
		{
			super(skill, "FlameArrow", duration, player);
			this.damage=damage;
			this.flameduration=flameduration;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "FlameArrow" + ChatColor.GRAY + "!", new Object());
		}
		
		public int getFlameDamage()
		{
			return this.damage;
		}
		
		public int getFlameDuration()
		{
			return this.flameduration;
		}
	}
}
