package me.marwzoor.skillrushingblood;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillRushingBlood extends PassiveSkill {
	public HashMap<Player, Double> stackedDamage = new HashMap<Player, Double>();
	
	public SkillRushingBlood(Heroes plugin) {
		super(plugin, "RushingBlood");
		setDescription("When you take damage, your damage is boosted with that amount next hit. Stacks up to $1 damage");
		setIdentifiers(new String[] {
				"skill rushingblood"
		});
		setEffectTypes(new EffectType[] {
				EffectType.BENEFICIAL
		});
		setTypes(new SkillType[] {
				SkillType.BUFF
		});
		Bukkit.getPluginManager().registerEvents(new SkillRushingBloodListener(this, plugin), plugin);
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set("damage-stack", Double.valueOf(20));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			double stack = SkillConfigManager.getUseSetting(hero, this, "damage-stack", Double.valueOf(20), false);
			return super.getDescription().replace("$1", stack + "");
		} else {
			return super.getDescription().replace("$1", "X");
		}
	}
	
	public HashMap<Player, Double> getStackedDamage() {
		return stackedDamage;
	}
	
	public class SkillRushingBloodListener implements Listener {
		private SkillRushingBlood skill;
		private Heroes heroes;
		
		public SkillRushingBloodListener(SkillRushingBlood skill, Heroes heroes) {
			this.skill = skill;
			this.heroes = heroes;
		}
		
		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent event) {
			Player player = event.getPlayer();
			Hero hero = heroes.getCharacterManager().getHero(player);
			if (hero.hasAccessToSkill("RushingBlood")) {
				Messaging.send(player, "Your blood begins to rush...");
				skill.getStackedDamage().put(player, 0.0D);
			}
		}
		
		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			Player player = event.getPlayer();
			Hero hero = heroes.getCharacterManager().getHero(player);
			if (hero.hasAccessToSkill("RushingBlood")) {
				skill.getStackedDamage().remove(player);
			}
		}
		
		@EventHandler
		public void onEntityDamageByDamage(EntityDamageByEntityEvent event) {
			if (event.isCancelled()) {
				return;
			}
			
			if (event.getDamager() instanceof Player) {
				Player player = (Player) event.getDamager();
				Hero hero = heroes.getCharacterManager().getHero(player);
				if (hero.hasEffect("RushingBlood")) {
					double currentStacked = skill.getStackedDamage().get(player);					
					double damage = event.getDamage();
					event.setDamage(damage + currentStacked);
					event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
					skill.getStackedDamage().put(player, 0.0D);
				}
			}
			
			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				Hero hero = heroes.getCharacterManager().getHero(player);
				if (hero.hasEffect("RushingBlood")) {
					double stack = SkillConfigManager.getUseSetting(hero, skill, "damage-stack", Double.valueOf(20), false);
					double currentStacked = skill.getStackedDamage().get(player);					
					double damage = event.getDamage();
					
					if (currentStacked + damage > stack) {
						currentStacked = stack;
					} else {
						currentStacked += damage;
					}
					
					skill.getStackedDamage().put(player, currentStacked);
				}
			}
		}
	}

}
