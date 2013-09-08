package me.marwzoor.skilllightningbolt;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillLightningbolt extends TargettedSkill
{
	public static SkillLightningbolt skill;	
	
	public SkillLightningbolt(Heroes instance)
	{
		super(instance, "Lightningbolt");
		skill=this;
		setDescription("You smite your enemy with lightning, dealing $X damage. M: $1 CD: $2");
		setUsage("/skill lightningbolt");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill lightningbolt" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 150, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", damage + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(), 150);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 50);
		return node;
	}
	
	@Override
	public SkillResult use(final Hero hero, final LivingEntity target, String[] args)
	{
		if(hero.getEntity().equals(target))
		{
			Messaging.send(hero.getPlayer(), "You can't target yourself!");
			return SkillResult.INVALID_TARGET_NO_MSG;
		}
		/*double distance = hero.getPlayer().getLocation().distance(target.getLocation());
		if(SkillConfigManager.getUseSetting(hero, skill, "range", 15, false) < distance)
		{
			Messaging.send(hero.getPlayer(), "The target is out of range!");
			return SkillResult.INVALID_TARGET_NO_MSG;
		}*/
		
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 150, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		target.getWorld().strikeLightningEffect(target.getLocation());
		damageEntity(target, hero.getPlayer(), damage);
		if(target instanceof Player)
			broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + 
					" smited " + ChatColor.DARK_RED + ((Player)target).getName() + ChatColor.GRAY + "!");
		else
			broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + 
					" smited " + ChatColor.DARK_GREEN + target.getType().getName() + ChatColor.GRAY + "!");
		return SkillResult.NORMAL;	
	}
}
