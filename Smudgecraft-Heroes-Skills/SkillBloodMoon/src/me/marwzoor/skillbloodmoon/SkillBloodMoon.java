package me.marwzoor.skillbloodmoon;

import java.util.Iterator;

import net.smudgecraft.heroeslib.companions.Companion;
import net.smudgecraft.heroeslib.companions.CompanionPlayer;
import net.smudgecraft.heroeslib.companions.Companions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillBloodMoon extends ActiveSkill
{
	public static SkillBloodMoon skill;	
	
	public SkillBloodMoon(Heroes instance)
	{
		super(instance, "BloodMoon");
		skill=this;
		setDescription("You call upon the blood moon and your and your party members’ minions deal $X% more damage for $Y seconds. M: $1 CD: $2");
		setUsage("/skill bloodmoon");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill bloodmoon" });
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new BloodMoonLinstener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 60000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			double damagePercent = (SkillConfigManager.getUseSetting(hero, skill, "damage-percent", 0.5, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "damage-percent-increase", 0.005, false) * hero.getSkillLevel(skill)));
			duration = duration / 1000;
			damagePercent *= 100;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", damagePercent + "").replace("$Y", duration + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 60000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 50);
		node.set("damage-percent", 0.5);
		node.set("damage-percent-increase", 0.005);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 60000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		double damagePercent = (SkillConfigManager.getUseSetting(hero, skill, "damage-percent", 0.5, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "damage-percent-increase", 0.005, false) * hero.getSkillLevel(skill)));
		hero.addEffect(new BloodMoonEffect(this, (long) duration, damagePercent));
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] The Blood Moon has been summoned by " 
				+ ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + "!");
		return SkillResult.NORMAL;
	}
	
	public class BloodMoonEffect extends ExpirableEffect
	{
		protected double damagePercent;
		
		public BloodMoonEffect(Skill skill, long duration, double damagePercent)
		{
			super(skill, "BloodMoon", duration);
			this.damagePercent = damagePercent;
		}
		
		@Override
		public void applyToHero(Hero hero)
		{
			CompanionPlayer cp = Companions.getPlayerManager().getCompanionPlayer(hero.getPlayer());
			Iterator<Companion> itr = cp.getCompanions().iterator();
			while(itr.hasNext())
			{
				plugin.getCharacterManager().getMonster(itr.next().getLivingEntity()).addEffect(this);
			}
			if(hero.hasParty())
			{
				Iterator<Hero> itr2 = hero.getParty().getMembers().iterator();
				while(itr2.hasNext())
				{
					Hero temp = itr2.next();
					if(temp.equals(hero))
						continue;
					CompanionPlayer tempCP = Companions.getPlayerManager().getCompanionPlayer(temp.getPlayer());
					Iterator<Companion> itr3 = tempCP.getCompanions().iterator();
					while(itr3.hasNext())
					{
						plugin.getCharacterManager().getMonster(itr3.next().getLivingEntity()).addEffect(this);
					}
				}
			}
			super.applyToHero(hero);
		}
		
		@Override
		public void applyToMonster(Monster monster)
		{
			super.applyToMonster(monster);
		}
		
		@Override
		public void removeFromHero(Hero hero)
		{
			broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] The Blood Moon summoned by " + ChatColor.DARK_RED + 
					hero.getName() + ChatColor.GRAY + " has disappeared!");
			super.removeFromHero(hero);
		}
	}
	
	public class BloodMoonLinstener implements Listener
	{
		@EventHandler(priority = EventPriority.HIGH)
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(!(event.getAttackerEntity() instanceof LivingEntity))
				return;
			
			if(!Companions.getPlayerManager().hasOwner((LivingEntity) event.getAttackerEntity()))
				return;
			
			Companion cmp = Companions.getPlayerManager().getCompanionByEntity((LivingEntity) event.getAttackerEntity());
			
			Monster monster = plugin.getCharacterManager().getMonster(cmp.getLivingEntity());
			
			if(!monster.hasEffect("BloodMoon"))
				return;
			
			event.setDamage(cmp.getDamage() + (cmp.getDamage() * ((BloodMoonEffect)monster.getEffect("BloodMoon")).damagePercent));
		}
	}
}
