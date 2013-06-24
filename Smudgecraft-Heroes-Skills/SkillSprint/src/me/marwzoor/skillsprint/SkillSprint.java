package me.marwzoor.skillsprint;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.QuickenEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

import org.bukkit.configuration.ConfigurationSection;

public class SkillSprint extends ActiveSkill
{
  private String applyText;
  private String expireText;

  public SkillSprint(Heroes plugin)
  {
    super(plugin, "Sprint");
    setDescription("You sprint, giving a burst of speed for $1 seconds.");
    setUsage("/skill sprint");
    setArgumentRange(0, 0);
    setIdentifiers(new String[] { "skill sprint" });
    setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT, SkillType.SILENCABLE });
  }

  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection node = super.getDefaultConfig();
    node.set("speed-multiplier", Integer.valueOf(2));
    node.set(SkillSetting.DURATION.node(), Integer.valueOf(15000));
    node.set("apply-text", "%hero% is now sprinting!");
    node.set("expire-text", "%hero% is no longer sprinting!");
    return node;
  }

  public void init()
  {
    super.init();
    this.applyText = SkillConfigManager.getRaw(this, SkillSetting.APPLY_TEXT, "%hero% is now sprinting!").replace("%hero%", "$1");
    this.expireText = SkillConfigManager.getRaw(this, SkillSetting.EXPIRE_TEXT, "%hero% is no longer sprinting!").replace("%hero%", "$1");
  }

  public SkillResult use(Hero hero, String[] args)
  {
    int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 15000, false);
    int multiplier = SkillConfigManager.getUseSetting(hero, this, "speed-multiplier", 2, false);
    if (multiplier > 20) {
      multiplier = 20;
    }
    hero.addEffect(new QuickenEffect(this, getName(), duration, multiplier, this.applyText, this.expireText));

    return SkillResult.NORMAL;
  }

  public String getDescription(Hero hero)
  {
    int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 1, false);
    return getDescription().replace("$1", duration / 1000 + "");
  }
}