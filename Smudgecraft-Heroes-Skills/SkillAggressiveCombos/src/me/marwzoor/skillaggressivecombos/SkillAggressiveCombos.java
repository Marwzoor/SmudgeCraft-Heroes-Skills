package me.marwzoor.skillaggressivecombos;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.util.Util;

public class SkillAggressiveCombos extends PassiveSkill {
	public HashMap<Player, Double> breakChance = new HashMap<Player, Double>();
	public HashMap<Player, Boolean> comboing = new HashMap<Player, Boolean>();
	
	public SkillAggressiveCombos(Heroes plugin) {
		super(plugin, "Aggressive Combos");
		setDescription("You have $1% chance to begin a combo, dealing $2% more damage per hit, but the chance to break the combo is $3% each hit.");
		setEffectTypes(new EffectType[] {
				EffectType.BENEFICIAL
		});
		setTypes(new SkillType[] {
				SkillType.BUFF
		});
		Bukkit.getServer().getPluginManager().registerEvents(new SkillAggressiveCombosListener(this, plugin), plugin);
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.CHANCE.node(), Double.valueOf(50));
		node.set(SkillSetting.DAMAGE.node(), Double.valueOf(50));
		node.set(SkillSetting.DAMAGE_INCREASE.node(), Double.valueOf(1));
		node.set("chance-break", Double.valueOf(20));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			double chance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE, Double.valueOf(50), false);
			double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Double.valueOf(50), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Double.valueOf(1), false) * hero.getSkillLevel(this)));
			double chanceBrk = SkillConfigManager.getUseSetting(hero, this, "chance-break", Double.valueOf(20), false);
			return super.getDescription().replace("$1", chance + "").replace("$2", damage + "").replace("$3", chanceBrk + "");
		} else {
			return super.getDescription().replace("$1", "X").replace("$2", "X").replace("$3", "X");
		}
	}
	
	public HashMap<Player, Double> getBreakChance() {
		return breakChance;
	}
	
	public HashMap<Player, Boolean> getIsComboing() {
		return comboing;
	}
	
	public class SkillAggressiveCombosListener implements Listener {
		private SkillAggressiveCombos skill;
		private Heroes heroes;
		
		public SkillAggressiveCombosListener(SkillAggressiveCombos skill, Heroes heroes) {
			this.skill = skill;
			this.heroes = heroes;
		}
		
		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent event) {
			Player player = event.getPlayer();
			Hero hero = heroes.getCharacterManager().getHero(player);
			if (hero.hasEffect("Aggressive Combos")) {
				Bukkit.getLogger().info("Got here.");
				skill.getIsComboing().put(player, false);
				skill.getBreakChance().put(player, 0.0D);
			}
		}
		
		@EventHandler
		public void onWeaponDamage(WeaponDamageEvent event) {			
			if (event.isCancelled()) {
				return;
			}
						
			if (!(event.getDamager().getEntity() instanceof Player)) {
				return;
			}
						
			Player damager = (Player) event.getDamager().getEntity();
			Hero hero = heroes.getCharacterManager().getHero(damager);
			if (!hero.hasEffect("Aggressive Combos")) {
				return;
			}
						
			double chance = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, Double.valueOf(50), false) * 0.01;
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, Double.valueOf(50), false) + (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, Double.valueOf(1), false) * hero.getSkillLevel(skill))) * 0.01;
			double chanceBrk = SkillConfigManager.getUseSetting(hero, skill, "chance-break", Double.valueOf(20), false) * 0.01;
			
			if (!skill.getIsComboing().get(damager)) {
				double rand = Util.nextRand();
				if (rand <= chance) {
					Messaging.send(damager, "You have started a combo!");
					skill.getIsComboing().put(damager, true);
					skill.getBreakChance().put(damager, chanceBrk);
					event.setDamage(event.getDamage() + event.getDamage() * damage);
				}
			} else {
				event.setDamage(event.getDamage() + event.getDamage() * damage);
				double rand = Util.nextRand();
				if (rand <= skill.getBreakChance().get(damager)) {
					Messaging.send(damager, "You have ended a combo!");
					skill.getIsComboing().put(damager, false);
					skill.getBreakChance().put(damager, 0.0D);
				} else {
					skill.getBreakChance().put(damager, skill.getBreakChance().get(damager) + chanceBrk);
				}
			}
		}
	}
}
