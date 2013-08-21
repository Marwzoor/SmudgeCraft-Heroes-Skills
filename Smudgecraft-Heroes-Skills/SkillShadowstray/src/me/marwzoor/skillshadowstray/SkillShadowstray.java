package me.marwzoor.skillshadowstray;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillShadowstray extends TargettedSkill {

	public SkillShadowstray(Heroes instance) {
		super(instance, "Shadowstray");
		setDescription("You stray in the shadows, ending up behind your enemy i fthey are within $1 blocks.");
		setIdentifiers(new String[] {
				"skill shadowstray"
		});
		setTypes(new SkillType[] {
				SkillType.TELEPORT
		});
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.RADIUS.node(), Integer.valueOf(20));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(20), false);
			return super.getDescription().replace("$1", radius + "");
		} else {
			return super.getDescription().replace("$1", "X");
		}
	}
	
	public SkillResult use(Hero hero, LivingEntity target, String[] args) {
		Player player = hero.getPlayer();
		if (player == target) {
			return SkillResult.INVALID_TARGET;
		}
		
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(20), false);
		if (player.getLocation().distance(target.getLocation()) > radius) {
			Messaging.send(player, "Target is too far away!");
			return SkillResult.FAIL;
		}
		
		Location loc = target.getLocation();
		Block behind = loc.getBlock();
		float direction = loc.getYaw();
		
		if (direction < 0) {
			direction += 360;
		}
		int intDirection = Math.round((direction + 45) / 90);
		
		switch(intDirection) {
		case 1:
			behind = target.getWorld().getBlockAt(behind.getX() + 1, behind.getY(), behind.getZ());
			break;
		case 2:
			behind = target.getWorld().getBlockAt(behind.getX(), behind.getY(), behind.getZ() + 1);
			break;
		case 3:
			behind = target.getWorld().getBlockAt(behind.getX() - 1, behind.getY(), behind.getZ());
			break;
		case 4:
			behind = target.getWorld().getBlockAt(behind.getX(), behind.getY(), behind.getZ() - 1);
			break;
		case 0:
			behind = target.getWorld().getBlockAt(behind.getX(), behind.getY(), behind.getZ() - 1);
			break;
		default:
			break;
		}
		
		player.teleport(behind.getLocation());
		return SkillResult.NORMAL;
	}
}
