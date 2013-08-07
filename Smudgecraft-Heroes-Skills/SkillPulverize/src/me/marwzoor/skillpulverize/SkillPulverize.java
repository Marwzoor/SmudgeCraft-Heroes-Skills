package me.marwzoor.skillpulverize;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillPulverize extends ActiveSkill
{
	public SkillPulverize(Heroes instance)
	{
		super(instance, "Pulverize");
		setDescription("You strike the ground furiously, slowing all enemies within %1 blocks for %2 seconds and dealing %3 damage. R: %4 DMG: %5 M: %6 CD: %7 D: %8");
		setUsage("/skill pulverize");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill pulverize" });
		setTypes(new SkillType[] { SkillType.PHYSICAL, SkillType.INTERRUPT, SkillType.FORCE, SkillType.MOVEMENT });
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 5, false);
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false);
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			cooldown = cooldown/1000;
			int damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 100, false) +
					(SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 1, false) * hero.getSkillLevel(this)));
			
			int duration = (SkillConfigManager.getUseSetting(hero, this, "slow-duration", 3000, false) +
					(SkillConfigManager.getUseSetting(hero, this, "slow-duration-increase", 50, false) * hero.getSkillLevel(this)));
			duration = duration/1000;
			
			return super.getDescription().replace("%1", radius + "").replace("%2", duration + "").replace("%3", damage + "").replace("%4", radius + "").replace("%5", damage + "").replace("%6", mana + "").replace("%7", cooldown + "s").replace("%8", duration + "s");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "X").replace("%3", "X").replace("%4", "X").replace("%5", "X").replace("%6", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.RADIUS.node(), 5);
		node.set(SkillSetting.DAMAGE.node(), 100);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 1);
		node.set("slow-duration", 3000);
		node.set("slow-duration-increase", 50);
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 5, false);
		
		double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 100, false) +
				(SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 1, false) * hero.getSkillLevel(this)));
		
		int duration = (SkillConfigManager.getUseSetting(hero, this, "slow-duration", 3000, false) +
				(SkillConfigManager.getUseSetting(hero, this, "slow-duration-increase", 50, false) * hero.getSkillLevel(this)));
		duration = ((duration/1000)*20)*2;
		
		for(Entity en : hero.getPlayer().getNearbyEntities(radius, radius, radius))
		{
			if(en instanceof LivingEntity && en.isOnGround())
			{
				LivingEntity le = (LivingEntity) en;
				le.addPotionEffect(PotionEffectType.SLOW.createEffect(duration, 5));
				Skill.damageEntity(le, hero.getEntity(), damage, DamageCause.MAGIC);
				this.addSpellTarget(le, hero);
				le.setVelocity(new Vector(0, 0.35, 0));
			}
			else if(en.isOnGround())
			{
				en.setVelocity(new Vector(0, 0.35, 0));
			}
		}
		
		this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.WHITE + hero.getPlayer().getName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "Pulverize" + ChatColor.GRAY + "!");
				
		return SkillResult.NORMAL;
	}
}
