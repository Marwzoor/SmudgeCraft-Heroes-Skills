package me.marwzoor.skillstab;

import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillStab extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillStab skill;
	
	public SkillStab(Heroes instance)
	{
		super(instance, "Stab");
		plugin=instance;
		skill=this;
		setDescription("You stab your opponent in their gut, stunning them for $1 seconds, dealing $2% more damage.");
		setIdentifiers(new String[] { "skill stab" });
		setArgumentRange(0, 0);
		setTypes(new SkillType[] { SkillType.BUFF });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
		String desc = super.getDescription();
		double stunduration = SkillConfigManager.getUseSetting(hero, skill, "stun-duration", Integer.valueOf(500), false);
		double percentage = SkillConfigManager.getUseSetting(hero, skill, "percentage", Double.valueOf(1.25), false);
		stunduration = stunduration/1000;
		percentage = percentage-1;
		percentage = percentage*100;
		desc = desc.replace("$1", stunduration + "");
		desc = desc.replace("$2", percentage + "");
		return desc;
		}
		else
		{
			return getDescription().replace("$1", "X").replace("$2", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("duration", Integer.valueOf(15000));
		node.set("stun-duration", Integer.valueOf(500));
		node.set("percentage", Double.valueOf(1.25));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(15000), false);
		int stunduration = SkillConfigManager.getUseSetting(hero, skill, "stun-duration", Integer.valueOf(500), false);
		double percent = SkillConfigManager.getUseSetting(hero, skill, "percentage", Double.valueOf(1.25), false);

		if(hero.hasEffect("Stab"))
		{
			hero.removeEffect(hero.getEffect("Stab"));
		}
		
		StabEffect sEffect = new StabEffect(skill, duration, stunduration, percent);
		
		hero.addEffect(sEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "Stab" + ChatColor.GRAY + "! The next opponent you hit will be stunned!");
		
		return SkillResult.NORMAL;
	}
	
	public class StabEffect extends ImbueEffect
	{
		private int stunduration;
		private double percent;
		
		public StabEffect(Skill skill, int duration, int stunduration, double percent)
		{
			super(skill, "Stab", duration);
			this.stunduration=stunduration;
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
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "Stab" + ChatColor.GRAY + "!", new Object());
		}
		
		public int getStunDuration()
		{
			return this.stunduration;
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
				
				if(hero.hasEffect("Stab"))
				{
					if(event.getEntity() instanceof LivingEntity)
					{
						StabEffect sEffect = (StabEffect) hero.getEffect("Stab");
						
						if(event.getEntity() instanceof Player)
						{
							Player tplayer = (Player) event.getEntity();
							Hero thero = plugin.getCharacterManager().getHero(tplayer);
							
							thero.addEffect(new StunEffect(skill, sEffect.getStunDuration()));
						}
						else
						{
							LivingEntity target = (LivingEntity) event.getEntity();
							
							CharacterTemplate ct = plugin.getCharacterManager().getCharacter(target);
							
							ct.addEffect(new SlowEffect(skill, sEffect.getStunDuration(), 2, true, Messaging.getLivingEntityName(target) + ChatColor.GRAY + " has been slowed by " + sEffect.getPlayer().getDisplayName(), Messaging.getLivingEntityName(target) + ChatColor.GRAY + " is no longer slowed by " + sEffect.getPlayer().getDisplayName(), hero));
						}
						
						double damage = event.getDamage()*sEffect.getPercent();
						
						event.setDamage(damage);
						
						hero.removeEffect(sEffect);
					}
				}
			}
		}
	}
}
