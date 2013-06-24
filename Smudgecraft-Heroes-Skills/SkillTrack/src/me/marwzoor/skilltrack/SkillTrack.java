package me.marwzoor.skilltrack;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillTrack extends ActiveSkill
{
  private static final Random random = new Random();

  public SkillTrack(Heroes plugin) {
    super(plugin, "Track");
    setDescription("Gives you tons of info on a player.");
    setUsage("/skill track [player]");
    setArgumentRange(1, 1);
    setIdentifiers(new String[] { "skill track" });

    setTypes(new SkillType[] { SkillType.EARTH });
  }

  public String getDescription(Hero hero)
  {
    String description = getDescription();

    int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;

    if (cooldown > 0) {
      description = description + " CD:" + cooldown + "s";
    }

    int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 10, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this);

    if (mana > 0) {
      description = description + " M:" + mana;
    }

    int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this);

    if (healthCost > 0) {
      description = description + " HP:" + healthCost;
    }

    int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA.node(), 0, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this);

    if (staminaCost > 0) {
      description = description + " FP:" + staminaCost;
    }

    int delay = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DELAY.node(), 0, false) / 1000;
    if (delay > 0) {
      description = description + " W:" + delay + "s";
    }

    int exp = SkillConfigManager.getUseSetting(hero, this, SkillSetting.EXP.node(), 0, false);
    if (exp > 0) {
      description = description + " XP:" + exp;
    }
    return description;
  }

  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection node = super.getDefaultConfig();
    node.set("randomness", Integer.valueOf(50));
    return node;
  }

  public SkillResult use(Hero hero, String[] args)
  {
    Player player = hero.getPlayer();

    Player target = this.plugin.getServer().getPlayer(args[0]);
    if (target == null) {
      return SkillResult.INVALID_TARGET;
    }

    Location location = target.getLocation();
    int randomness = SkillConfigManager.getUseSetting(hero, this, "randomness", 50, false);
    int x = location.getBlockX() + random.nextInt(randomness);
    int y = location.getBlockY() + random.nextInt(randomness / 10);
    y = y < 1 ? 1 : y;
    int z = location.getBlockZ() + random.nextInt(randomness);
    Hero tHero = this.plugin.getCharacterManager().getHero(target);
    Messaging.send(player, "$1 is a level $2 $3 with $4 health!", new Object[] { tHero.getPlayer().getDisplayName(), Integer.valueOf(tHero.getTieredLevel(tHero.getHeroClass())), tHero.getHeroClass().getName(), Integer.valueOf(tHero.getPlayer().getHealth()) });
    Messaging.send(player, "$5 pos: $1: $2,$3,$4", new Object[] { location.getWorld().getName(), Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z), tHero.getPlayer().getDisplayName() });
    Location hLoc = hero.getPlayer().getLocation();
    int hX = hLoc.getBlockX();
    int hY = hLoc.getBlockY();
    int hZ = hLoc.getBlockZ();

    Messaging.send(player, "Your current pos: $1: $2, $3, $4", new Object[] { hLoc.getWorld().getName(), Integer.valueOf(hX), Integer.valueOf(hY), Integer.valueOf(hZ) });
    if (damageCheck(player, tHero.getPlayer()))
      Messaging.send(player, "You $1 hurt $2", new Object[] { "can", tHero.getPlayer().getDisplayName() });
    else {
      Messaging.send(player, "You $1 hurt $2", new Object[] { "cannot", tHero.getPlayer().getDisplayName() });
    }
    player.setCompassTarget(location);
    broadcastExecuteText(hero);
    return SkillResult.NORMAL;
  }
}