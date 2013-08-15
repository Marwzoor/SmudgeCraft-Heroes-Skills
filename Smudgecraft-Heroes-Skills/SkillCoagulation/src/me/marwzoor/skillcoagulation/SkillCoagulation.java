package me.marwzoor.skillcoagulation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillCoagulation extends ActiveSkill
{
	public SkillCoagulation(Heroes instance)
	{
		super(instance, "Coagulation");
		setDescription("You slow anyone bleeding within %1 blocks for %2 seconds and coagulate your wounds, healing you by %3. D: %4 M: %5 CD: %6 R: %7");
		setUsage("/skill coagulation");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill coagulation" });
		setTypes(new SkillType[] { SkillType.HARMFUL, SkillType.DAMAGING, SkillType.MOVEMENT});
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int duration = SkillConfigManager.getUseSetting(hero, this, "slow-duration", 7000, false)/1000;
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000;
			int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false);
			int heal = (int)(SkillConfigManager.getUseSetting(hero, this, "heal", 0.1D, false)*100);
			
			return super.getDescription().replace("%1", radius + "").replace("%2", duration + "").replace("%3", heal + "%").replace("%4", duration + "s").replace("%5", mana + "").replace("%6", cooldown + "s").replace("%7", radius + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "X").replace("%3", "X").replace("%4", "Xs").replace("%5", "X").replace("%6", "Xs").replace("%7", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("slow-duration", 7000);
		node.set("slow-amplifier", 2);
		node.set(SkillSetting.RADIUS.node(), 10);
		node.set(SkillSetting.COOLDOWN.node(), 0);
		node.set(SkillSetting.MANA.node(), 0);
		node.set("heal", 0.1D);
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false);
		
		List<Entity> nearbyEntities = hero.getPlayer().getNearbyEntities(radius, radius, radius);
		
		if(nearbyEntities.isEmpty())
		{
			Messaging.send(hero.getPlayer(), "There was no entity in range!");
			return SkillResult.NORMAL;
		}
		
		List<LivingEntity> livingEntities = new ArrayList<LivingEntity>();
		
		for(Entity en : nearbyEntities)
		{
			if(en instanceof LivingEntity)
			{
				livingEntities.add((LivingEntity)en);
			}
		}
		
		if(livingEntities.isEmpty())
		{
			Messaging.send(hero.getPlayer(), "There was no entity in range!");
			return SkillResult.NORMAL;
		}
		
		int duration = SkillConfigManager.getUseSetting(hero, this, "slow-duration", 7000, false);
		int amplifier = SkillConfigManager.getUseSetting(hero, this, "slow-amplifier", 2, false);
		
		if(amplifier>10)
			amplifier=10;
				
		for(LivingEntity le : livingEntities)
		{
			CharacterTemplate c = plugin.getCharacterManager().getCharacter(le);
			
			if(c.hasEffect("Bleed"))
			{
				if(c instanceof Hero)
				{
					c.addEffect(new SlowEffect(this, "Slow", duration, amplifier, true, ChatColor.DARK_RED + ((Player)le).getName() + "'s" + ChatColor.GRAY + " blood has been coagulated by " + ChatColor.RED + hero.getPlayer().getName() + ChatColor.GRAY + "!", ChatColor.DARK_RED + ((Player) le).getName() + "'s" + ChatColor.GRAY + " blood has stopped coagulate.", hero));
				}
				else
				{
					c.addEffect(new SlowEffect(this, "Slow", duration, amplifier, true, ChatColor.WHITE + le.getType().getName() + "'s" + ChatColor.GRAY + " blood has been coagulated by " + ChatColor.RED + hero.getPlayer().getName() + ChatColor.GRAY + "!", ChatColor.WHITE + le.getType().getName() + "'s" + ChatColor.GRAY + " blood has stopped coagulate.", hero));
				}
			}
		}
		
		double heal = SkillConfigManager.getUseSetting(hero, this, "heal", 0.1D, false);
		
		hero.heal((hero.getPlayer().getMaxHealth()*heal));
		
		this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "Coagulation" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
}
