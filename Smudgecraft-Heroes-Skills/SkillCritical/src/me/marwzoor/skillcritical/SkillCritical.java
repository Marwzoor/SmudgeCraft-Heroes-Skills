package me.marwzoor.skillcritical;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillCritical extends PassiveSkill {

	public SkillCritical(Heroes plugin) {
		super(plugin, "Critical");
		setDescription("You have a $1% chance to deal $2% more damage.");
		setTypes(new SkillType[] {
			SkillType.COUNTER, SkillType.BUFF
		});
		Bukkit.getServer().getPluginManager().registerEvents(new SkillCriticalListener(this), plugin);
	}
	
	public String getDescription(Hero hero) {
		if(hero.hasAccessToSkill(this))
		{
		double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE, 0.5, false) +
				(SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL, 0.005, false) * hero.getSkillLevel(this))) * 100;
		double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 1.5, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0.05, false) * hero.getSkillLevel(this)));
		
		damage = damage*100;
		damage = damage-100;
		
		int dmg = (int) damage;
		int c = (int) chance;
		
		return getDescription().replace("$1", c + "").replace("$2", dmg + "");
		}
		else
		{
			return super.getDescription().replace("$1", "X").replace("$2", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.CHANCE.node(), Double.valueOf(0.5));
		node.set(SkillSetting.CHANCE_LEVEL.node(), Double.valueOf(0.005));
		node.set(SkillSetting.DAMAGE.node(), Double.valueOf(20));
		node.set(SkillSetting.DAMAGE_INCREASE.node(), Double.valueOf(0.05));
		return node;
	}
	
	public class SkillCriticalListener implements Listener {
		
		private Skill skill;
		
		public SkillCriticalListener(Skill skill) {
			this.skill = skill;
		}
		
		@EventHandler
		public void onEntityDamage(WeaponDamageEvent event) {
			if (event.isCancelled()) {
				return;
			}
			
			if (!(event.getDamager() instanceof Hero)) {
				return;
			}
			
			Hero hero = (Hero) event.getDamager();
			if (!hero.hasAccessToSkill(skill)) {
				return;
			}
			
			double chance = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.5, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL, 0.005, false) * hero.getSkillLevel(skill)));
			if(hero.getPlayer().hasMetadata("CriticalChance")) {
				if(hero.getPlayer().getMetadata("CriticalChance").get(0)!=null) {
				chance += hero.getPlayer().getMetadata("CriticalChance").get(0).asDouble();
				}
			}
						
			if (Math.random() <= chance) {
				double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 1.5, false) +
		                (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0.05, false) * hero.getSkillLevel(skill)));
				
				double finaldamage = (event.getDamage() * damage);
				
				int extradamage = ((int)(finaldamage - event.getDamage()));
				
				event.setDamage((int) finaldamage);
				
				Messaging.send(hero.getPlayer(), "You performed a" + ChatColor.WHITE + " Critical" + ChatColor.GRAY + " hit dealing " + ChatColor.WHITE + extradamage + ChatColor.GRAY + " extra damage!");
			}
			
		}
	}
}
