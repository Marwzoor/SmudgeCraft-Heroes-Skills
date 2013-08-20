package me.marwzoor.skillswiftness;

import org.bukkit.configuration.ConfigurationSection;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillSwiftness extends ActiveSkill {

	public SkillSwiftness(Heroes plugin) {
		super(plugin, "Swiftness");
		setDescription("You become $1% faster.");
		setIdentifiers(new String[] {
				"skill swiftness"
		});
		setTypes(new SkillType[] {
				SkillType.BUFF
		});
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set("speed-increase", Integer.valueOf(20));
		return node;
	}
	
	public String getDescription(Hero hero) {
		if (hero.hasAccessToSkill(this)) {
			int speed = SkillConfigManager.getUseSetting(hero, this, "speed-increase", Integer.valueOf(20), false);
			return super.getDescription().replace("$1", speed + "");
		} else {
			return super.getDescription().replace("$1", "X");
		}
	}
	
	public SkillResult use(Hero hero, String[] args) {
		double speed = SkillConfigManager.getUseSetting(hero, this, "speed-increase", Integer.valueOf(20), false) * 0.01;
		if (hero.hasEffect("Swiftness")) {
			hero.removeEffect(hero.getEffect("Swiftness"));
			return SkillResult.NORMAL;
		} else {
			SwiftnessEffect sEffect = new SwiftnessEffect(this, speed);
			hero.addEffect(sEffect);
			return SkillResult.NORMAL;
		}
	}
	
	public class SwiftnessEffect extends Effect {
		private double speed;
		
		public SwiftnessEffect(Skill skill, double speed) {
			super(skill, "Swiftness");
			this.speed = speed;
		}
		
		@Override
		public void applyToHero(Hero hero) {
			super.applyToHero(hero);
			double walkSpeed = 0.2 * speed + 0.2;
			hero.getPlayer().setWalkSpeed((float) walkSpeed);
		}
		
		@Override
		public void removeFromHero(Hero hero) {
			super.applyToHero(hero);
			hero.getPlayer().setWalkSpeed(0.2f);
		}
	}
}
