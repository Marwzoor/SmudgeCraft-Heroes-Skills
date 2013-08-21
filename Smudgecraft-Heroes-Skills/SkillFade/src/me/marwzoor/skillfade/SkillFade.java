package me.marwzoor.skillfade;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillFade extends PassiveSkill {
	
	public SkillFade(Heroes plugin) {
		super(plugin, "Fade");
		setDescription("You become invisible for $1 seconds when sneaking in a light level below $2");
		setIdentifiers(new String[] {
				"skill fade"
		});
		setEffectTypes(new EffectType[] {
				EffectType.INVISIBILITY
		});
		setTypes(new SkillType[] {
				SkillType.ILLUSION
		});
		Bukkit.getPluginManager().registerEvents(new SkillFadeListener(this, plugin), plugin);
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(30000));
		node.set(SkillSetting.DURATION_INCREASE.node(), Integer.valueOf(0));
		node.set("light-level", Integer.valueOf(4));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(30000), false) + SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(0), false) * hero.getSkillLevel(this)) / 1000;
			int light = SkillConfigManager.getUseSetting(hero, this, "light-level", Integer.valueOf(4), false);
			return super.getDescription().replace("$1", duration + "").replace("$2", light + "");
		} else {
			return super.getDescription().replace("$1", "X").replace("$2", "X");
		}
	}
	
	public class FadeEffect extends ExpirableEffect {
		
		public FadeEffect(SkillFade skill, Heroes plugin, int duration) {
			super(skill, plugin, "Fade", duration);
		}
		
		@Override
		public void applyToHero(Hero hero) {
			super.applyToHero(hero);
			Player player = hero.getPlayer();
			for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
				if (player.equals(onlinePlayer) || !player.getWorld().equals(onlinePlayer.getWorld()))
					continue;

				if (player.getLocation().distanceSquared(onlinePlayer.getLocation()) > 16000) {
					continue;
				}

				onlinePlayer.hidePlayer(player);
			}
		}
		
		@Override
		public void removeFromHero(Hero hero) {
			super.removeFromHero(hero);
			Player player = hero.getPlayer();
			for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
				if (onlinePlayer.equals(player) || !player.getWorld().equals(onlinePlayer.getWorld()))
					continue;
				if (player.getLocation().distanceSquared(onlinePlayer.getLocation()) > 16000) {
					continue;
				}

				onlinePlayer.showPlayer(player);
			}
		}
	}
	
	public class SkillFadeListener implements Listener {
		private SkillFade skill;
		private Heroes plugin;
		
		public SkillFadeListener(SkillFade skill, Heroes plugin) {
			this.skill = skill;
			this.plugin = plugin;
		}
		
		@EventHandler
		public void onPlayerSneak(PlayerToggleSneakEvent event) {
			if (event.isCancelled()) {
				return;
			}
			
			Player player = event.getPlayer();
			Hero hero = plugin.getCharacterManager().getHero(player);
			if (!hero.hasEffect("Fade")) {
				return;
			}
			
			if (!event.isSneaking()) {
				hero.removeEffect(hero.getEffect("Fade"));
				return;
			}
			
			int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, Integer.valueOf(30000), false) + SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, Integer.valueOf(0), false) * hero.getSkillLevel(skill);
			if (player.getLocation().getBlock().getLightLevel() <= 4) {
				FadeEffect fe = new FadeEffect(skill, plugin, duration);
				hero.addEffect(fe);
			}
		}
		
		@EventHandler
		public void onPlayerMove(PlayerMoveEvent event) {
			if (event.isCancelled()) {
				return;
			}
			
			Player player = event.getPlayer();
			Hero hero = plugin.getCharacterManager().getHero(player);
			if (!hero.hasEffect("Fade")) {
				return;
			}
			
			if (event.getTo().getBlock().getLightLevel() > 4) {
				hero.removeEffect(hero.getEffect("Fade"));
			}
		}
		
		@EventHandler
		public void onEntityTarget(EntityTargetEvent event) {
			if (!(event.getTarget() instanceof Player)) {
				return;
			}
			
			Player player = (Player) event.getTarget();
			Hero hero = plugin.getCharacterManager().getHero(player);
			if (!hero.hasEffect("Fade")) {
				return;
			}
			
			if (!(event.getEntity() instanceof Monster)) {
				return;
			}
			
			event.setCancelled(true);
		}
	}

}
