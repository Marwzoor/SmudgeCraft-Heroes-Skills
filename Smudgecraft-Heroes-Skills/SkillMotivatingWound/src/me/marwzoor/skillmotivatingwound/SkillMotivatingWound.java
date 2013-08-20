package me.marwzoor.skillmotivatingwound;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillMotivatingWound extends ActiveSkill {
	public Heroes plugin;
	
	public SkillMotivatingWound(Heroes plugin) {
		super(plugin, "MotivatingWound");
		this.plugin = plugin;
		setDescription("You sacrifice $1% health and deal $2% more damage for $3 seconds.");
		setIdentifiers(new String[] {
				"skill motivatingwound"
		});
		setTypes(new SkillType[] {
				SkillType.BUFF
		});
		Bukkit.getPluginManager().registerEvents(new SkillMotivatingWoundListener(this, plugin), plugin);
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.HEALTH_COST.node(), Integer.valueOf(20));
		node.set(SkillSetting.DAMAGE.node(), Integer.valueOf(20));
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(20000));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, Integer.valueOf(20), false);
			int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Integer.valueOf(20), false);
			int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(20000), false) / 1000;
			return super.getDescription().replace("$1", healthCost + "").replace("$2", damage + "").replace("$3", duration + "");
		} else {
			return super.getDescription().replace("$1", "X").replace("$2", "X").replace("$3", "X");
		}
	}
	
	public SkillResult use(Hero hero, String[] args) {
		double healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, Integer.valueOf(20), false) * 0.01;
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(20000), false);
		
		if (hero.hasEffect("MotivatingWound")) {
			Messaging.send(hero.getPlayer(), "You are already affected by MotivatingWound!");
			return SkillResult.NORMAL;
		}
		
		MotivatingWoundEffect mvEffect = new MotivatingWoundEffect(this, plugin, duration);
		hero.addEffect(mvEffect);
		double health = ((Damageable) hero.getPlayer()).getMaxHealth() * healthCost;
		hero.getPlayer().setHealth(((Damageable) hero.getPlayer()).getHealth() - health);
		return SkillResult.NORMAL;
	}
	
	public class MotivatingWoundEffect extends ExpirableEffect {
		
		public MotivatingWoundEffect(Skill skill, Heroes plugin, int duration) {
			super(skill, plugin, "MotivatingWound", duration);
		}
		
	}
	
	public class SkillMotivatingWoundListener implements Listener {
		private SkillMotivatingWound skill;
		private Heroes plugin;
		
		public SkillMotivatingWoundListener(SkillMotivatingWound skill, Heroes plugin) {
			this.skill = skill;
			this.plugin = plugin;
		}
		
		@EventHandler
		public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
			if (event.isCancelled()) {
				return;
			}
			
			if (!(event.getDamager() instanceof Player)) {
				return;
			}
			
			Player player = (Player) event.getDamager();
			Hero hero = plugin.getCharacterManager().getHero(player);
			
			if (hero.hasEffect("MotivatingWound")) {
				double damageIncrease = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, Integer.valueOf(20), false) * 0.01;
				double damage = event.getDamage();
				event.setDamage(damage + damage * damageIncrease);
			}
		}
	}

}
