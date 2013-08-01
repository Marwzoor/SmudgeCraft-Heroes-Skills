package me.marwzoor.skillrapidfire;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillRapidFire extends ActiveSkill
{
	private final Map<Hero, Integer> shootingPlayers = new HashMap<Hero, Integer>();

	  public SkillRapidFire(Heroes plugin) {
	    super(plugin, "RapidFire");
	    setDescription("Shoots between $1-$2 arrows at a rate of $3-$4 per second. CD: $5s M: $6");
	    setUsage("/skill rapidfire");
	    setArgumentRange(0, 0);
	    setIdentifiers(new String[] { "skill rapidfire" });

	    setTypes(new SkillType[] { SkillType.PHYSICAL, SkillType.DAMAGING });
	  }

	  public String getDescription(Hero hero)
	  {
	    int maxArrows = SkillConfigManager.getUseSetting(hero, this, "max-arrows", 30, false) + SkillConfigManager.getUseSetting(hero, this, "arrows-per-level", 0, false) * hero.getSkillLevel(this);

	    int minArrows = SkillConfigManager.getUseSetting(hero, this, "min-arrows", 15, false) + SkillConfigManager.getUseSetting(hero, this, "arrows-per-level", 0, false) * hero.getSkillLevel(this);

	    int maxRate = SkillConfigManager.getUseSetting(hero, this, "max-rate", 20, false);
	    int minRate = SkillConfigManager.getUseSetting(hero, this, "min-rate", 2, false);
	    String description = getDescription().replace("$1", maxArrows + "").replace("$2", minArrows + "").replace("$3", maxRate + "").replace("$4", minRate + "");

	    int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE, 0, false) * hero.getSkillLevel(this)) / 1000;

	    if (cooldown > 0) {
	      description = description + " CD:" + cooldown + "s";
	    }

	    int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 10, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE, 0, false) * hero.getSkillLevel(this);

	    if (mana > 0) {
	      description = description + " M:" + mana;
	    }

	    int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this);

	    if (healthCost > 0) {
	      description = description + " HP:" + healthCost;
	    }

	    int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA, 0, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE, 0, false) * hero.getSkillLevel(this);

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
	    node.set("max-arrows", Integer.valueOf(30));
	    node.set("min-arrows", Integer.valueOf(15));
	    node.set("min-rate", Integer.valueOf(2));
	    node.set("max-rate", Integer.valueOf(20));
	    node.set("arrows-per-level", Double.valueOf(0.0D));
	    node.set("damage", Integer.valueOf(30));
	    node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(1000));
	    node.set(SkillSetting.MANA.node(), Integer.valueOf(10));
	    return node;
	  }

	  public SkillResult use(Hero hero, String[] args)
	  {
	    if (this.shootingPlayers.containsKey(hero)) {
	      this.plugin.getServer().getScheduler().cancelTask(((Integer)this.shootingPlayers.get(hero)).intValue());
	      this.shootingPlayers.remove(hero);
	      long cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 1000, false);
	      hero.setCooldown("Arrowstorm", System.currentTimeMillis() + cooldown);
	      Messaging.send(hero.getPlayer(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] $1 stopped shooting arrows prematurely.", new Object[] { hero.getPlayer().getDisplayName() });
	      return SkillResult.INVALID_TARGET_NO_MSG;
	    }
	    Player player = hero.getPlayer();
	    PlayerInventory inv = player.getInventory();
	    int minArrows = SkillConfigManager.getUseSetting(hero, this, "min-arrows", 15, false) + SkillConfigManager.getUseSetting(hero, this, "arrows-per-level", 0, false) * hero.getSkillLevel(this);

	    if (minArrows <= 0)
	      minArrows = 1;
	    else if (minArrows > 64) {
	      minArrows = 64;
	    }
	    int maxArrows = SkillConfigManager.getUseSetting(hero, this, "max-arrows", 30, false) + SkillConfigManager.getUseSetting(hero, this, "arrows-per-level", 0, false) * hero.getSkillLevel(this);

	    if (maxArrows < minArrows)
	      maxArrows = minArrows;
	    else if (maxArrows > 64) {
	      maxArrows = 64;
	    }
	    int minRate = SkillConfigManager.getUseSetting(hero, this, "min-rate", 2, false);
	    if (((minRate != 1 ? 1 : 0) | (minRate != 2 ? 1 : 0) | (minRate != 4 ? 1 : 0) | (minRate != 5 ? 1 : 0) | (minRate != 10 ? 1 : 0) | (minRate != 20 ? 1 : 0)) != 0) {
	      minRate = 2;
	    }
	    int maxRate = SkillConfigManager.getUseSetting(hero, this, "max-rate", 10, false);
	    if (((maxRate != 1 ? 1 : 0) | (maxRate != 2 ? 1 : 0) | (maxRate != 4 ? 1 : 0) | (maxRate != 5 ? 1 : 0) | (maxRate != 10 ? 1 : 0) | (maxRate != 20 ? 1 : 0)) != 0) {
	      maxRate = 10;
	    }
	    if (maxRate < minRate) {
	      maxRate = minRate;
	    }
	    int randRate = maxRate - minRate;
	    int randArrows = maxArrows - minArrows;

	    Map<Integer, ? extends ItemStack> arrowSlots = inv.all(Material.ARROW);

	    int numArrows = 0;
	    for (Map.Entry<Integer, ? extends ItemStack> entry : arrowSlots.entrySet()) {
	      numArrows += ((ItemStack)entry.getValue()).getAmount();
	    }

	    int preTotalArrows = (int)Math.rint(Math.random() * randArrows) + minArrows;
	    if (numArrows > preTotalArrows) {
	      numArrows = preTotalArrows;
	    }
	    if (numArrows < minArrows) {
	      numArrows = 0;
	    }
	    if (numArrows == 0) {
	      return new SkillResult(SkillResult.ResultType.MISSING_REAGENT, true, new Object[] { Integer.valueOf(minArrows), "Arrows" });
	    }

	    int removedArrows = 0;
	    for (Map.Entry<Integer, ? extends ItemStack> entry : arrowSlots.entrySet()) {
	      int amount = ((ItemStack)entry.getValue()).getAmount();
	      int remove = amount;
	      if (removedArrows + remove > numArrows) {
	        remove = numArrows - removedArrows;
	      }
	      removedArrows += remove;
	      if (remove == amount)
	        inv.clear(((Integer)entry.getKey()).intValue());
	      else {
	        inv.getItem(((Integer)entry.getKey()).intValue()).setAmount(amount - remove);
	      }

	      if (removedArrows >= numArrows) {
	        break;
	      }
	    }

	    long sleepTime = (long) Math.rint(Math.random() * randRate) + minRate;
	    Skill skill = this;
		skill.broadcast(player.getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " +  player.getDisplayName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "RapidFire" + ChatColor.GRAY + "!");
	    float rate = (float)(20L / sleepTime);
	    final Hero h = hero;
	    final Player p = player;
	    final Skill s = skill;
	    final int damage = SkillConfigManager.getUseSetting(hero, s, "damage", 30, false);
	    Messaging.send(player, "Casting $1 at a rate of $2 per second", new Object[] { Integer.valueOf(preTotalArrows), Float.valueOf(rate) });
	    this.shootingPlayers.put(hero, Integer.valueOf(this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable()
	    {
	      public void run() {
	        Arrow ar = p.launchProjectile(Arrow.class);
	        ar.setDamage(damage);
	      }
	    }
	    , 0L, sleepTime)));

	    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
	    {
	      public void run() {
	        try {
	          SkillRapidFire.this.plugin.getServer().getScheduler().cancelTask(((Integer)SkillRapidFire.this.shootingPlayers.get(h)).intValue());
	          SkillRapidFire.this.shootingPlayers.remove(h);
	          h.setCooldown("RapidFire", SkillConfigManager.getUseSetting(h, s, SkillSetting.COOLDOWN.node(), 1000, false) + System.currentTimeMillis());
	        }
	        catch (Exception e)
	        {
	        }
	      }
	    }
	    , preTotalArrows * sleepTime);

	    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
	    {
	      public void run() {
	        h.setCooldown("RapidFire", System.currentTimeMillis());
	      }
	    }
	    , 1L);

	    return SkillResult.NORMAL;
	  }
}
