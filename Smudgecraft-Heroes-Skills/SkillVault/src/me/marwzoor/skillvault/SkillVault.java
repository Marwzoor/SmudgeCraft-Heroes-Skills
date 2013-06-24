package me.marwzoor.skillvault;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillVault extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillVault skill;
	
	public SkillVault(Heroes instance)
	{
		super(instance, "Vault");
		plugin=instance;
		skill=this;
		setDescription("You vault backwards through the air.");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill vault" });
		setTypes(new SkillType[] { SkillType.DAMAGING });
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		return desc;
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		Player player = hero.getPlayer();
		
		BlockFace face = yawToFace(player.getLocation().getYaw());
		
		player.setVelocity(new Vector(face.getModX()*3*0.5, 1, face.getModZ()*3*0.5));
		
		skill.broadcast(player.getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " +  player.getDisplayName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "Vault" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
    public static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    public static BlockFace yawToFace(float yaw) 
    {
            return radial[Math.round(yaw / 45f) & 0x7];
    }
}
