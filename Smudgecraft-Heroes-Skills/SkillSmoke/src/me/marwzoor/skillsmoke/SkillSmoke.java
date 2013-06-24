package me.marwzoor.skillsmoke;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.InvisibleEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillSmoke extends ActiveSkill
{
  private String applyText;
  private String expireText;

  public SkillSmoke(Heroes plugin)
  {
    super(plugin, "Smoke");
    setDescription("You completely disappear from view.");
    setUsage("/skill smoke");
    setArgumentRange(0, 0);
    setIdentifiers(new String[] { "skill smoke" });
    setNotes(new String[] { "Note: Taking damage removes the effect" });
    setTypes(new SkillType[] { SkillType.ILLUSION, SkillType.BUFF, SkillType.COUNTER, SkillType.STEALTHY });
  }

  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection node = super.getDefaultConfig();
    node.set(SkillSetting.DURATION.node(), Integer.valueOf(20000));
    node.set(SkillSetting.APPLY_TEXT.node(), "You vanish in a cloud of smoke!");
    node.set(SkillSetting.EXPIRE_TEXT.node(), "You reappeared!");
    return node;
  }

  public void init()
  {
    super.init();
    this.applyText = SkillConfigManager.getRaw(this, SkillSetting.APPLY_TEXT, "You vanish in a cloud of smoke!");
    this.expireText = SkillConfigManager.getRaw(this, SkillSetting.EXPIRE_TEXT, "You reappeared");
  }

  public SkillResult use(Hero hero, String[] args)
  {
    broadcastExecuteText(hero);

    long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 20000, false);
    Player player = hero.getPlayer();
    player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 4);
    hero.addEffect(new InvisibleEffect(this, duration, this.applyText, this.expireText));
    
    return SkillResult.NORMAL;
  }

  public String getDescription(Hero hero)
  {
    return getDescription();
  }
}