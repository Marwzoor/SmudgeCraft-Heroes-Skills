package me.marwzoor.shieldstance;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillShieldStance extends ActiveSkill {
	
	public SkillShieldStance(Heroes plugin) {
		super(plugin, "ShieldStance");
		setDescription("You block all incoming damage while wielding a sheild, but you are unable to move. D: %1s.");
		setIdentifiers(new String[] {
			"skill shieldstance"
		});
		setArgumentRange(0, 0);
		setTypes(new SkillType[] {
			SkillType.BUFF
		});
		Bukkit.getServer().getPluginManager().registerEvents(new ShieldStanceListener(this, plugin), plugin);
	}
	
	public String getDescription(Hero hero) {
		if(hero.hasAccessToSkill(this)) {
			String desc = super.getDescription();
			long duration = (long) ((SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Long.valueOf(15000), false) + SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Long.valueOf(100), false) * hero.getSkillLevel(this)) / 1000);
			desc.replace("%1", duration + "");
			return desc;
		} else {
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Long.valueOf(15000));
		node.set(SkillSetting.DURATION_INCREASE.node(), Long.valueOf(100));
		node.set("shield-item", Integer.valueOf(36));
		node.set(SkillSetting.COOLDOWN.node(), Long.valueOf(30000));
		return node;
	}

	@Override
	public SkillResult use(Hero hero, String[] args) {
		if (hero.hasEffect("ShieldStance")) {
			hero.removeEffect(hero.getEffect("ShieldStance"));
			return SkillResult.NORMAL;
		}
		Player player = hero.getPlayer();
		int shieldItem = SkillConfigManager.getUseSetting(hero, this, "shield-item", Integer.valueOf(36), false);
		
		if (player.getItemInHand().getTypeId() != shieldItem) {
			Messaging.send(player, "You are not wielding a shield!");
			return SkillResult.FAIL;
		}
				
		long duration = (long) ((SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Long.valueOf(15000), false) + SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Long.valueOf(100), false) * hero.getSkillLevel(this)) / 1000);
		hero.addEffect(new ShieldStanceEffect(this, duration));
		return SkillResult.NORMAL;
	}
	
	public class ShieldStanceEffect extends ExpirableEffect {
		
		private Skill skill;
		
		public ShieldStanceEffect(Skill skill, long duration) {
			super(skill, "ShieldStance", duration);
			this.skill = skill;
			types.add(EffectType.INVULNERABILITY);
			types.add(EffectType.ROOT);
		}
		
		@Override
		public void applyToHero(Hero hero) {
			super.applyToHero(hero);
			Player player = hero.getPlayer();
			hero.setCooldown("ShieldStance", 0);
			player.setSneaking(true);
			broadcast(player.getLocation(), player.getDisplayName() + ChatColor.GRAY + " has entered a " + ChatColor.WHITE + "ShieldStance" + ChatColor.GRAY + ".");
		}
		
		@Override
		public void removeFromHero(Hero hero) {
			super.removeFromHero(hero);
			Player player = hero.getPlayer();
			player.setSneaking(false);
			hero.setCooldown("ShieldStance", (long) SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, Long.valueOf(30000), false));
			broadcast(player.getLocation(), player.getDisplayName() + ChatColor.GRAY + " is no longer in a " + ChatColor.WHITE + "ShieldStance" + ChatColor.GRAY + ".");
		}
	}
	
	public class ShieldStanceListener implements Listener {
		
		private Skill skill;
		private Heroes heroes;
		
		public ShieldStanceListener(Skill skill, Heroes heroes) {
			this.skill = skill;
			this.heroes = heroes;
		}
		
		@EventHandler
		public void onItemChange(PlayerItemHeldEvent event) {
			Player player = event.getPlayer();
			Hero hero = heroes.getCharacterManager().getHero(player);
			int shieldItem = SkillConfigManager.getUseSetting(hero, skill, "shield-item", Integer.valueOf(36), false);
			if (player.getInventory().getItem(event.getNewSlot()).getTypeId() != shieldItem) {
				if (hero.hasEffect("ShieldStance")) {
					hero.removeEffect(hero.getEffect("ShieldStance"));
				}
			}
		}
	}

}
