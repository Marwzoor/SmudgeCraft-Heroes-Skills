package me.marwzoor.skillphoenix;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.CharacterDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillPhoenix extends PassiveSkill
{
	public static SkillPhoenix skill;
	
	public SkillPhoenix(Heroes plugin)
	{
		super(plugin,"Phoenix");
		//feedingPlayers = new HashMap();
		setDescription("You have a $X% chance to gain full health when you would have died.");
		//setArgumentRange(0,0);
		setTypes(new SkillType[]{SkillType.HEAL,SkillType.BUFF});
		setIdentifiers(new String[]{"skill phoenix"});
		Bukkit.getServer().getPluginManager().registerEvents(new PhoenixListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double chance = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.05, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL, 0.001, false) * hero.getSkillLevel(skill)));
			chance *= 100;
			return super.getDescription().replace("$1", chance + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.CHANCE.node(), 0.05);
		node.set(SkillSetting.CHANCE_LEVEL.node(), 0.001);
		return node;
	}
	
	public class PhoenixListener implements Listener
	{
		@EventHandler
		public void onCharacterDamageEvent(CharacterDamageEvent event)
		{
			if(!(event.getEntity() instanceof Player))
				return;
			Hero hero = plugin.getCharacterManager().getHero((Player)event.getEntity());
			if(!hero.hasAccessToSkill(skill))
				return;
			if((((Damageable)hero.getPlayer()).getHealth() - event.getDamage()) > 0)
				return;
			double chance = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.05, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL, 0.001, false) * hero.getSkillLevel(skill)));
			if(Math.random() > chance)
				return;
			hero.getPlayer().setHealth(((Damageable)hero.getPlayer()).getMaxHealth());
			broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
					" has risen from the ashes like a " + ChatColor.WHITE + "Phoenix" + ChatColor.GRAY + "!");
		}
	}
}
