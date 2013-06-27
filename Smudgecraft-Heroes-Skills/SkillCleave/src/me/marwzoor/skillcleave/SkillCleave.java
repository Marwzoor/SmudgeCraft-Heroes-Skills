package me.marwzoor.skillcleave;

import java.util.List;

import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.party.HeroParty;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.util.Util;

public class SkillCleave extends ActiveSkill
{
	public SkillCleave(Heroes plugin)
	  {
	    super(plugin, "Cleave");
	    setUsage("/skill cleave");
	    setArgumentRange(0, 0);
	    setIdentifiers(new String[] { "skill cleave" });
	    setTypes(new SkillType[] { SkillType.DAMAGING, SkillType.PHYSICAL });

	    Bukkit.getServer().getPluginManager().registerEvents(new SkillListener(this), plugin);
	  }

	  public ConfigurationSection getDefaultConfig()
	  {
	    ConfigurationSection node = super.getDefaultConfig();
	    node.set("MaxTargets", Integer.valueOf(3));
	    node.set(SkillSetting.DURATION.node(), Integer.valueOf(5000));
	    node.set("BaseDamage", Integer.valueOf(3));
	    node.set("LevelMultiplier", Double.valueOf(0.5D));
	    node.set(SkillSetting.RADIUS.node(), Integer.valueOf(5));
	    return node;
	  }

	  public String getDescription(Hero hero)
	  {
	    int MaxTargets = SkillConfigManager.getSetting(hero.getHeroClass(), this, "maxtargets", 10);
	    int radius = SkillConfigManager.getSetting(hero.getHeroClass(), this, SkillSetting.RADIUS.node(), 3);
	    int bDmg = SkillConfigManager.getUseSetting(hero, this, "nasedamage", 3, false);
	    float bMulti = (float)SkillConfigManager.getUseSetting(hero, this, "levelmultiplier", 0.5D, false);
	    int newDmg = (int)(bMulti <= 0.0F ? bDmg : bDmg + bMulti * hero.getLevel());

	    StringBuilder description = new StringBuilder(String.format("Attack up to %s enemies within a %s block radius in front of you for %s damage.", new Object[] { Integer.valueOf(MaxTargets), Integer.valueOf(radius), Integer.valueOf(newDmg) }));
	    return description.toString();
	  }

	  public SkillResult use(Hero hero, String[] args)
	  {
	    int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 5000, false);

	    CleaveBuff cb = new CleaveBuff(this, duration);
	    hero.addEffect(cb);
	    return SkillResult.NORMAL;
	  }

	  private int damageAround(Player player, Entity exception, Skill skill, int newDmg)
	  {
	    Hero hero = this.plugin.getCharacterManager().getHero(player);
	    int MaxTargets = SkillConfigManager.getUseSetting(hero, this, "maxtargets", 3, false) - 1;
	    int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 5, false);

	    int Hits = 0;
	    List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
	    HeroParty hParty = hero.getParty();
	    if (hParty != null)
	      for (Entity entity : nearby) {
	        if (Hits >= MaxTargets) break;
	        if (entity.equals(exception)) {
	          continue;
	        }
	        if (((entity instanceof Player)) && (hParty.isPartyMember((Player)entity))) {
	          continue;
	        }
	        if ((((entity instanceof Monster)) || ((entity instanceof ComplexLivingEntity)) || ((entity instanceof Player))) && (isInFront(player, entity))) {
	          damageEntity((LivingEntity)entity, player, newDmg, DamageCause.ENTITY_ATTACK);
	          Hits++;
	        }
	      }
	    else {
	      for (Entity entity : nearby) {
	        if (Hits >= MaxTargets) break;
	        if (entity.equals(exception)) {
	          continue;
	        }
	        if ((((entity instanceof Monster)) || ((entity instanceof ComplexLivingEntity)) || ((entity instanceof Player))) && (isInFront(player, entity))) {
	          damageEntity((LivingEntity)entity, player, newDmg, DamageCause.ENTITY_ATTACK);
	          Hits++;
	        }
	      }
	    }
	    return Hits;
	  }

	  private boolean isInFront(Player player, Entity target)
	  {
	    if (!target.getWorld().equals(player.getWorld())) {
	      return false;
	    }
	    Location pLoc = player.getLocation();
	    Location tLoc = target.getLocation();
	    Vector u = player.getLocation().getDirection().normalize();
	    Vector v = new Vector(tLoc.getX() - pLoc.getX(), 0.0D, tLoc.getZ() - pLoc.getZ());
	    double magU = Math.sqrt(Math.pow(u.getX(), 2.0D) + Math.pow(u.getZ(), 2.0D));
	    double magV = Math.sqrt(Math.pow(v.getX(), 2.0D) + Math.pow(v.getZ(), 2.0D));
	    double angle = Math.acos(u.dot(v) / (magU * magV));
	    angle = angle * 180.0D / 3.141592653589793D;
	    return angle < 90.0D;
	  }

	  public class CleaveBuff extends ImbueEffect
	  {
	    private long duration = 0L;

	    public CleaveBuff(Skill skill, int duration) {
	      super(skill, "CleaveBuff", duration);
	      this.duration = duration;
	    }

	    public void applyToHero(Hero hero)
	    {
	      super.applyToHero(hero, this);
	      Messaging.send(hero.getPlayer(), "Cleaving available on your next attack for $1 seconds", new Object[] { Long.valueOf(this.duration / 1000L) });
	    }

	    public void removeFromHero(Hero hero)
	    {
	      super.removeFromHero(hero);
	      Messaging.send(hero.getPlayer(), "Can no longer cleave", new Object[0]);
	    }
	  }

	  public class SkillListener implements Listener {
	    private final Skill skill;

	    public SkillListener(Skill skill) {
	      this.skill = skill;
	    }
	    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	    public void onEntityDamage(EntityDamageEvent event) {
	      if (!(event instanceof EntityDamageByEntityEvent)) {
	        return;
	      }

	      Entity initTarg = event.getEntity();
	      if ((!(initTarg instanceof LivingEntity)) && (!(initTarg instanceof Player))) {
	        return;
	      }

	      EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent)event;
	      if (!(subEvent.getDamager() instanceof Player)) {
	        return;
	      }

	      Player player = (Player)subEvent.getDamager();
	      Hero hero = SkillCleave.this.plugin.getCharacterManager().getHero(player);
	      if (hero.hasEffect("CleaveBuff"))
	      {
	        ItemStack item = player.getItemInHand();
	        if ((!Util.swords.contains(item.getType().name())) && (!Util.axes.contains(item.getType().name()))) {
	          return;
	        }
	        SkillCleave.CleaveBuff cb = (SkillCleave.CleaveBuff)hero.getEffect("CleaveBuff");

	        hero.removeEffect(cb);
	        int bDmg = SkillConfigManager.getUseSetting(hero, this.skill, "basedamage", 3, false);
	        float bMulti = (float)SkillConfigManager.getUseSetting(hero, this.skill, "levelmultiplier", 0.5D, false);
	        int newDmg = (int)(bMulti <= 0.0F ? bDmg : bDmg + bMulti * hero.getLevel());
	        event.setDamage(newDmg);
	        int hitAmount = SkillCleave.this.damageAround(player, initTarg, this.skill, newDmg) + 1;

	        SkillCleave.this.broadcast(player.getLocation(), "$1 hit $2 enemies with" + ChatColor.WHITE + " Cleave" + ChatColor.GRAY + "!", new Object[] { player.getDisplayName(), Integer.valueOf(hitAmount) });
	      }
	    }
	  }
}
