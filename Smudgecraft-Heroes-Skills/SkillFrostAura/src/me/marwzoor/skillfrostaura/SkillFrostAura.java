package me.marwzoor.skillfrostaura;

import net.smudgecraft.heroeslib.util.PlayerUtils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillFrostAura extends ActiveSkill
{
	public SkillFrostAura(Heroes instance)
	{
		super(instance, "FrostAura");
		setDescription("You form a Frost Aura around you, slowing enemies within %1 blocks and dealing %2 damage every %3 they remain inside the area. M: %4 CD: %5 R: %6 D: %7 DMG: %8");
		setUsage("/skill frostaura");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill frostaura" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.DEBUFF});
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000;
			int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false);
			int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false)
					+ (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(this)))/1000;
			double period = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD, 1000, false)/1000;
			double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 50, false)
					+ (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0.5, false) * hero.getSkillLevel(this)));
			
			if(((int)period)==1)
			{
				return super.getDescription().replace("%1", radius + "").replace("%2", damage + "").replace("%3", "second").replace("%4", mana + "").replace("%5", cooldown + "s").replace("%6", radius + "").replace("%7", duration + "s").replace("%8", damage + "");
			}
			else
			{
				return super.getDescription().replace("%1", radius + "").replace("%2", damage + "").replace("%3", period + " seconds").replace("%4", mana + "").replace("%5", cooldown + "s").replace("%6", radius + "").replace("%7", duration + "s").replace("%8", damage + "");
			}
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "X").replace("%3", "second").replace("%4", "Xs").replace("%5", "X").replace("%6", "Xs");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.MANA.node(), 0);
		node.set(SkillSetting.COOLDOWN.node(), 0);
		node.set(SkillSetting.RADIUS.node(), 10);
		node.set(SkillSetting.DURATION.node(), 10000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 100);
		node.set(SkillSetting.PERIOD.node(), 1000);
		node.set(SkillSetting.DAMAGE.node(), 30);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 0.5);
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		if(hero.hasEffect("FrostAura"))
		{
			hero.removeEffect(hero.getEffect("FrostAura"));
			return SkillResult.NORMAL;
		}
		
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false);
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false)
				+ (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(this));
		int period = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD, 1000, false);
		double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 50, false)
				+ (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0.5, false) * hero.getSkillLevel(this)));
		
		FrostAuraEffect faEffect = new FrostAuraEffect(this, period, duration, damage, radius);
		
		hero.addEffect(faEffect);
		
		this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] A " + ChatColor.WHITE + "Frost Aura" + ChatColor.GRAY + " has been formed around " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	public class FrostAuraEffect extends PeriodicExpirableEffect
	{
		private int radius;
		private double damage;
		
		public FrostAuraEffect(SkillFrostAura skill, int period, int duration, double damage, int radius)
		{
			super(skill, "FrostAura", period, duration);
			this.damage=damage;
			this.radius=radius;
		}
		
		public int getRadius()
		{
			return this.radius;
		}
		
		public double getDamage()
		{
			return this.damage;
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			skill.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + "'s" + ChatColor.WHITE + " Frost Aura " + ChatColor.GRAY + "has been dissolved!");
		}
		
		public void tickHero(Hero hero)
		{
			for(Entity en : hero.getPlayer().getNearbyEntities(radius, radius, radius))
			{
				if(en instanceof LivingEntity)
				{
					if(PlayerUtils.damageCheck(hero.getPlayer(), (LivingEntity) en))
					{
						skill.damageEntity((LivingEntity) en, hero.getPlayer(), damage);
						skill.addSpellTarget((LivingEntity) en, hero);
						
						((LivingEntity) en).addPotionEffect(PotionEffectType.SLOW.createEffect((int) this.getPeriod(), 2));
					}
				}
			}
		}

		public void tickMonster(Monster monster) 
		{
			
		}
	}
}
