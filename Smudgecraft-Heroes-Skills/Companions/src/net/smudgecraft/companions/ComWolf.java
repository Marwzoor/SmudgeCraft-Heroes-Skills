package net.smudgecraft.companions;


import net.minecraft.server.EntityWolf;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.characters.Hero;

public class ComWolf
{
	protected final Wolf wolf;
	protected String owner;
	protected String name;
	protected int damage;
	protected int regscheduleid;
	
	public ComWolf(Wolf wolf, String owner)
	{
		this.wolf=wolf;
		this.owner=owner;
		this.wolf.setOwner(Bukkit.getOfflinePlayer(owner));
		this.name=null;
		this.damage=-1;
	}
	
	public ComWolf(Wolf wolf, String owner, String name)
	{
		this.wolf=wolf;
		this.owner=owner;
		this.wolf.setOwner(Bukkit.getOfflinePlayer(owner));
		this.name=name;
		this.damage=-1;
	}
	
	public ComWolf(Wolf wolf, String owner, int customDamage)
	{
		this.wolf=wolf;
		this.owner=owner;
		this.wolf.setOwner(Bukkit.getOfflinePlayer(owner));
		this.name=null;
		this.damage=customDamage;
	}
	
	public ComWolf(Wolf wolf, String owner, String name, int customDamage)
	{
		this.wolf=wolf;
		this.owner=owner;
		this.wolf.setOwner(Bukkit.getOfflinePlayer(owner));
		this.name=name;
		this.damage=customDamage;
	}
	
	public World getWorld()
	{
		return this.wolf.getWorld();
	}
	
	public void kill()
	{
		Bukkit.getScheduler().cancelTask(this.regscheduleid);
		this.wolf.remove();
	}
	
	public Wolf getWolf()
	{
		return this.wolf;
	}
	
	public Player getWolfOwner()
	{
		return Bukkit.getPlayer(owner);
	}
	
	public String getWolfOwnerName()
	{
		return this.owner;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public int getDamage()
	{
		return this.damage;
	}
	
	public boolean hasCustomDamage()
	{
		if(this.damage>=0)
		{
			return true;
		}
		return false;
	}
	
	public void setOwner(Player player)
	{
		this.owner=player.getName();
	}
	
	public void setDamage(int damage)
	{
		this.damage=damage;
	}
	
	public boolean hasOnlineOwner()
	{
		OfflinePlayer oplayer = Bukkit.getOfflinePlayer(this.owner);
		if(oplayer.isOnline())
			return true;
		return false;
	}
	
	public boolean isInCombat()
	{
		if(hasOnlineOwner())
		{
			Player player = Bukkit.getPlayer(this.owner);
			Hero hero = Companions.heroes.getCharacterManager().getHero(player);
			
			if(hero!=null)
			{
			if(hero.isInCombat())
				return true;
			}
		}
		return false;
	}
	
	//Makes the wolf attack a specified target, dunno if it works
	public void attack(LivingEntity target)
	{
		CraftLivingEntity cle = (CraftLivingEntity) target;
		CraftWolf cw = (CraftWolf) this.wolf;
		EntityWolf ew = (EntityWolf) cw.getHandle();
		ew.c(cle.getHandle());
	}
	
	//Makes the wolf jump
	public void jump()
	{
		CraftWolf cw = (CraftWolf) this.wolf;
		EntityWolf ew = (EntityWolf) cw.getHandle();
		ew.getControllerJump().a();
	}
	
	public void heal(int heal)
	{
		if(this.wolf.getHealth()+heal>=this.wolf.getMaxHealth())
		{
			this.wolf.setHealth(this.wolf.getMaxHealth());
			
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(wolf, heal, RegainReason.MAGIC_REGEN);
			
			Bukkit.getPluginManager().callEvent(event);
		}
		else
		{
			this.wolf.setHealth(this.wolf.getHealth()+heal);
			
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(wolf, heal, RegainReason.MAGIC_REGEN);
			
			Bukkit.getPluginManager().callEvent(event);
		}
	}
	
	public void setHealth(int health)
	{
		if(health>=this.wolf.getMaxHealth())
		{
			this.wolf.setHealth(this.wolf.getMaxHealth());
		}
		else
		{
			this.wolf.setHealth(health);
		}
	}
	
	public void setMaxHealth(int maxhealth)
	{
		this.wolf.setMaxHealth(maxhealth);
	}
	
	public void addPotionEffect(PotionEffectType pet, int duration, int amplifier)
	{
		PotionEffect pe = pet.createEffect(duration, amplifier);

		this.wolf.addPotionEffect(pe);
	}
	
	public Location getLocation()
	{
		return this.wolf.getLocation();
	}
	
}
