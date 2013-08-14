package me.marwzoor.skillflamearrow;

import net.smudgecraft.heroeslib.events.ImbueArrowHitEvent;
import net.smudgecraft.heroeslib.events.ImbueArrowLaunchEvent;
import net.smudgecraft.heroeslib.commoneffects.ImbueEffect;

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
	
	public SkillFlameArrow(Heroes instance)
	{
		super(instance, "FlameArrow");
		plugin=instance;
		setDescription("The next arrow you fire sets your opponent on fire for %1 seconds. You have %2 seconds to fire your arrow. M:%3 CD:%4");
		setUsage("/skill flamearrow");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill flamearrow" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT});
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if (hero.hasAccessToSkill(this)) {
			String desc = super.getDescription();
			double duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(30000), false);
			double fireduration = SkillConfigManager.getUseSetting(hero, this, "fire-duration", Integer.valueOf(6000), false);
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, Integer.valueOf(0), false);
			int cd = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, Integer.valueOf(0), false);
			desc.replace("%1", fireduration + "").replace("%2", duration + "").replace("%3", mana + "").replace("%4", cd + "");
			return desc;
		} else {
			return super.getDescription().replace("%1", "X").replace("%2", "X").replace("%3", "X").replace("%4", "X");
		}
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
		int duration = SkillConfigManager.getUseSetting(hero, this, "duration", Integer.valueOf(30000), false);
		int flameduration = SkillConfigManager.getUseSetting(hero, this, "fire-duration", Integer.valueOf(6000), false);
		flameduration += SkillConfigManager.getUseSetting(hero, this, "fire-duration-increase", Integer.valueOf(10), false) * hero.getSkillLevel(this);
		
		int firedamage = SkillConfigManager.getUseSetting(hero, this, "fire-damage", Integer.valueOf(20), false);
		firedamage += SkillConfigManager.getUseSetting(hero, this, "fire-damage-increase", Integer.valueOf(1), false) * hero.getSkillLevel(this);
		
		if(hero.hasEffect("FlameArrow"))
		{
			hero.removeEffect(hero.getEffect("FlameArrow"));
		}
		
		FlameArrowEffect fEffect = new FlameArrowEffect(this, duration, hero.getPlayer(), flameduration, firedamage);
		
		hero.addEffect(fEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "FlameArrow" + ChatColor.GRAY + "! Your next arrow will set fire to your opponent!");
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		private SkillFlameArrow skill;
		
		public SkillHeroListener(SkillFlameArrow skill) {
			this.skill = skill;
		}
		
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
		public FireDamageTickEffect(Skill skill, int period, int duration, double tickDamage, Player applier) 
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
