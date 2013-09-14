package me.marwzoor.skillsearingflames;

import java.util.Iterator;

import net.smudgecraft.heroeslib.util.ParticleEffects;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillSearingFlames extends ActiveSkill
{
	public static SkillSearingFlames skill;	
	
	public SkillSearingFlames(Heroes instance)
	{
		super(instance, "SearingFlames");
		skill=this;
		setDescription("Deals $X damage to enemies that are burning within $Y blocks. M: $1 CD: $2");
		setUsage("/skill searingflames");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill searingflames" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new FlameballListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 10, false) * hero.getSkillLevel(skill)));
			double radius = (SkillConfigManager.getUseSetting(hero, skill, "radius", 15, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "radius-increase", 0, false) * hero.getSkillLevel(skill)));
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", damage + "").replace("$Y", radius + "").replace("$1", mana + "").replace("$2", cd + "");
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
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 10);
		node.set("range", 15);
		node.set("range-increase", 0);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 50, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 10, false) * hero.getSkillLevel(skill)));
		double radius = (SkillConfigManager.getUseSetting(hero, skill, "radius", 15, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "radius-increase", 0, false) * hero.getSkillLevel(skill)));
		
		Iterator<Entity> itr = hero.getPlayer().getNearbyEntities(radius, radius, radius).iterator();
		
		while(itr.hasNext())
		{
			Entity en = itr.next();
			if(!(en instanceof LivingEntity))
				continue;
			LivingEntity target = (LivingEntity)en;
			if(target instanceof Player)
			{
				if(hero.hasParty())
				{
					if(hero.getParty().getMembers().contains(plugin.getCharacterManager().getHero((Player)target)))
							continue;
				}
			}
			if(plugin.getCharacterManager().getCharacter(target).hasEffect("Burn"))
			{
				skill.damageEntity(target, hero.getPlayer(), damage);
				try
				{
					ParticleEffects.FLAME.sendToLocation(target.getEyeLocation(),0.3f,0.3f,0.3f, 0.025F, 10);
				}
				catch(Exception e)
				{
				}
			}
		}
		
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
				" used " + ChatColor.WHITE + "Searing Flames" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
}
