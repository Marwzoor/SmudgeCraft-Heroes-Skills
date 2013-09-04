package net.smudgecraft.heroeslib.companions.companiontypes;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Wolf;

public class SerializableCompanion implements Serializable
{
	private static final long serialVersionUID = 7917029616232446072L;
	private String owner;
	private String id;
	private double damage;
	private double health;
	private double maxHealth;
	private String name;
	private String entityType;
	private int x;
	private int y;
	private int z;
	private String worldName;
	private byte dyeData;
	
	public SerializableCompanion(Companion cmp)
	{
		owner = cmp.getOwnerName();
		id = cmp.getId();
		damage = cmp.getDamage();
		health = cmp.getHealth();
		maxHealth = cmp.getMaxHealth();
		name = cmp.getLivingEntity().getCustomName();
		entityType = cmp.getLivingEntity().getType().toString();
		x = cmp.getLocation().getBlockX();
		y = cmp.getLocation().getBlockY();
		z = cmp.getLocation().getBlockZ();
		worldName = cmp.getLocation().getWorld().getName();
		
		if(cmp.getLivingEntity() instanceof Wolf)
		{
			this.dyeData = ((Wolf) cmp.getLivingEntity()).getCollarColor().getDyeData();
		}
	}
	
	public Companion unserialize()
	{
		return new Companion(entityType, owner, id, name, damage, health, maxHealth, new Location(Bukkit.getWorld(worldName),x,y,z), DyeColor.getByDyeData(dyeData));
	}
}
