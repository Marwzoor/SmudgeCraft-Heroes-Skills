package net.smudgecraft.heroeslib.companions.companiontypes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.smudgecraft.heroeslib.HeroesLib;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class Companion 
{
	public static final double NODAMAGE = -1;
	private LivingEntity livingEntity;
	private double damage;
	private String owner;
	private String id;
	
	public Companion(String entity, String owner, String id, String name, double damage, double health, double maxHealth, Location loc, DyeColor dc)
	{
		EntityType entityType = EntityType.fromName(entity);
		
		if(entityType==null)
			return;
		
		Entity en = loc.getWorld().spawnEntity(loc, entityType);
		
		if(!(en instanceof LivingEntity))
		{
			en.remove();
			return;
		}
		
		this.livingEntity = (LivingEntity) en;
		this.livingEntity.setMaxHealth(maxHealth);
		this.livingEntity.setHealth(health);
		this.livingEntity.setCustomName(name);
		
		this.damage=damage;
		this.owner=owner;
		this.id=id;
		
		if(livingEntity instanceof Wolf)
		{
			Wolf wolf = (Wolf) this.livingEntity;
			wolf.setTamed(true);
			wolf.setOwner(Bukkit.getOfflinePlayer(owner));
			
			wolf.setCollarColor(dc);
		}
	}
	
	public boolean hasCustomDamage()
	{
		return damage != -1;
	}
	
	public void setDamage(double damage)
	{
		this.damage=damage;
	}
	
	public String getOwnerName()
	{
		return this.owner;
	}
	
	public Player getOwner()
	{
		return Bukkit.getPlayer(owner);
	}
	
	public boolean hasOnlineOwner()
	{
		if(Bukkit.getOfflinePlayer(owner).isOnline())
			return true;
		return false;
	}
	
	public double getDamage()
	{
		return damage;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public LivingEntity getLivingEntity()
	{
		return this.livingEntity;
	}
	
	public double getHealth()
	{
		return ((Damageable) this.livingEntity).getHealth();
	}
	
	public double getMaxHealth()
	{
		return ((Damageable) this.livingEntity).getMaxHealth();
	}
	
	public Location getLocation()
	{
		return this.livingEntity.getLocation();
	}
	
	public void setHealth(double health)
	{
		if(((Damageable) this.livingEntity).getMaxHealth()<health)
		{
			this.livingEntity.setHealth(((Damageable) this.livingEntity).getMaxHealth());
			return;
		}
			
		this.livingEntity.setHealth(health);	
	}
	
	public void setMaxHealth(double maxHealth)
	{
		livingEntity.setMaxHealth(maxHealth);
	}
	
	public void heal(double heal)
	{
		if(((Damageable) this.livingEntity).getHealth()+heal>=((Damageable) this.livingEntity).getMaxHealth())
		{
			this.livingEntity.setHealth(((Damageable) this.livingEntity).getMaxHealth());
			
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(livingEntity, heal, RegainReason.MAGIC_REGEN);
			
			Bukkit.getPluginManager().callEvent(event);
		}
		else
		{
			this.livingEntity.setHealth(((Damageable) this.livingEntity).getHealth()+heal);
			
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(livingEntity, heal, RegainReason.MAGIC_REGEN);
			
			Bukkit.getPluginManager().callEvent(event);
		}
	}
	
	public boolean save()
	{
		File folder = new File(HeroesLib.plugin.getDataFolder() + "/players/" + Character.toLowerCase(owner.charAt(0)) + "/" + owner + "/Companions");
		if(!folder.exists())
		{
			folder.mkdirs();
		}
		
		File saveFile = new File(HeroesLib.plugin.getDataFolder() + "/players/" + Character.toLowerCase(owner.charAt(0)) + "/" + owner + "/Companions/" + livingEntity.getType().toString() + "_"
				+ id + ".cmp");
		if(!saveFile.exists())
		{
			try
			{
				saveFile.createNewFile();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		
		SerializableCompanion serCmp = new SerializableCompanion(this);
		try
		{
			FileOutputStream fos = new FileOutputStream(saveFile);
	        ZipOutputStream zos = new ZipOutputStream(fos);
	        zos.putNextEntry(new ZipEntry("data"));
	        ObjectOutputStream oos = new ObjectOutputStream(zos);
	        oos.writeObject(serCmp);
	        oos.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
