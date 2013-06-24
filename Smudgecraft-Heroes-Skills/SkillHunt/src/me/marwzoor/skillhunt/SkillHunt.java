package me.marwzoor.skillhunt;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SkillHunt extends ActiveSkill
{
  private String huntText;
  
  public SkillHunt(Heroes plugin)
  {
    super(plugin, "Hunt");
    setDescription("Teleports you someplace within $1 blocks of the target.");
    setUsage("/skill hunt [player]");
    setArgumentRange(1, 1);
    setIdentifiers(new String[] { "skill hunt" });
    
    setTypes(new SkillType[] { SkillType.TELEPORT, SkillType.KNOWLEDGE, SkillType.SILENCABLE });
  }

  public String getDescription(Hero hero)
  {
    int radius = (int)(SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 200, false) - SkillConfigManager.getUseSetting(hero, this, "radius-decrease", 0.0D, false) * hero.getSkillLevel(this));

    radius = radius > 0 ? radius : 0;
    String description = getDescription().replace("$1", radius + "");

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
    node.set(SkillSetting.RADIUS.node(), Integer.valueOf(200));
    node.set("radius-decrease", Integer.valueOf(0));
    node.set("hunter-nearby-text", "Someone is hunting players in your area");
    ArrayList<String> tempList = new ArrayList<String>();
    tempList.add("someworld");
    tempList.add("someworld_nether");
    node.set("disabled-worlds", tempList);
    return node;
  }

  public void init()
  {
    super.init();
    this.huntText = SkillConfigManager.getUseSetting(null, this, "hunter-nearby-text", "Someone is hunting players in your area");
  }

  public SkillResult use(Hero hero, String[] args)
  {
    Player player = hero.getPlayer();

    Player target = this.plugin.getServer().getPlayer(args[0]);
    List<String> disabledWorlds = SkillConfigManager.getUseSetting(hero, this, "disabled-worlds", new ArrayList<String>());
    ArrayList<String> tempList = new ArrayList<String>();
    for (String s : disabledWorlds) {
      tempList.add(s.toLowerCase());
    }
    if ((target == null) || (tempList.contains(target.getWorld().getName().toLowerCase()))) {
      return SkillResult.INVALID_TARGET;
    }

    for(Player p : player.getWorld().getPlayers())
    {
    	if(player.getLocation().distance(p.getLocation())<=35)
    	{
    		FPlayer fplayer = FPlayers.i.get(player);
    		FPlayer tfplayer = FPlayers.i.get(p);
    		if(fplayer.getFaction().getRelationTo(tfplayer.getFaction()).equals(Relation.ENEMY))
    		{
    		player.sendMessage(ChatColor.RED + "You can't hunt while there are enemies nearby you.");
    		return SkillResult.CANCELLED;
    		}
    	}
    }
    
    Location location = target.getLocation();
    int radius = (int)(SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 200, false) - SkillConfigManager.getUseSetting(hero, this, "radius-decrease", 0.0D, false) * hero.getSkillLevel(this));

    radius = radius > 0 ? radius : 0;
    int xRadius = (int)(Math.random() * radius);
    if (Math.random() > 0.5D) {
      xRadius *= -1;
    }
    int x = location.getBlockX() + xRadius;

    int zRadius = (int)Math.sqrt(radius * radius - xRadius * xRadius);
    if (Math.random() > 0.5D) {
      zRadius *= -1;
    }
    int z = location.getBlockZ() + zRadius;
    hero.getPlayer().teleport(location.getWorld().getHighestBlockAt(x, z).getLocation());

    List<Entity> nearbyEntities = player.getNearbyEntities(200.0D, 128.0D, 200.0D);
    for (Entity e : nearbyEntities) {
      if ((e instanceof Player)) {
        Messaging.send((Player)e, this.huntText, new Object[0]);
      }
    }
    return SkillResult.NORMAL;
  }
}