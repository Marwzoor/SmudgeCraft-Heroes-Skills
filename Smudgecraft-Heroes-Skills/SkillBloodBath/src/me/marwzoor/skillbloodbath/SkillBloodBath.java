package me.marwzoor.skillbloodbath;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillBloodBath extends ActiveSkill {
	public Heroes plugin;
	
	public SkillBloodBath(Heroes plugin) {
		super(plugin, "BloodBath");
		this.plugin = plugin;
		setDescription("You deal $1 damage to anyone bleeding within $2 blocks.");
		setIdentifiers(new String[] {
				"skill bloodbath"
		});
		setTypes(new SkillType[] {
				SkillType.DAMAGING,
				SkillType.HARMFUL
		});
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.RADIUS.node(), Integer.valueOf(10));
		node.set(SkillSetting.RADIUS_INCREASE.node(), Integer.valueOf(0));
		node.set(SkillSetting.DAMAGE.node(), Integer.valueOf(20));
		node.set(SkillSetting.DAMAGE_INCREASE.node(), Integer.valueOf(0));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(10), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE, Integer.valueOf(0), false)) * hero.getSkillLevel(this);
			int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Integer.valueOf(20), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Integer.valueOf(0), false)) * hero.getSkillLevel(this);
			return super.getDescription().replace("$1", damage + "").replace("$2", radius + "");
		} else {
			return super.getDescription().replace("$1", "X").replace("$2", "X");
		}
	}
	
	public SkillResult use(Hero hero, String[] args) {
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(10), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE, Integer.valueOf(0), false)) * hero.getSkillLevel(this);
		int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Integer.valueOf(20), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Integer.valueOf(0), false)) * hero.getSkillLevel(this);
		for (Entity e : hero.getPlayer().getNearbyEntities(radius, radius, radius)) {
			if (e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				CharacterTemplate ct = plugin.getCharacterManager().getCharacter(le);
				if (ct.hasEffect("Bleed")) {
					((Damageable) le).setHealth(((Damageable) le).getHealth() - damage);
				}
			}
		}
		
		return SkillResult.NORMAL;
	}
}
