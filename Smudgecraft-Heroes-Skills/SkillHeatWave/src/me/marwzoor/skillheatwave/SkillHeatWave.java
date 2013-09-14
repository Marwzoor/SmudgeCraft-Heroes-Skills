package me.marwzoor.skillheatwave;

import java.util.Iterator;

import net.smudgecraft.heroeslib.commoneffects.BurnEffect;
import net.smudgecraft.heroeslib.util.ParticleEffects;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillHeatWave extends ActiveSkill
{
	public static SkillHeatWave skill;	
	
	public SkillHeatWave(Heroes instance)
	{
		super(instance, "Heatwave");
		skill=this;
		setDescription("You send out a cone shaped heatwave which sets all enemies within the area of effect on fire, dealing $X burn damage every second for $Y seconds. M: $1 CD: $2");
		setUsage("/skill heatwave");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill heatwave" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new TwinLashLinstener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, "burn-damage", 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "burn-damage-increase", 0.5, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 5000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			duration = duration/1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", damage + "").replace("$Y", duration + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 5000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 50);
		node.set("burn-damage", 50);
		node.set("burn-damage-increase", 0.5);
		node.set("range", 10);
		node.set("range-increase", 0);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		Vector v = hero.getPlayer().getLocation().getDirection();
		Location loc = hero.getPlayer().getLocation().add(0,0.5,0);
		double damage = (SkillConfigManager.getUseSetting(hero, skill, "burn-damage", 50, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "burn-damage-increase", 0.5, false) * hero.getSkillLevel(skill)));
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 5000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		int range = (SkillConfigManager.getUseSetting(hero, skill, "range", 10, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "range-increase", 0, false) * hero.getSkillLevel(skill)));
		try
		{
			makeEffect(range, loc.clone(), v);
		}
		catch(Exception e)
		{
		}
		
		Location base = loc.clone().add(v);
		Location top1 = loc.clone().add(range*v.getX(), range*v.getY(), range*v.getZ()).add(v.getZ()*range/2, 0, v.getX()*(range/2)*-1);
		Location top2 = loc.clone().add(range*v.getX(), range*v.getY(), range*v.getZ()).add(v.getZ()*(range/2)*-1, 0, v.getX()*range/2);
		
		hitEntities(base,top1,top2, damage, duration, hero.getPlayer());
		
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
				" sent out a " + ChatColor.WHITE + "Heatwave" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	private void makeEffect(int range, Location loc, Vector direction) throws Exception
	{
		int counter = 0;
		while(counter < range)
		{
			loc.add(direction);
			int sides = counter*2;
			ParticleEffects.FLAME.sendToLocation(loc,0.3f,0.3f,0.3f, (float)0.025, 2);
			Location side1 = loc.clone();
			Location side2 = loc.clone();
			for(int i=1;i<=sides;++i)
			{
				if(i <= sides/2)
				{
					side1.add(direction.getZ(), 0, direction.getX()*-1);
					ParticleEffects.FLAME.sendToLocation(side1,0.3f,0.3f,0.3f, 0.025F, 2);
				}
				else
				{
					side2.add(direction.getZ()*-1, 0, direction.getX());
					ParticleEffects.FLAME.sendToLocation(side2,0.3f,0.3f,0.3f, 0.025F, 2);
				}
			}
			++counter;
		}
	}
	
	private void hitEntities(Location base, Location top1, Location top2, double damage, double duration, Player caster)
	{
		Iterator<Entity> itr = base.getWorld().getEntities().iterator();
		while(itr.hasNext())
		{
			Entity en = itr.next();
			if(!(en instanceof LivingEntity))
				continue;
			LivingEntity target = (LivingEntity)en;
			if(target.equals(caster))
				continue;
			if(!pointInTriangle(target.getLocation(),base,top1,top2))
				continue;
			plugin.getCharacterManager().getCharacter(target).addEffect(new BurnEffect(skill, 1000, (int)duration, damage, caster, false));
		}
	}
	
	private double sign(Location p1, Location p2, Location p3)
	{
		return (p1.getX() - p3.getX()) * (p2.getZ() - p3.getZ()) - (p2.getX() - p3.getX()) * (p1.getZ() - p3.getZ());
	}

	private boolean pointInTriangle(Location pt, Location v1, Location v2, Location v3)
	{
		boolean b1, b2, b3;

		b1 = sign(pt, v1, v2) < 0.0f;
		b2 = sign(pt, v2, v3) < 0.0f;
		b3 = sign(pt, v3, v1) < 0.0f;

		return ((b1 == b2) && (b2 == b3));
	}
}
