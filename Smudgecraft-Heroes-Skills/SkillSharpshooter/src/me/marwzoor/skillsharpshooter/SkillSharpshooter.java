package me.marwzoor.skillsharpshooter;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

import net.smudgecraft.heroeslib.commoneffects.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SkillSharpshooter extends ActiveSkill
{
  public static Heroes plugin;
  public static SkillSharpshooter skill;

  public SkillSharpshooter(Heroes instance)
  {
    super(instance, "Sharpshooter");
    skill = this;
    plugin = instance;
    setDescription("The next arrow you fire deals $1% more damage.");
    setUsage("/skill sharpshooter");
    setArgumentRange(0, 0);
    setIdentifiers(new String[] { "skill sharpshooter" });
    setTypes(new SkillType[] { SkillType.BUFF });

    Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
  }

  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection node = super.getDefaultConfig();
    node.set(SkillSetting.DURATION.node(), Integer.valueOf(60000));
    node.set("percentage", Integer.valueOf(20));
    node.set("percentage-increase", Integer.valueOf(1));
    return node;
  }

  public String getDescription(Hero hero)
  {
	  if(hero.hasAccessToSkill(skill))
	  {
		  int percentage = SkillConfigManager.getUseSetting(hero, skill, "percentage", Integer.valueOf(20), false);
		  percentage += SkillConfigManager.getUseSetting(hero, skill, "percentage-increase", Integer.valueOf(1), false) * hero.getSkillLevel(skill);
		  return super.getDescription().replace("$1", percentage + "");
	  }
	  else
	  {
		 return getDescription().replace("$1", "X"); 
	  }
  }

  public SkillResult use(Hero hero, String[] args)
  {
    int percentage = SkillConfigManager.getUseSetting(hero, skill, "percentage", Integer.valueOf(20).intValue(), false);
    percentage += SkillConfigManager.getUseSetting(hero, skill, "percentage-increase", Integer.valueOf(1).intValue(), false) * hero.getSkillLevel(skill);
    int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, Integer.valueOf(60000).intValue(), false);

    SharpshotEffect sEffect = new SharpshotEffect(skill, duration, percentage, hero.getPlayer());

    hero.addEffect(sEffect);

    Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "Sharpshooter" + ChatColor.GRAY + "! Your next arrow will deal an extra " + ChatColor.WHITE + percentage + ChatColor.GRAY + "%", new Object[] { new Object() });

    return SkillResult.NORMAL;
  }

  public class SharpshotEffect extends ImbueEffect
  {
    public int percentage;

    public SharpshotEffect(Skill skill, int duration, int percentage, Player player) {
      super(skill, "Sharpshot", duration, player);
      this.percentage = percentage;
    }

    public void applyToHero(Hero hero)
    {
      super.applyToHero(hero, this);
    }

    public void removeFromHero(Hero hero)
    {
      super.removeFromHero(hero);
      Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "Sharpshooter" + ChatColor.GRAY + "!", new Object[] { new Object() });
    }

    public int getPercentage()
    {
      return this.percentage;
    }
  }
  public class SkillHeroListener implements Listener {
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
      if ((event.getDamager() instanceof Arrow))
      {
        Arrow arrow = (Arrow)event.getDamager();

        if ((arrow.getShooter() instanceof Player))
        {
          Player player = (Player)arrow.getShooter();

          Hero hero = SkillSharpshooter.plugin.getCharacterManager().getHero(player);

          if (hero.hasEffect("Sharpshot"))
          {
            SkillSharpshooter.SharpshotEffect sEffect = (SkillSharpshooter.SharpshotEffect)hero.getEffect("Sharpshot");

            double percentage = sEffect.getPercentage();

            percentage /= 100.0D;

            percentage += 1.0D;

            double damage = event.getDamage() * percentage;

            event.setDamage((int)damage);

            hero.removeEffect(sEffect);
          }
        }
      }
    }
  }
}