package me.marwzoor.skillscorch;

import java.util.Iterator;

import net.smudgecraft.heroeslib.util.ParticleEffects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillScorch extends ActiveSkill
{
	public static SkillScorch skill;	
	
	public SkillScorch(Heroes instance)
	{
		super(instance, "Scorch");
		skill=this;
		setDescription("You heat up all pure metal within $X blocks, dealing $Y burn damage every second for $Z second to all enemies in metal armor. M: $1 CD: $2");
		setUsage("/skill scorch");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill scorch" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new FlameballListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 10, false) * hero.getSkillLevel(skill)));
			int range = (SkillConfigManager.getUseSetting(hero, skill, "range", 15, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "range-increase", 0, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 15000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			duration = duration/1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", range + "").replace("$Y", damage + "").replace("$Z", duration + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 15000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 50);
		node.set(SkillSetting.DAMAGE.node(), 50);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 10);
		node.set("range", 15);
		node.set("range-increase", 0);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 50, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 10, false) * hero.getSkillLevel(skill)));
		int range = (SkillConfigManager.getUseSetting(hero, skill, "range", 15, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "range-increase", 0, false) * hero.getSkillLevel(skill)));
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 15000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new ScorchRunnable(hero.getPlayer(), range, damage, (int) (duration/1000)));
		
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
				" is scorching enemies in metal armor!");
		
		return SkillResult.NORMAL;
	}
	
	public class ScorchRunnable implements Runnable
	{
		private Player caster;
		private int radius;
		private double damage;
		private int duration;
		private int counter=0;
		
		public ScorchRunnable(Player caster, int radius, double damage, int duration)
		{
			this.caster = caster;
			this.radius = radius;
			this.damage = damage;
			this.duration = duration;
		}
		
		@Override
		public void run()
		{
			Iterator<Entity> itr = caster.getNearbyEntities(radius, radius, radius).iterator();
			while(itr.hasNext())
			{
				Entity en = itr.next();
				if(!(en instanceof LivingEntity))
					continue;
				LivingEntity target = (LivingEntity)en;
				if(!hasMetalArmor(target.getEquipment()))
					continue;
				skill.damageEntity(target, caster, damage);
				try
				{
					ParticleEffects.FLAME.sendToLocation(target.getLocation().add(0,1.3,0),0.3f,0.3f,0.3f, 0.025F, 10);
				}
				catch(Exception e)
				{
				}
			}
			++counter;
			if(counter >= duration)
			{
				broadcast(caster.getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + caster.getName() + ChatColor.GRAY +
						" is no longer scorching enemies!");
				return;
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 20L);
			return;
		}
		
		private boolean hasMetalArmor(EntityEquipment eq)
		{
			for(ItemStack is : eq.getArmorContents())
			{
				if(is == null)
					continue;
				switch(is.getTypeId())
				{
				case 417:
				case 418:
				case 419:
				case 306:
				case 307:
				case 308:
				case 309:
				case 310:
				case 311:
				case 312:
				case 313:
				case 314:
				case 315:
				case 316:
				case 317:
					return true;
				}
			}
			return false;
		}
	}
}
