package me.marwzoor.skillteleport;

import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillTeleport extends ActiveSkill
{
	public static final HashSet<Byte> TRANSPARENT_MATERIALS = new HashSet<Byte>();
	
	public SkillTeleport(Heroes instance)
	{
		super(instance, "Teleport");
		setDescription("You teleport to the targeted location. M: %1 CD: %2 R: %3");
		setUsage("/skill teleport");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill teleport" });
		setTypes(new SkillType[] { SkillType.TELEPORT, SkillType.MOVEMENT});
		
		addTransparents();
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000;
			int range = SkillConfigManager.getUseSetting(hero, this, "range", 10, false);
			
			return super.getDescription().replace("%1", mana + "").replace("%2", cooldown + "s").replace("%3", range + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "Xs").replace("%3", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.MANA.node(), 0);
		node.set(SkillSetting.COOLDOWN.node(), 0);
		node.set("range", 10);
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int range = SkillConfigManager.getUseSetting(hero, this, "range", 10, false);
		
		Block block = hero.getPlayer().getTargetBlock(TRANSPARENT_MATERIALS, range);
		
		if(block == null || block.getType().equals(Material.AIR))
		{
			if(hero.getPlayer().getTargetBlock(TRANSPARENT_MATERIALS, range+10) == null || hero.getPlayer().getTargetBlock(TRANSPARENT_MATERIALS, range+10).getType().equals(Material.AIR))
			{
				Messaging.send(hero.getPlayer(), "You have to target a block!");
			}
			else
			{
				Messaging.send(hero.getPlayer(), "That block is out of range!");
			}
			return SkillResult.INVALID_TARGET_NO_MSG;
		}
		 
		Location bloc = block.getLocation();
		
		bloc.setPitch(hero.getPlayer().getLocation().getPitch());
		bloc.setYaw(hero.getPlayer().getLocation().getYaw());
		
		BlockFace face = null;
		
		List<Block> blocks = hero.getPlayer().getLastTwoTargetBlocks(null, 10);
		if (blocks.size() > 1) {
		  face = blocks.get(1).getFace(blocks.get(0));
		}
		
		if(isTeleportable(bloc.clone().add(0, 1, 0)))
		{
			if(face==null || !face.equals(BlockFace.DOWN))
			{
				this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.WHITE + " Teleported" + ChatColor.GRAY + "!");
				Location loc = new Location(bloc.getWorld(), bloc.getBlockX()+0.5, bloc.getBlockY()+1, bloc.getBlockZ()+0.5);
				loc.setPitch(bloc.getPitch());
				loc.setYaw(bloc.getYaw());
				hero.getPlayer().teleport(loc);
				return SkillResult.NORMAL;
			}
			else
			{
				if(isTeleportable(bloc.clone().add(0, -2, 0)))
				{
					this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.WHITE + " Teleported" + ChatColor.GRAY + "!");
					Location loc = new Location(bloc.getWorld(), bloc.getBlockX()+0.5, bloc.getBlockY()-2, bloc.getBlockZ()+0.5);
					loc.setPitch(bloc.getPitch());
					loc.setYaw(bloc.getYaw());
					hero.getPlayer().teleport(loc);
					return SkillResult.NORMAL;
				}
			}
		}
		
		Vector v = hero.getPlayer().getLocation().getDirection();
		v.multiply(-1);
		
		for(int i=0; i<5; ++i)
		{
			if(face==null || !face.equals(BlockFace.DOWN) || i!=0)
			{
				if(isTeleportable(bloc.add(v)))
				{
					this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.WHITE + " Teleported" + ChatColor.GRAY + "!");
					Location loc = new Location(bloc.getWorld(), bloc.getBlockX()+0.5, bloc.getBlockY(), bloc.getBlockZ()+0.5);
					loc.setPitch(bloc.getPitch());
					loc.setYaw(bloc.getYaw());
					hero.getPlayer().teleport(loc);
					return SkillResult.NORMAL;
				}
			}
		}
		
		Messaging.send(hero.getPlayer(), "You can't teleport to that location!");
		return SkillResult.INVALID_TARGET_NO_MSG;
	}
	
	private boolean isTeleportable(Location loc)
	{
		if(!loc.getBlock().getType().isSolid() && !loc.clone().add(0, 1, 0).getBlock().getType().isSolid())
			return true;
		return false;
	}
	
	private void addTransparents()
	{
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.AIR.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.SAPLING.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.POWERED_RAIL.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.DETECTOR_RAIL.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.LONG_GRASS.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.DEAD_BUSH.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.YELLOW_FLOWER.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.RED_ROSE.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.BROWN_MUSHROOM.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.RED_MUSHROOM.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.TORCH.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.REDSTONE_WIRE.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.SEEDS.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.SIGN_POST.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.LADDER.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.RAILS.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.WALL_SIGN.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.LEVER.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.STONE_PLATE.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.WOOD_PLATE.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.REDSTONE_TORCH_OFF.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.REDSTONE_TORCH_ON.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.STONE_BUTTON.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.SNOW.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.SUGAR_CANE_BLOCK.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.DIODE_BLOCK_OFF.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.DIODE_BLOCK_ON.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.PUMPKIN_STEM.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.MELON_STEM.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.VINE.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.WATER_LILY.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.NETHER_WARTS.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.WATER.getId()));
	    TRANSPARENT_MATERIALS.add(Byte.valueOf((byte)Material.STATIONARY_WATER.getId()));
	}
}
