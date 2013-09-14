package me.marwzoor.skillflameball;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.smudgecraft.heroeslib.commoneffects.BurnEffect;
import net.smudgecraft.heroeslib.util.FireworkEffectPlayer;
import net.smudgecraft.heroeslib.util.ParticleEffects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillFlameball extends ActiveSkill
{
	public static SkillFlameball skill;	
	public static List<Snowball> fireballs = new ArrayList<Snowball>();
	
	public SkillFlameball(Heroes instance)
	{
		super(instance, "Flameball");
		skill=this;
		setDescription("You hurl a flame ball at your target, dealing $X damage and $Y burn damage every second for $Z seconds. M: $1 CD: $2");
		setUsage("/skill flameball");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill flameball" });
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new FlameballListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 10, false) * hero.getSkillLevel(skill)));
			double flameDamage = (SkillConfigManager.getUseSetting(hero, skill, "burn-damage", 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "burn-damage-increase", 0.5, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 7000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			duration = duration/1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", damage + "").replace("$Y", flameDamage + "").replace("$Z", duration + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 7000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 50);
		node.set(SkillSetting.DAMAGE.node(), 50);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 10);
		node.set("burn-damage", 50);
		node.set("burn-damage-increase", 0.5);
		node.set("range", 16);
		node.set("range-increase", 0);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double range = (SkillConfigManager.getUseSetting(hero, skill, "range", 16, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "range-increase", 0, false) * hero.getSkillLevel(skill)));
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 50, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 10, false) * hero.getSkillLevel(skill)));
		double burnDamage = (SkillConfigManager.getUseSetting(hero, skill, "burn-damage", 50, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "burn-damage-increase", 0.5, false) * hero.getSkillLevel(skill)));
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 7000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		//Vector v = hero.getPlayer().getLocation().getDirection();
		//Bukkit.broadcastMessage(v.toString());
		//Location loc = hero.getPlayer().getLocation();
		//Location target = loc.clone().add(v.getX()*range, v.getY()*range, v.getZ()*range);
		//Vector velocity = getTargetVector(loc, target);
		Snowball en = hero.getPlayer().launchProjectile(Snowball.class);
		en.setFireTicks(9999999);
		//en.setVelocity(v);
		//en.setVelocity(en.getVelocity().multiply(2));
		//Fireball en = (Fireball)loc.getWorld().spawnEntity(loc, EntityType.FIREBALL);
		fireballs.add(en);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new FlameballRunnable(en, (int) range,hero.getPlayer(),damage,burnDamage,duration));
		//sb.setShooter(hero.getPlayer());
		//sb.setVelocity(v);
		/*double damage = (SkillConfigManager.getUseSetting(hero, skill, "burn-damage", 50, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "burn-damage-increase", 0.5, false) * hero.getSkillLevel(skill)));
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 5000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		try
		{
			makeEffect(range, loc.clone(), v);
		}
		catch(Exception e)
		{
		}
		
		Location base = loc.clone().add(v);
		Location top1 = loc.clone().add(range*v.getX(), range*v.getY(), range*v.getZ()).add(v.getZ()*range/2, 0, v.getX()*(range/2)*-1);
		Location top2 = loc.clone().add(range*v.getX(), range*v.getY(), range*v.getZ()).add(v.getZ()*(range/2)*-1, 0, v.getX()*range/2);
		
		hitEntities(base,top1,top2, damage, duration, hero.getPlayer());*/
		
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
				" hurls a " + ChatColor.WHITE + "Flameball" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	/*private Vector getTargetVector(Location shooter, Location target)
	{
		Location first_location = shooter.add(0, 1, 0);
		Location second_location = target.add(0, 1, 0);
		Vector vector = second_location.toVector().subtract(first_location.toVector());
		return vector;
	}*/
	
	public class FlameballRunnable implements Runnable
	{
		private Entity flameball;
		private int range;
		private Player caster;
		private double damage;
		private double burnDamage;
		private double duration;
		private final Location startLoc;
		private final FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
		private final FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.ORANGE).build();
		
		public FlameballRunnable(Entity en, int range, Player caster, double damage, double burnDamage, double duration)
		{
			flameball = en;
			this.range = range;
			this.caster = caster;
			this.damage = damage;
			this.burnDamage = burnDamage;
			this.duration = duration;
			startLoc = en.getLocation();
		}
		
		@Override
		public void run()
		{
			if(flameball.isDead())
			{
				fireballs.remove((Snowball)flameball);
				return;
			}
			//flameball.teleport(flameball.getLocation().add(direction));
			//flameball.setVelocity(direction);
			try
			{
				ParticleEffects.FLAME.sendToLocation(flameball.getLocation(),0.3f,0.3f,0.3f, 0.025F, 2);
			}
			catch(Exception e)
			{
			}
			Iterator<Entity> itr = flameball.getNearbyEntities(1.75, 1.75, 1.75).iterator();
			while(itr.hasNext())
			{
				Entity en = itr.next();
				if(!(en instanceof LivingEntity))
					continue;
				LivingEntity target = (LivingEntity)en;
				if(target.equals(caster))
					continue;
				skill.damageEntity(target, caster, damage);
				plugin.getCharacterManager().getCharacter(target).addEffect(new BurnEffect(skill,1000,(int) duration,burnDamage,caster,false));
				try
				{
					fplayer.playFirework(target.getWorld(), target.getLocation(), fe);
				}
				catch(Exception e)
				{
				}
				fireballs.remove((Snowball)flameball);
				flameball.remove();
				return;
			}
			//++counter;
			if(startLoc.distance(flameball.getLocation()) >= range)
			{
				fireballs.remove((Snowball)flameball);
				flameball.remove();
				return;
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 1L);
			return;
		}
	}
	
	public class FlameballListener implements Listener
	{
		@EventHandler
		public void onEntityExplodeEvent(EntityExplodeEvent event)
		{
			if(fireballs.contains(event.getEntity()))
				event.setCancelled(true);
		}
		
		/*@EventHandler
		public void onEntityDamageEvent(EntityDamageEvent event)
		{
			if(event.isCancelled())
				return;
			if(!(event.getEntity() instanceof Snowball))
				return;
			if(!fireballs.contains((Snowball)event.getEntity()))
				return;
			event.setCancelled(true);
		}*/
	}
}
