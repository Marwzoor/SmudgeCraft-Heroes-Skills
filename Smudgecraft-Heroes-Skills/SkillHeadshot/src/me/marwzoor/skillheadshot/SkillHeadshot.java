package me.marwzoor.skillheadshot;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SkillHeadshot extends PassiveSkill
{
  public static Heroes plugin;
  public static SkillHeadshot skill;

  public SkillHeadshot(Heroes instance)
  {
    super(instance, "Headshot");
    plugin = instance;
    skill = this;
    setDescription("When you shoot your target in the head, you deal $1% more damage. (PvP only)");
    setTypes(new SkillType[] { SkillType.BUFF, SkillType.UNBINDABLE });
    Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
  }

  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection node = super.getDefaultConfig();
    node.set("percentage", Integer.valueOf(20));
    node.set("percentage-increase", Integer.valueOf(1));
    return node;
  }

  public String getDescription(Hero hero)
  {
    String desc = super.getDescription();
    int percentage = SkillConfigManager.getUseSetting(hero, skill, "percentage", Integer.valueOf(20).intValue(), false);
    percentage += SkillConfigManager.getUseSetting(hero, skill, "percentage-increase", Integer.valueOf(1).intValue(), false) * hero.getSkillLevel(skill);
    desc = desc.replace("$1", percentage + "");
    return desc;
  }
  public class SkillHeroListener implements Listener {
    public SkillHeroListener() {
    }
    @EventHandler(ignoreCancelled=true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
      if (((event.getDamager() instanceof Arrow)) && ((event.getEntity() instanceof Player)))
      {
        Arrow arrow = (Arrow)event.getDamager();
        Player target = (Player)event.getEntity();

        if ((arrow.getShooter() instanceof Player))
        {
          Player shooter = (Player)arrow.getShooter();
          Hero hero = SkillHeadshot.plugin.getCharacterManager().getHero(shooter);

          if (hero.hasAccessToSkill(SkillHeadshot.skill))
          {
            if (arrow.getLocation().getY() > target.getLocation().getY() + 1.35D)
            {
              double percentage = SkillConfigManager.getUseSetting(hero, SkillHeadshot.skill, "percentage", Double.valueOf(20.0D).doubleValue(), false);
              percentage += SkillConfigManager.getUseSetting(hero, SkillHeadshot.skill, "percentage-increase", Double.valueOf(1.0D).doubleValue(), false) * hero.getSkillLevel(SkillHeadshot.skill);
              percentage /= 100.0D;
              percentage += 1.0D;
              double damage = event.getDamage();
              damage *= percentage;
              event.setDamage((int)damage);
              Messaging.send(shooter, "You headshotted " + ChatColor.WHITE + target.getName() + ChatColor.GRAY + "!", new Object[] { new Object() });
            }
          }
        }
      }
    }
  }
}