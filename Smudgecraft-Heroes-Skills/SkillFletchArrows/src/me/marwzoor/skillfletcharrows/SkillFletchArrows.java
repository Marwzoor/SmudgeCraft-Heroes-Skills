package me.marwzoor.skillfletcharrows;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SkillFletchArrows extends ActiveSkill
{
  public SkillFletchArrows(Heroes plugin)
  {
    super(plugin, "FletchArrows");
    setDescription("You fletch $1 arrows.");
    setUsage("/skill fletcharrow");
    setArgumentRange(0, 0);
    setIdentifiers(new String[] { "skill fletcharrows", "skill farrows" });
    setTypes(new SkillType[] { SkillType.ITEM, SkillType.SUMMON, SkillType.SILENCABLE });
  }

  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection node = super.getDefaultConfig();
    node.set(SkillSetting.AMOUNT.node(), Integer.valueOf(10));
    node.set("amount-increase", Integer.valueOf(1));
    return node;
  }

  public SkillResult use(Hero hero, String[] args)
  {
	int amount = SkillConfigManager.getUseSetting(hero, this, "amount", 2, false);
	amount += SkillConfigManager.getUseSetting(hero, this, "amount-increase", 1, false) * hero.getSkillLevel(this);
    Player player = hero.getPlayer();
    World world = player.getWorld();
    ItemStack dropItem = new ItemStack(Material.ARROW, amount);
    world.dropItem(player.getLocation(), dropItem);
    Messaging.send(player, "You fletch yourself " + ChatColor.WHITE + amount + " Arrows");
    return SkillResult.NORMAL;
  }

  public String getDescription(Hero hero)
  {
	if(hero.hasAccessToSkill(this))
	{
    int amount = SkillConfigManager.getUseSetting(hero, this, SkillSetting.AMOUNT, 2, false);
    amount += SkillConfigManager.getUseSetting(hero, this, "amount-increase", 1, false) * hero.getSkillLevel(this);
    return getDescription().replace("$1", amount + "");
	}
	else
	{
		return getDescription().replace("$1", "X amount of");
	}
}
}