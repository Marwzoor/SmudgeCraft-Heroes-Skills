package me.marwzoor.skillrecklessslam;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillRecklessSlam extends TargettedSkill {

	public SkillRecklessSlam(Heroes instance) {
		super(instance, "RecklessSlam");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill recklessslam" });
		setDescription("You charge and slam your target, dealing $1 damage to them and dealing $2 damage to everyone within $3 blocks while sending them through the air.");
		setTypes(new SkillType[] { SkillType.FORCE, SkillType.HARMFUL, SkillType.DAMAGING, SkillType.INTERRUPT, SkillType.MOVEMENT });
	}

	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.MAX_DISTANCE.node(), Integer.valueOf(8));
		node.set(SkillSetting.DAMAGE.node(), Double.valueOf(70));
		node.set(SkillSetting.DAMAGE_INCREASE.node(), Double.valueOf(1));
		node.set("aoe", Double.valueOf(5));
		node.set("aoe-increase", Double.valueOf(0.1));
		node.set("aoe-damage", Double.valueOf(50));
		node.set("aoe-damage-increase", Double.valueOf(1));
		node.set("knockup-distance", Double.valueOf(5));
		node.set("knockup-distance-increase", Double.valueOf(0.1));
		node.set(SkillSetting.COOLDOWN.node(), Long.valueOf(20000));
		node.set(SkillSetting.MANA.node(), Integer.valueOf(20));
		return node;
	}

	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Double.valueOf(70), false) 
					+ SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Double.valueOf(1), false) * hero.getSkillLevel(this));
			double aoe = (SkillConfigManager.getUseSetting(hero, this, "aoe", Double.valueOf(50), false) 
					+ SkillConfigManager.getUseSetting(hero, this, "aoe-increase", Double.valueOf(1), false) * hero.getSkillLevel(this));
			double aoeDmg = (SkillConfigManager.getUseSetting(hero, this, "aoe-damage", Double.valueOf(50), false) 
					+ SkillConfigManager.getUseSetting(hero, this, "aoe-damage-increase", Double.valueOf(1), false) * hero.getSkillLevel(this));
			return super.getDescription().replace("$1", damage + "").replace("$2", aoeDmg + "").replace("$3", aoe + "");
		} else {
			return super.getDescription().replace("$1", "X").replace("$2", "X").replace("$3", "X");
		}
	}

	public SkillResult use(Hero hero, LivingEntity target, String[] args) {
		final Player player = hero.getPlayer();

		if (player == target) {
			return SkillResult.INVALID_TARGET;
		}

		int maxdistance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, Integer.valueOf(7), false);

		if(player.getLocation().distance(target.getLocation()) > maxdistance) {
			Messaging.send(player, "Target is too far away!");

			return SkillResult.FAIL;
		}

		Vector v = target.getLocation().toVector().subtract(player.getLocation().toVector());
		v = v.multiply(1.6).setY(0.5D);
		player.setVelocity(v);

		broadcast(player.getLocation(), player.getDisplayName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "RecklessSlam" + ChatColor.GRAY + "!");

		double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Double.valueOf(70), false) 
				+ SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Double.valueOf(1), false) * hero.getSkillLevel(this));
		double aoe = (SkillConfigManager.getUseSetting(hero, this, "aoe", Double.valueOf(50), false) 
				+ SkillConfigManager.getUseSetting(hero, this, "aoe-increase", Double.valueOf(1), false) * hero.getSkillLevel(this));
		double aoeDmg = (SkillConfigManager.getUseSetting(hero, this, "aoe-damage", Double.valueOf(50), false) 
				+ SkillConfigManager.getUseSetting(hero, this, "aoe-damage-increase", Double.valueOf(1), false) * hero.getSkillLevel(this));
		double knockup = (SkillConfigManager.getUseSetting(hero, this, "knockup-distance", Double.valueOf(5), false) 
				+ SkillConfigManager.getUseSetting(hero, this, "knockup-distance-increase", Double.valueOf(0.1), false) * hero.getSkillLevel(this));

		addSpellTarget(target, hero);
		damageEntity(target, player, damage, DamageCause.ENTITY_ATTACK);
		
		for (Entity e : player.getNearbyEntities(aoe, 0, aoe)) {
			if (!(e instanceof LivingEntity)) {
				continue;
			}
			LivingEntity le = (LivingEntity) e;
			le.damage(aoeDmg);
			Location loc = le.getLocation();
			Location newLoc = le.getLocation();
			newLoc.setY(newLoc.getY() + knockup);
			Vector vector = newLoc.toVector().subtract(loc.toVector());
			le.setVelocity(vector);
		}

		return SkillResult.NORMAL;
	}
}
