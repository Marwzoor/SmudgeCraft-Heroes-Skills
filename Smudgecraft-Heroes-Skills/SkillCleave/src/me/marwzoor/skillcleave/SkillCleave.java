package me.marwzoor.skillcleave;

import java.util.List;

import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.party.HeroParty;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.util.Util;

public class SkillCleave extends ActiveSkill {
	private Heroes plugin;
	
	public SkillCleave(Heroes plugin) {
		super(plugin, "Cleave");
		this.plugin = plugin;
		setDescription("Attack up to %1 enemies in front of you. R:%2 DMG:%3 D:%4 CD:%5 M:%6");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill cleave" });
		setTypes(new SkillType[] { SkillType.DAMAGING, SkillType.PHYSICAL });

		Bukkit.getServer().getPluginManager().registerEvents(new SkillListener(this), plugin);
	}

	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set("maxtargets", Integer.valueOf(3));
		node.set(SkillSetting.RADIUS.node(), Integer.valueOf(5));
		node.set(SkillSetting.DAMAGE.node(), Double.valueOf(3));
		node.set(SkillSetting.DAMAGE_INCREASE.node(), Double.valueOf(0.5));
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(5000));
		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(0));
		node.set(SkillSetting.MANA.node(), Integer.valueOf(0));
		return node;
	}

	public String getDescription(Hero hero) {
		String desc = super.getDescription();
		if(hero.hasAccessToSkill(this)) {
			int targets = SkillConfigManager.getUseSetting(hero, this, "maxtargets", Integer.valueOf(3), false);
			int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), Integer.valueOf(5), false);
			double damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Double.valueOf(3), false) + SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Double.valueOf(0.5), false) * hero.getSkillLevel(this);
			int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(5000), false) / 1000;
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, Integer.valueOf(0), false);
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, Integer.valueOf(0), false);
			return desc.replace("%1", targets + "").replace("%2", radius + "").replace("%3", damage + "").replace("%4", duration + "").replace("%5", cooldown + "").replace("%6", mana + "");
		} else {
			return desc.replace("%1", "X").replace("%2", "X").replace("%3", "X").replace("%4", "X");
		}
	}

	public SkillResult use(Hero hero, String[] args) {
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(5000), false);

		CleaveBuff cb = new CleaveBuff(this, duration);
		hero.addEffect(cb);
		return SkillResult.NORMAL;
	}

	public int damageAround(Player player, Entity exception, Skill skill, int newDmg) {
		Hero hero = plugin.getCharacterManager().getHero(player);
		int MaxTargets = SkillConfigManager.getUseSetting(hero, this, "maxtargets", Integer.valueOf(3), false) - 1;
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(5), false);

		int Hits = 0;
		List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
		HeroParty hParty = hero.getParty();
		if (hParty != null) {
			for (Entity entity : nearby) {
				if (Hits >= MaxTargets)
					break;
				if (entity.equals(exception)) {
					continue;
				}
				if (((entity instanceof Player)) && (hParty.isPartyMember((Player) entity))) {
					continue;
				}
				if ((((entity instanceof Monster)) || ((entity instanceof ComplexLivingEntity)) || ((entity instanceof Player))) && (isInFront(player, entity))) {
					damageEntity((LivingEntity) entity, player, newDmg, DamageCause.ENTITY_ATTACK);
					Hits++;
				}
			}
		} else {
			for (Entity entity : nearby) {
				if (Hits >= MaxTargets)
					break;
				if (entity.equals(exception)) {
					continue;
				}
				if ((((entity instanceof Monster)) || ((entity instanceof ComplexLivingEntity)) || ((entity instanceof Player))) && (isInFront(player, entity))) {
					damageEntity((LivingEntity) entity, player, newDmg, DamageCause.ENTITY_ATTACK);
					Hits++;
				}
			}
		}
		return Hits;
	}

	private boolean isInFront(Player player, Entity target) {
		if (!target.getWorld().equals(player.getWorld())) {
			return false;
		}
		Location pLoc = player.getLocation();
		Location tLoc = target.getLocation();
		Vector u = player.getLocation().getDirection().normalize();
		Vector v = new Vector(tLoc.getX() - pLoc.getX(), 0.0D, tLoc.getZ() - pLoc.getZ());
		double magU = Math.sqrt(Math.pow(u.getX(), 2.0D) + Math.pow(u.getZ(), 2.0D));
		double magV = Math.sqrt(Math.pow(v.getX(), 2.0D) + Math.pow(v.getZ(), 2.0D));
		double angle = Math.acos(u.dot(v) / (magU * magV));
		angle = angle * 180.0D / 3.141592653589793D;
		return angle < 90.0D;
	}

	public class CleaveBuff extends ImbueEffect {

		public CleaveBuff(Skill skill, int duration) {
			super(skill, "CleaveBuff", duration);
		}

		public void applyToHero(Hero hero) {
			super.applyToHero(hero, this);
			Messaging.send(hero.getPlayer(), "You are preparing to cleave!");
		}

		public void removeFromHero(Hero hero) {
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "The urge to cleave fades from you.");
		}
	}

	public class SkillListener implements Listener {
		private SkillCleave skill;

		public SkillListener(Skill skill) {
			this.skill = (SkillCleave) skill;
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onEntityDamage(EntityDamageEvent event) {
			if (!(event instanceof EntityDamageByEntityEvent)) {
				return;
			}

			Entity initTarg = event.getEntity();
			if ((!(initTarg instanceof LivingEntity)) && (!(initTarg instanceof Player))) {
				return;
			}

			EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
			if (!(subEvent.getDamager() instanceof Player)) {
				return;
			}

			Player player = (Player) subEvent.getDamager();
			Hero hero = plugin.getCharacterManager().getHero(player);
			if (hero.hasEffect("CleaveBuff")) {
				ItemStack item = player.getItemInHand();
				if ((!Util.swords.contains(item.getType().name())) && (!Util.axes.contains(item.getType().name()))) {
					return;
				}
				CleaveBuff cb = (CleaveBuff) hero.getEffect("CleaveBuff");

				hero.removeEffect(cb);
				int damage = (int) Math.round(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, Integer.valueOf(3), false) + SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, Double.valueOf(0.5), false) * hero.getSkillLevel(skill));
				event.setDamage(damage);
				int hitAmount = skill.damageAround(player, initTarg, skill, damage) + 1;

				skill.broadcast(player.getLocation(), player.getName() + " hit " + hitAmount + " players with " + ChatColor.WHITE + "Cleave" + ChatColor.GRAY + "!");
			}
		}
	}
}
