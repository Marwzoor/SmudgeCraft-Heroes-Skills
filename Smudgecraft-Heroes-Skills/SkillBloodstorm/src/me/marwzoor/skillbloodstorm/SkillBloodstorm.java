package me.marwzoor.skillbloodstorm;

import net.smudgecraft.heroeslib.commoneffects.BleedEffect;
import net.smudgecraft.heroeslib.util.ParticleEffects;

import org.bukkit.configuration.ConfigurationSection;
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

public class SkillBloodstorm extends ActiveSkill {
public Heroes plugin;
	
	public SkillBloodstorm(Heroes plugin) {
		super(plugin, "Bloodstorm");
		this.plugin = plugin;
		setDescription("You slice yourself, unleashing cursed blood in all directions, making everyone within $1 blocks recieve $2 bleed damage for $3 seconds.");
		setIdentifiers(new String[] {
				"skill bloodstorm"
		});
		setTypes(new SkillType[] {
				SkillType.DAMAGING,
				SkillType.HARMFUL,
				SkillType.DEBUFF
		});
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.RADIUS.node(), Integer.valueOf(10));
		node.set(SkillSetting.RADIUS_INCREASE.node(), Integer.valueOf(0));
		node.set(SkillSetting.DAMAGE.node(), Double.valueOf(5));
		node.set(SkillSetting.DAMAGE_INCREASE.node(), Double.valueOf(0));
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(10000));
		node.set(SkillSetting.DURATION_INCREASE.node(), Integer.valueOf(0));
		node.set(SkillSetting.PERIOD.node(), Integer.valueOf(1000));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(10), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE, Integer.valueOf(0), false)) * hero.getSkillLevel(this);
			double damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Double.valueOf(5), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Double.valueOf(0), false)) * hero.getSkillLevel(this);
			int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(10000), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(0), false)) * hero.getSkillLevel(this)) / 1000;
			return super.getDescription().replace("$1", radius + "").replace("$2", damage + "").replace("$3", duration + "");
		} else {
			return super.getDescription().replace("$1", "X").replace("$2", "X").replace("$3", "X");
		}
	}
	
	public SkillResult use(Hero hero, String[] args) {
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, Integer.valueOf(10), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE, Integer.valueOf(0), false)) * hero.getSkillLevel(this);
		double damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Double.valueOf(20), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Double.valueOf(0), false)) * hero.getSkillLevel(this);
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(10000), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(0), false)) * hero.getSkillLevel(this);
		int period = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD, Integer.valueOf(10000), false);
		for (Entity e : hero.getPlayer().getNearbyEntities(radius, radius, radius)) {
			if (e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				CharacterTemplate ct = plugin.getCharacterManager().getCharacter(le);
				if (ct.hasEffect("Bleed")) {
					ct.removeEffect(ct.getEffect("Bleed"));
				}
				BleedEffect bEffect = new BleedEffect(this, period, duration, damage, hero.getPlayer(), false, true);
				ct.addEffect(bEffect);
			}
		}
		
		try {
			ParticleEffects.DRIP_LAVA.sendToLocation(hero.getPlayer().getLocation(), radius, radius, radius, 1.0f, 100);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return SkillResult.NORMAL;
	}

}
