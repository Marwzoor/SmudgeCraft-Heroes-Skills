package me.marwzoor.skillbatswarm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillBatSwarm extends TargettedSkill
{
	public static SkillBatSwarm skill;	
	
	public SkillBatSwarm(Heroes instance)
	{
		super(instance, "BatSwarm");
		skill=this;
		setDescription("You summon a swarm of bats, surrounding your enemy, dealing $X damage every second for $Y seconds. M: $1 CD: $2");
		setUsage("/skill batswarm");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill batswarm" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 70, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0.5D, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 15000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			duration = duration/1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", damage + "").replace("$Y", duration + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(), 70);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 0.5);
		node.set(SkillSetting.DURATION.node(), 15000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 50);
		return node;
	}
	
	@Override
	public SkillResult use(final Hero hero, final LivingEntity target, String[] args)
	{
		if(hero.getEntity().equals(target))
		{
			Messaging.send(hero.getPlayer(), "You can't target yourself!");
			return SkillResult.INVALID_TARGET_NO_MSG;
		}
		/*double distance = hero.getPlayer().getLocation().distance(target.getLocation());
		if(SkillConfigManager.getUseSetting(hero, skill, "range", 15, false) < distance)
		{
			Messaging.send(hero.getPlayer(), "The target is out of range!");
			return SkillResult.INVALID_TARGET_NO_MSG;
		}*/
		
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 70, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0.5D, false) * hero.getSkillLevel(skill)));
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 15000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		plugin.getCharacterManager().getCharacter(target).addEffect(new BatSwarmEffect(this, (long)duration, damage, hero.getPlayer(), target));
		
		if(target instanceof Player)
			broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + 
					" summons a " + ChatColor.WHITE + "Bat Swarm " + ChatColor.GRAY + "at " + ChatColor.DARK_RED + ((Player)target).getName() + ChatColor.GRAY + "!");
		else
			broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + 
					" summons a " + ChatColor.WHITE + "Bat Swarm " + ChatColor.GRAY + "at " + ChatColor.DARK_GREEN + target.getType().getName() + ChatColor.GRAY + "!");
		return SkillResult.NORMAL;	
	}
	
	public class BatSwarmEffect extends ExpirableEffect
	{
		List<LivingEntity> bats = new ArrayList<LivingEntity>();
		private final Player applier;
		private final int id;
		private final int id2;
		
		public BatSwarmEffect(Skill skill, long duration, final double damage, final Player applier, final LivingEntity target)
		{
			super(skill, "BatSwarm", duration);
			this.applier = applier;
			for(int i=0;i<10;++i)
			{
				Bat bat = (Bat)applier.getWorld().spawnEntity(applier.getEyeLocation(), EntityType.BAT);
				bat.setMaxHealth(30000D);
				bat.setHealth(30000D);
				bats.add(bat);
			}
			id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						boolean doneDamage = false;
						Iterator<LivingEntity> itr = bats.iterator();
						while(itr.hasNext())
						{
							LivingEntity bat = itr.next();
							if(bat == null)
								continue;
							if(bat.isDead())
								continue;
							
							if(!doneDamage && bat.getNearbyEntities(0.5, 0.5, 0.5).contains(target))
							{
								doneDamage = true;
								damageEntity(target, applier, damage);
							}
						}
					}
					catch(Exception e)
					{
					}
				}
			},0,20L);
			id2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					Iterator<LivingEntity> itr = bats.iterator();
					while(itr.hasNext())
					{
						LivingEntity bat = itr.next();
						if(bat == null)
							continue;
						if(bat.isDead())
							continue;
						double dist = bat.getLocation().distance(target.getLocation());
						if(target instanceof Player)
						{
							Player pTarget = (Player)target;
							double percent = 1 / dist;
							final double x = ((bat.getLocation().getX() - pTarget.getEyeLocation().getX()) * percent) * -1;
							final double y = ((bat.getLocation().getY() - pTarget.getEyeLocation().getY()) * percent) * -1;
							final double z = ((bat.getLocation().getZ() - pTarget.getEyeLocation().getZ()) * percent) * -1;
							bat.teleport(bat.getLocation().add(x,y,z));
						}
						else
						{
							double percent = 1 / dist;
							final double x = ((bat.getLocation().getX() - target.getLocation().getX()) * percent) * -1;
							final double y = ((bat.getLocation().getY() - (target.getLocation().getY()+1)) * percent) * -1;
							final double z = ((bat.getLocation().getZ() - target.getLocation().getZ()) * percent) * -1;
							bat.teleport(bat.getLocation().add(x,y,z));
						}
					}
				}
			},3,1L);
		}
		
		@Override
		public void applyToHero(Hero hero)
		{
			//Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + applier.getName() + ChatColor.GRAY + " is draining your life!");
			super.applyToHero(hero);
		}
		
		@Override
		public void removeFromHero(Hero hero)
		{
			Bukkit.getScheduler().cancelTask(id);
			Bukkit.getScheduler().cancelTask(id2);
			Iterator<LivingEntity> itr = bats.iterator();
			while(itr.hasNext())
			{
				LivingEntity bat = itr.next();
				if(bat == null)
					continue;
				if(bat.isDead())
					continue;
				bat.remove();
				itr.remove();
			}
			broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] The " + ChatColor.WHITE + "Bat Swarm " + ChatColor.GRAY + 
					"summoned by " + ChatColor.DARK_RED + applier.getName() + ChatColor.GRAY + " has perished!");
			super.removeFromHero(hero);
		}
		
		@Override
		public void removeFromMonster(Monster monster)
		{
			Bukkit.getScheduler().cancelTask(id);
			Bukkit.getScheduler().cancelTask(id2);
			Iterator<LivingEntity> itr = bats.iterator();
			while(itr.hasNext())
			{
				LivingEntity bat = itr.next();
				if(bat == null)
					continue;
				if(bat.isDead())
					continue;
				bat.remove();
				itr.remove();
			}
			broadcast(monster.getEntity().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] The " + ChatColor.WHITE + "Bat Swarm " + ChatColor.GRAY + 
					"summoned by " + ChatColor.DARK_RED + applier.getName() + ChatColor.GRAY + " has perished!");
			super.removeFromMonster(monster);
		}
		
		/*private Vector getTargetVector(Location shooter, Location target)
		{
			Location first_location = shooter.add(0, 1, 0);
			Location second_location = target.add(0, 1, 0);
			Vector vector = second_location.toVector().subtract(first_location.toVector());
			return vector;
		}*/
	}
}
