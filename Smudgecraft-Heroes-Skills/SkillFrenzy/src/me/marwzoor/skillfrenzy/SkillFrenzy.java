package me.marwzoor.skillfrenzy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillFrenzy extends ActiveSkill {
	public String applyText;
	public String removeText;
	
	public SkillFrenzy(Heroes instance) {
		super(instance, "Frenzy");
		setDescription("You absorb $1% of all damage for $2 seconds and you gain $3% of all damage dealt as health.");
		setIdentifiers(new String[] {
				"skill frenzy"
		});
		setArgumentRange(0, 0);
		setTypes(new SkillType[] {
				SkillType.BUFF, SkillType.HEAL
		});
		Bukkit.getPluginManager().registerEvents(new SkillFrenzyListener(instance), instance);
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(10000));
		node.set(SkillSetting.DURATION_INCREASE.node(), Integer.valueOf(100));
		node.set("absorb-percentage", Double.valueOf(15));
		node.set("absorb-percentage-increase", Double.valueOf(0.05));
		node.set("heal-percentage", Double.valueOf(50));
		node.set("heal-percentage-increase", Double.valueOf(0.05));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			double absorb = (SkillConfigManager.getUseSetting(hero, this, "absorb-percentage", Double.valueOf(20), false)
					+ SkillConfigManager.getUseSetting(hero, this, "absorb-percentage-increase", Double.valueOf(0.1), false) * hero.getSkillLevel(this));
			double duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(10000), false)
					+ SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(100), false) * hero.getSkillLevel(this)) / 1000;
			double heal = (SkillConfigManager.getUseSetting(hero, this, "heal-percentage", Double.valueOf(20), false)
					+ SkillConfigManager.getUseSetting(hero, this, "heal-percentage-increase", Double.valueOf(0.1), false) * hero.getSkillLevel(this));
			return super.getDescription().replace("$1", absorb + "").replace("$2", duration + "").replace("$3", heal + "");
		} else {
			return super.getDescription().replace("$1", "X").replace("$2", "X").replace("$3", "X");
		}
	}
	
	public SkillResult use(Hero hero, String[] args) {
		double absorb = (SkillConfigManager.getUseSetting(hero, this, "absorb-percentage", Double.valueOf(20), false)
				+ SkillConfigManager.getUseSetting(hero, this, "absorb-percentage-increase", Double.valueOf(0.1), false) * hero.getSkillLevel(this));
		int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(10000), false)
				+ SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(100), false) * hero.getSkillLevel(this));
		double heal = (SkillConfigManager.getUseSetting(hero, this, "heal-percentage", Double.valueOf(20), false)
				+ SkillConfigManager.getUseSetting(hero, this, "heal-percentage-increase", Double.valueOf(0.1), false) * hero.getSkillLevel(this));
		
		FrenzyEffect fEffect = new FrenzyEffect(this, duration, absorb, heal);
		
		if (hero.hasEffect("Frenzy")) {
			hero.removeEffect(hero.getEffect("Frenzy"));
		}
		
		hero.addEffect(fEffect);
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "] " + ChatColor.WHITE + hero.getPlayer().getName() + ChatColor.GRAY + " has entered a " + ChatColor.WHITE + "Frenzy" + ChatColor.GRAY + "!");
		return SkillResult.NORMAL;
	}
	
	public class FrenzyEffect extends ExpirableEffect {
		private double absorbPerc;
		private double healPerc;
		private SkillFrenzy skill;
		
		public FrenzyEffect(SkillFrenzy skill, int duration, double absorbPerc, double healPerc) {
			super(skill, "Frenzy", duration);
			this.skill = skill;
			this.absorbPerc = absorbPerc;
			this.healPerc = healPerc;
		}
		
		public double getAbsorbPerc() {
			return this.absorbPerc;
		}
		
		public double getHealPerc() {
			return this.healPerc;
		}
		
		public void applyToHero(Hero hero) {
			super.applyToHero(hero);
		}
		
		public void removeFromHero(Hero hero) {
			super.removeFromHero(hero);
		    skill.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "] " + ChatColor.WHITE + hero.getPlayer().getName() + ChatColor.GRAY + " is no longer in a " + ChatColor.WHITE + "Frenzy" + ChatColor.GRAY + "!");
		}
	}
	
	public class SkillFrenzyListener implements Listener {
		private Heroes plugin;
		
		public SkillFrenzyListener(Heroes plugin) {
			this.plugin = plugin;
		}
		
		@EventHandler
		public void onEntityDamage(EntityDamageEvent event) {
			if (event.isCancelled()) {
				return;
			}
			
			if (event instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) event;
				if (ede.getDamager() instanceof Player) {
					Player p = (Player) ede.getDamager();
					Hero hero = plugin.getCharacterManager().getHero(p);
					if (hero.hasEffect("Frenzy")) {
						p.playEffect(new Location(event.getEntity().getWorld(), event.getEntity().getLocation().getX(), event.getEntity().getLocation().getY() + 1, event.getEntity().getLocation().getZ()), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
						FrenzyEffect fEffect = (FrenzyEffect) hero.getEffect("Frenzy");
						double healPerc = fEffect.getHealPerc() * 0.01;
						double heal = event.getDamage() * healPerc;
						hero.heal(heal);
					}
				}
			}
			
			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				Hero hero = plugin.getCharacterManager().getHero(player);
				if (hero.hasEffect("Frenzy")) {
					FrenzyEffect fEffect = (FrenzyEffect) hero.getEffect("Frenzy");
					double absorbPerc = fEffect.getAbsorbPerc() * 0.01;
					double finalDmg = Math.round(event.getDamage() - (event.getDamage() * absorbPerc));
					event.setDamage(finalDmg);
				}
			}
		}
	}

}
