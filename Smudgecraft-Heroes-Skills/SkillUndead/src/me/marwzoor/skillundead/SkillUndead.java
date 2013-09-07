package me.marwzoor.skillundead;

import net.smudgecraft.heroeslib.util.ParticleEffects;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillUndead extends ActiveSkill
{
	public static SkillUndead skill;	
	
	public SkillUndead(Heroes instance)
	{
		super(instance, "Undead");
		skill=this;
		setDescription("You turn undead and are not targeted by monsters for $X seconds. M: $1 CD: $2");
		setUsage("/skill undead");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill undead" });
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new UndeadListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 120000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(skill)));
			duration = duration / 1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", duration + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 120000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 100);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 120000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(skill)));
		hero.addEffect(new UndeadEffect(this, (long) duration, hero.getPlayer()));
		//broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + 
		//		" Your skin peels as you become undead. Monsters no longer target you!");
		Messaging.send(hero.getPlayer(), "Your skin peels as you become undead. Monsters no longer target you.");
		return SkillResult.NORMAL;
	}
	
	public class UndeadEffect extends PeriodicDamageEffect
	{
		public UndeadEffect(Skill skill, long duration, Player p)
		{
			super(skill, "Undead", 1000, duration, 0D, p, false);
		}
		
		@Override
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
		}
		
		@Override
		public void removeFromHero(Hero hero)
		{
			Messaging.send(hero.getPlayer(), "You are no longer undead and monsters target you!");
			super.removeFromHero(hero);
		}
		
		@Override
		public void tickHero(Hero hero)
		{
			try
			{
				ParticleEffects.CLOUD.sendToLocation(hero.getPlayer().getLocation(), 1f, 1f, 1f, 0.5f, 50);
			}
			catch(Exception e)
			{
			}
		}
	}
	
	public class UndeadListener implements Listener
	{
		@EventHandler
		public void onEntityTargetEvent(EntityTargetEvent event)
		{
			Entity en = event.getTarget();
			if(!(en instanceof Player))
				return;
			Hero hero = plugin.getCharacterManager().getHero((Player)en);
			if(hero.hasEffect("Undead"))
			{
				event.setCancelled(true);
				return;
			}
		}
		
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			Entity en = event.getAttackerEntity();
			if(!(en instanceof Player))
				return;
			Entity target = event.getEntity();
			if(target instanceof Player || !(target instanceof Monster))
				return;
			Hero hero = plugin.getCharacterManager().getHero((Player)en);
			if(!hero.hasEffect("Undead"))
				return;
			((Monster)target).setTarget(hero.getPlayer());
		}
	}
}
