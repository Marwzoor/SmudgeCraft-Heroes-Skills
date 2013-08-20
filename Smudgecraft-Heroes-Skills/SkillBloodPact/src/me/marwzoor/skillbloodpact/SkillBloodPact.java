package me.marwzoor.skillbloodpact;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;

public class SkillBloodPact extends ActiveSkill {
	public Heroes plugin;
	
	public SkillBloodPact(Heroes plugin) {
		super(plugin, "BloodPact");
		this.plugin = plugin;
		setDescription("You target up to three allies within $1 blocks to join a blood pact. You each gain $2% max health for $3 seconds, but if a blood pact member dies you each take damage equal to $4% of your new max health. You may only enter a blood pact with Faction allies.");
		setIdentifiers(new String[] {
				"skill bloodpact"
		});
		setTypes(new SkillType[] {
				SkillType.BUFF,
				SkillType.DEBUFF
		});
		Bukkit.getPluginManager().registerEvents(new SkillBloodPactListener(plugin), plugin);
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.RADIUS.node(), Integer.valueOf(10));
		node.set("max-health-gain", Integer.valueOf(20));
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(20000));
		node.set(SkillSetting.DAMAGE.node(), Integer.valueOf(15));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(10), false);
			int max = SkillConfigManager.getUseSetting(hero, this, "max-health-gain", Integer.valueOf(20), false);
			int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(20000), false) / 1000;
			int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Integer.valueOf(15), false);
			return super.getDescription().replace("$1", radius + "").replace("$2", max + "").replace("$3", duration + "").replace("$4", damage + "");
		} else {
			return super.getDescription().replace("$1", "X").replace("$2", "X").replace("$3", "X").replace("$4", "X");
		}
	}
	
	public SkillResult use(Hero hero, String[] args) {
		if (hero.hasEffect("BloodPact")) {
			return SkillResult.FAIL;
		}
		Player player = hero.getPlayer();
		UPlayer uPlayer = UPlayer.get(player);
		Faction faction = uPlayer.getFaction();
		
		ArrayList<Player> participating = new ArrayList<Player>();
		participating.add(player);
				
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(10), false);
		int max = SkillConfigManager.getUseSetting(hero, this, "max-health-gain", Integer.valueOf(20), false);
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(20000), false);
		int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Integer.valueOf(15), false);
		for (Entity e : hero.getPlayer().getNearbyEntities(radius, radius, radius)) {
			if (e instanceof Player) {
				Player p = (Player) e;
				Hero h = plugin.getCharacterManager().getHero(p);
				UPlayer up = UPlayer.get(p);
				Faction f = up.getFaction();
				if (participating.size() == 4) {
					break;
				}
				
				if (h.hasEffect("BloodPact")) {
					continue;
				}
				
				if (faction == f) {
					participating.add(p);
				} else if (faction.getRelationWish(f) == Rel.ALLY) {
					participating.add(p);
				}
			}
		}
		
		BloodPactEffect bpEffect = new BloodPactEffect(this, plugin, max, duration, damage, participating);
		for (Player p : participating) {
			Messaging.send(p, "You have entered a blood pact!");
			Hero h = plugin.getCharacterManager().getHero(p);
			h.addEffect(bpEffect);
		}
		
		return SkillResult.NORMAL;
	}
	
	public class BloodPactEffect extends ExpirableEffect {
		private int maxHealthPercIncrease;
		private int damagePerc;
		private ArrayList<Player> participating;
		
		public BloodPactEffect(SkillBloodPact skill, Heroes plugin, int max, int duration, int damage, ArrayList<Player> participating) {
			super(skill, plugin, "BloodPact", duration);
			this.maxHealthPercIncrease = max;
			this.damagePerc = damage;
			this.participating = participating;
		}
		
		public int getDamagePerc() {
			return damagePerc;
		}
		
		public ArrayList<Player> getParticipating() {
			return participating;
		}
		
		@Override
		public void applyToHero(Hero hero) {
			super.applyToHero(hero);
			double maxHealth = ((Damageable) hero.getPlayer()).getMaxHealth();
			double newMaxHealth = maxHealth + maxHealth * (maxHealthPercIncrease * 0.01);
			hero.getPlayer().setMaxHealth(newMaxHealth);
		}
		
		@Override
		public void removeFromHero(Hero hero) {
			super.removeFromHero(hero);
			hero.getPlayer().setMaxHealth(hero.getHeroClass().getBaseMaxHealth() + (hero.getHeroClass().getMaxHealthPerLevel() * hero.getLevel(hero.getHeroClass())));
		}
	}
	
	public class SkillBloodPactListener implements Listener {
		private Heroes plugin;
		
		public SkillBloodPactListener(Heroes plugin) {
			this.plugin = plugin;
		}
		
		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event) {
			Player player = event.getPlayer();
			Hero hero = plugin.getCharacterManager().getHero(player);
			if (hero.hasEffect("BloodPact")) {
				hero.removeEffect(hero.getEffect("BloodPact"));
			}
		}
		
		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent event) {
			Player player = event.getEntity();
			Hero hero = plugin.getCharacterManager().getHero(player);
			if (hero.hasEffect("BloodPact")) {
				BloodPactEffect bpEffect = (BloodPactEffect) hero.getEffect("BloodPact");
				ArrayList<Player> participating = bpEffect.getParticipating();
				for (Player p : participating) {
					if (player == p) {
						continue;
					}
					
					int damagePerc = bpEffect.getDamagePerc();
					p.setHealth(((Damageable) p).getHealth() - ((Damageable) p).getMaxHealth() * (damagePerc * 0.01));
					Messaging.send(player, "A blood pact member has died!");
				}
			}
		}
		
	}

}
