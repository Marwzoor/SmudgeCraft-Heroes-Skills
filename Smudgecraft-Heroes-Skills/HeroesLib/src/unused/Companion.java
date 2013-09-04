package unused;

import net.smudgecraft.heroeslib.HeroesLib;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.characters.Hero;

@Deprecated
public abstract class Companion 
{
	private final LivingEntity livingEntity;
	protected CompanionType ctype;
	private String owner;
	private String name;
	private final String id;
	private boolean shouldSave;
	
	protected Companion(LivingEntity livingEntity, String owner, String name, String id, boolean shouldSave)
	{
		this.livingEntity=livingEntity;
		this.owner=owner;
		this.name=name;
		this.ctype=CompanionType.UNKNOWN;
		this.id=id;
		this.shouldSave=shouldSave;
	}
	
	public boolean shouldSave()
	{
		return this.shouldSave;
	}
	
	public void setShouldSave(boolean shouldSave)
	{
		this.shouldSave=shouldSave;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public CompanionType getType()
	{
		return this.ctype;
	}
	
	public LivingEntity getLivingEntity()
	{
		return this.livingEntity;
	}
	
	public void kill()
	{
		this.livingEntity.remove();
	}
	
	public Player getOwner()
	{
		return Bukkit.getPlayer(owner);
	}
	
	public String getOwnerName()
	{
		return this.owner;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void showName()
	{
		if(this.name!=null)
		{
			this.livingEntity.setCustomName(this.name);
			this.livingEntity.setCustomNameVisible(true);
		}
	}
	
	public void hideName()
	{
		this.livingEntity.setCustomNameVisible(false);
	}
	
	public void setName(String name)
	{
		this.name=name;
	}
	
	public void setOwner(Player player)
	{
		this.owner=player.getName();
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
			Hero hero = HeroesLib.heroes.getCharacterManager().getHero(player);
			
			if(hero!=null)
			{
			if(hero.isInCombat())
				return true;
			}
		}
		return false;
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
	
	public void setHealth(double health)
	{
		if(health>=((Damageable) this.livingEntity).getMaxHealth())
		{
			this.livingEntity.setHealth(((Damageable) this.livingEntity).getMaxHealth());
		}
		else
		{
			this.livingEntity.setHealth(health);
		}
	}
	
	public void setMaxHealth(double maxhealth)
	{
		this.livingEntity.setMaxHealth(maxhealth);
	}
	
	public void addPotionEffect(PotionEffectType pet, int duration, int amplifier)
	{
		PotionEffect pe = pet.createEffect(duration, amplifier);

		this.livingEntity.addPotionEffect(pe);
	}
	
	public Location getLocation()
	{
		return this.livingEntity.getLocation();
	}
}
