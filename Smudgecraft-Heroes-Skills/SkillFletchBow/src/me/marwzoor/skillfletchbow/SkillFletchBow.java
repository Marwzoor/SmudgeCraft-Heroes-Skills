package me.marwzoor.skillfletchbow;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SkillFletchBow extends ActiveSkill
{
  public static Heroes plugin;
  public static SkillFletchBow skill;

	
  public SkillFletchBow(Heroes instance)
  {
    super(plugin, "FletchBow");
    plugin=instance;
    skill=this;
    setDescription("You fletch yourself a bow$enchant");
    setUsage("/skill fletchbow");
    setArgumentRange(0, 0);
    setIdentifiers(new String[] { "skill fletchbow", "skill fbow" });
    setTypes(new SkillType[] { SkillType.ITEM, SkillType.SUMMON, SkillType.SILENCABLE });
  }

  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection node = super.getDefaultConfig();
    node.set(SkillSetting.AMOUNT.node(), Integer.valueOf(1));
    node.set("enchantment", "noenchant");
    node.set("enchantment-level", -1);
    node.set("namelist", Integer.valueOf(1));
    return node;
  }

  public SkillResult use(Hero hero, String[] args)
  {
	String[] names1 = new String[]{"Athena's bow"};
	String[] lores1 = new String[]{"The very bow from the godess athena"};
	
	String[] names2 = new String[]{"Athena's bow"};
	String[] lores2 = new String[]{"The very bow from the godess athena"};
	
	int amount = SkillConfigManager.getUseSetting(hero, this, "amount", 1, false);
	String enchname = SkillConfigManager.getUseSetting(hero, this, "enchantment", "noenchant");
	int enchlevel = SkillConfigManager.getUseSetting(hero, this, "enchantment-level", -1, false);
    Player player = hero.getPlayer();
    World world = player.getWorld();
    ItemStack dropItem = new ItemStack(Material.BOW, amount);
    if(enchname!="noenchant" && enchlevel!=-1)
    {
    Enchantment ench = Enchantment.getByName(enchname);
    
    	if(ench!=null)
    	{
    		ItemMeta im = dropItem.getItemMeta();
    		im.addEnchant(ench, enchlevel, true);
    		dropItem.setItemMeta(im);
    	}
    }
    ItemMeta im = dropItem.getItemMeta();
    
    if(SkillConfigManager.getUseSetting(hero, skill, "namelist", Integer.valueOf(1), false)==1)
    {
    	int random = new Random().nextInt(names1.length);
    	im.setDisplayName(getBowName(names1[random]));
    	im.setLore(getBowLore(lores1[random]));
    	dropItem.setItemMeta(im);
    	world.dropItem(player.getLocation(), dropItem);
    	Messaging.send(player, "You fletch yourself a" + ChatColor.WHITE + " Bow " + ChatColor.GRAY + "by the name " + getBowName(names1[random]) + ChatColor.GRAY + "!");
    }
    else if(SkillConfigManager.getUseSetting(hero, skill, "namelist", Integer.valueOf(1), false)==2)
    {
        int random = new Random().nextInt(names2.length);
        im.setDisplayName(getBowName(names2[random]));
        im.setLore(getBowLore(lores2[random]));
        dropItem.setItemMeta(im);
        world.dropItem(player.getLocation(), dropItem);
        Messaging.send(player, "You fletch yourself a" + ChatColor.WHITE + " Bow " + ChatColor.GRAY + "by the name " + getBowName(names2[random]) + ChatColor.GRAY + "!");
    }
    else
    {
    	return SkillResult.FAIL;
    }
    return SkillResult.NORMAL;
  }

  public String getDescription(Hero hero)
  {
	  	String desc = getDescription();
	  	String enchname = SkillConfigManager.getUseSetting(hero, this, "enchantment", "noenchant");
	  	int enchlevel = SkillConfigManager.getUseSetting(hero, this, "enchantment-level", -1, false);
	  	if(enchname!="noenchant" && enchlevel!=-1)
	  	{
	  		desc = desc.replace("$enchant", " with the " + enchname + " enchant at level " + enchlevel + ".");
	  	}
	  	else
	  	{
	  		desc = desc.replace("$enchant", ".");
	  	}
   		return desc;
  }
  
  public String getBowName(String name)
  {
	  Random rand = new Random();
	  String[] cs = new String[]{"§a", "§b", "§c", "§d", "§e"};
	  
	  return "§r" + cs[rand.nextInt(cs.length)] + name;
  }
  
  public String getBowName(String name, String format)
  {
	  return format + name;
  }
  
  public List<String> getBowLore(String lore)
  {
	  return Arrays.asList("§r§8§o" + lore);
  }
  
  public List<String> getBowLore(String lore, String format)
  {
	  return Arrays.asList(format + lore);
  }
}