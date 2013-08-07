package me.marwzoor.skillaxethrow;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillAxeThrow extends ActiveSkill
{
	public static SkillAxeThrow skill;	
	
	public SkillAxeThrow(Heroes instance)
	{
		super(instance, "AxeThrow");
		skill=this;
		setDescription("You throw you an axe, dealing %1 damage if it hits your enemy.");
		setUsage("/skill axethrow");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill axethrow" });
		setTypes(new SkillType[] { SkillType.HARMFUL, SkillType.DAMAGING, SkillType.FORCE, SkillType.ITEM, SkillType.PHYSICAL });
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			int damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 1, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0, false) * hero.getSkillLevel(skill)));
			return super.getDescription().replace("%1", damage + "");
		}
		else
		{
		return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(), 150);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 5);
		node.set("itemid", 258);
		return node;
	}
	
	public SkillResult use(final Hero hero, String[] args)
	{
		Vector v = hero.getPlayer().getLocation().getDirection();
		int itemid = SkillConfigManager.getUseSetting(hero, skill, "itemid", 258, false);
		ItemStack throwitem = new ItemStack(Material.getMaterial(itemid));
		final Item item = hero.getPlayer().getWorld().dropItem(hero.getPlayer().getEyeLocation(), throwitem);
		item.setVelocity(v);
		item.setMetadata("Owner", new FixedMetadataValue(plugin, hero.getPlayer().getName()));
		
		int id=0;
		
		id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			public void run()
			{
				if(hero==null || hero.getPlayer() == null)
				{
					if(item.hasMetadata("ScheduleId") && item.getMetadata("ScheduleId").get(0)!=null)
					{
						Bukkit.getScheduler().cancelTask(item.getMetadata("ScheduleId").get(0).asInt());
					}
				}
				else
				{
				if(!item.getNearbyEntities(0.1, 0.1, 0.1).isEmpty())
				{
					List<Entity> entities = item.getNearbyEntities(0.1, 0.1, 0.1);
					
					boolean success = false;
					
					for(Entity en : entities)
					{
						if(en instanceof LivingEntity && !((LivingEntity) en).equals(hero.getEntity()))
						{
							LivingEntity le = (LivingEntity) en;
							
							double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 1, false) +
									(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0, false) * hero.getSkillLevel(skill)));
							Skill.damageEntity(le, hero.getEntity(), damage, DamageCause.MAGIC);
							skill.addSpellTarget(le, hero);
							success=true;
							continue;
						}
					}
					
					if(success)
					{
						if(item.hasMetadata("ScheduleId") && item.getMetadata("ScheduleId").get(0)!=null)
						{
							Bukkit.getScheduler().cancelTask(item.getMetadata("ScheduleId").get(0).asInt());
							item.removeMetadata("ScheduleId", plugin);
						}
					}
				}
				else if(item.getVelocity().getX()<0.01 && item.getVelocity().getZ()<0.01)
				{
					if(item.hasMetadata("ScheduleId") && item.getMetadata("ScheduleId").get(0)!=null)
					{
						Bukkit.getScheduler().cancelTask(item.getMetadata("ScheduleId").get(0).asInt());
						item.removeMetadata("ScheduleId", plugin);
					}
				}
				}
			}
		}, 1L, 1L);
		
		item.setMetadata("ScheduleId", new FixedMetadataValue(plugin, id));
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onItemPickupEvent(PlayerPickupItemEvent event)
		{
			Player player = event.getPlayer();
			
			if(event.getItem().hasMetadata("Owner") && event.getItem().getMetadata("Owner").get(0)!=null)
			{
				if(!event.getItem().getMetadata("Owner").get(0).asString().equalsIgnoreCase(player.getName()))
				{
					event.setCancelled(true);
				}
			}
		}
	}
}
