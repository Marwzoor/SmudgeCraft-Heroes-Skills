package me.marwzoor.skillfrostbite;

import java.util.Iterator;

import net.smudgecraft.heroeslib.commoneffects.FreezeEffect;
import net.smudgecraft.heroeslib.util.PlayerUtils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillFrostBite extends ActiveSkill
{
	public static SkillFrostBite skill;	
	
	public SkillFrostBite(Heroes instance)
	{
		super(instance, "FrostBite");
		skill=this;
		setDescription("For $X seconds you make everyone within $Y blocks under the slow effect suffer from Frost Bite, dealing $Z damage every second if they are slowed. M: $1 CD: $2");
		setUsage("/skill frostbite");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill frostbite" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new TwinLashLinstener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 20, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 20000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(skill)));
			int radius = (SkillConfigManager.getUseSetting(hero, skill, "radius", 20, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "radius-increase", 0, false) * hero.getSkillLevel(skill)));
			duration = duration/1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", duration + "").replace("$Y", radius + "").replace("$Z", damage + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(), 50);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 20);
		node.set(SkillSetting.DURATION.node(), 20000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 100);
		node.set("radius", 20);
		node.set("radius-increase", 0);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 50, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 20, false) * hero.getSkillLevel(skill)));
		long duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 20000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(skill)));
		int radius = (SkillConfigManager.getUseSetting(hero, skill, "radius", 20, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "radius-increase", 0, false) * hero.getSkillLevel(skill)));
		
		hero.addEffect(new FrostBiteEffect(duration, damage, radius));
		
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
				" is triggering " + ChatColor.WHITE + "Frost Bite" + ChatColor.GRAY + " among slowed enemies!");
		
		return SkillResult.NORMAL;
	}
	
	public class FrostBiteEffect extends PeriodicExpirableEffect
	{
		private double damage;
		private int radius;
		
		public FrostBiteEffect(long duration, double damage, int radius)
		{
			super(SkillFrostBite.skill, "FrostBite", 1000, duration);
			this.damage = damage;
			this.radius = radius;
		}

		@Override
		public void tickHero(Hero hero)
		{
			Iterator<Entity> itr = hero.getPlayer().getNearbyEntities(radius, radius, radius).iterator();
			while(itr.hasNext())
			{
				Entity en = itr.next();
				if(!(en instanceof LivingEntity))
					continue;
				LivingEntity target = (LivingEntity)en;
				if(!PlayerUtils.damageCheck(hero.getPlayer(), target))
					continue;
				if(!FreezeEffect.hasFreezeEffect(plugin.getCharacterManager().getCharacter(target)))
					continue;
				skill.damageEntity(target, hero.getPlayer(), damage);
			}
		}

		@Override
		public void tickMonster(Monster monster)
		{
		}
		
		@Override
		public void removeFromHero(Hero hero)
		{
			broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + "'s" + ChatColor.GRAY +
					" " + ChatColor.WHITE + "Frost Bite" + ChatColor.GRAY + " is no longer active!");
			super.removeFromHero(hero);
		}
	}
}
