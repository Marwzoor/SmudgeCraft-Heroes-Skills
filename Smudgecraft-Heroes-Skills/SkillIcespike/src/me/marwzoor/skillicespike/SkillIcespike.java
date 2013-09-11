package me.marwzoor.skillicespike;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillIcespike extends ActiveSkill
{
	public static SkillIcespike skill;	
	
	public SkillIcespike(Heroes instance)
	{
		super(instance, "Icespike");
		skill=this;
		setDescription("You launch an ice spike, dealing $X damage and applying the frozen condition for $Y seconds. Every second in the frozen condition drains $Z " +
				"mana and slows the target. M: $1 CD: $2");
		setUsage("/skill icespike");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill icespike" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new TwinLashLinstener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 30, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0.05, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 10000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			double manaDrain = (SkillConfigManager.getUseSetting(hero, skill, "mana-drain", 5, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "mana-drain-increase", 0.05, false) * hero.getSkillLevel(skill)));
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", damage + "").replace("$Y", duration + "").replace("$Z", manaDrain + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(), 30);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 0.05);
		node.set(SkillSetting.DURATION.node(), 10000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 50);
		node.set("mana-drain", 5);
		node.set("mana-drain-increase", 0.05);
		node.set("range", 10);
		node.set("range-increase", 0);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		//double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 60, false) +
		//		(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 1, false) * hero.getSkillLevel(skill)));
		Vector v = hero.getPlayer().getLocation().getDirection();
		Location loc = hero.getPlayer().getEyeLocation();
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 30, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0.05, false) * hero.getSkillLevel(skill)));
		int range = (SkillConfigManager.getUseSetting(hero, skill, "range", 10, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "range-increase", 0, false) * hero.getSkillLevel(skill)));
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new IcespikeRunnable(range, damage, v, loc, hero.getPlayer()));
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
				" launched an " + ChatColor.WHITE + "Ice Spike" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	public class FireworkEffectPlayer {
	    
	    /*
	     * Example use:
	     * 
	     * public class FireWorkPlugin implements Listener {
	     * 
	     * FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
	     * 
	     * @EventHandler
	     * public void onPlayerLogin(PlayerLoginEvent event) {
	     *   fplayer.playFirework(event.getPlayer().getWorld(), event.getPlayer.getLocation(), Util.getRandomFireworkEffect());
	     * }
	     * 
	     * }
	     */
	    
	    // internal references, performance improvements
	    private Method world_getHandle = null;
	    private Method nms_world_broadcastEntityEffect = null;
	    private Method firework_getHandle = null;
	    
	    /**
	     * Play a pretty firework at the location with the FireworkEffect when called
	     * @param world
	     * @param loc
	     * @param fe
	     * @throws Exception
	     */
	    public void playFirework(World world, Location loc, FireworkEffect fe) throws Exception {
	        // Bukkity load (CraftFirework)
	        Firework fw = (Firework) world.spawn(loc, Firework.class);
	        // the net.minecraft.server.World
	        Object nms_world = null;
	        Object nms_firework = null;
	        /*
	         * The reflection part, this gives us access to funky ways of messing around with things
	         */
	        if(world_getHandle == null) {
	            // get the methods of the craftbukkit objects
	            world_getHandle = getMethod(world.getClass(), "getHandle");
	            firework_getHandle = getMethod(fw.getClass(), "getHandle");
	        }
	        // invoke with no arguments
	        nms_world = world_getHandle.invoke(world, (Object[]) null);
	        nms_firework = firework_getHandle.invoke(fw, (Object[]) null);
	        // null checks are fast, so having this seperate is ok
	        if(nms_world_broadcastEntityEffect == null) {
	            // get the method of the nms_world
	            nms_world_broadcastEntityEffect = getMethod(nms_world.getClass(), "broadcastEntityEffect");
	        }
	        /*
	         * Now we mess with the metadata, allowing nice clean spawning of a pretty firework (look, pretty lights!)
	         */
	        // metadata load
	        FireworkMeta data = (FireworkMeta) fw.getFireworkMeta();
	        // clear existing
	        data.clearEffects();
	        // power of one
	        data.setPower(1);
	        // add the effect
	        data.addEffect(fe);
	        // set the meta
	        fw.setFireworkMeta(data);
	        /*
	         * Finally, we broadcast the entity effect then kill our fireworks object
	         */
	        // invoke with arguments
	        nms_world_broadcastEntityEffect.invoke(nms_world, new Object[] {nms_firework, (byte) 17});
	        // remove from the game
	        fw.remove();
	    }
	    
	    /**
	     * Internal method, used as shorthand to grab our method in a nice friendly manner
	     * @param cl
	     * @param method
	     * @return Method (or null)
	     */
	    private Method getMethod(Class<?> cl, String method) {
	        for(Method m : cl.getMethods()) {
	            if(m.getName().equals(method)) {
	                return m;
	            }
	        }
	        return null;
	    }

	}
	
	public class IcespikeRunnable implements Runnable
	{
		private Player caster;
		private int range;
		private double damage;
		private int counter = 0;
		private Vector direction;
		private Location loc;
		private final FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
		private final FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.AQUA).withColor(Color.BLUE).build();
		
		public IcespikeRunnable(int range, double damage, Vector direction, Location loc, Player caster)
		{
			this.range = range;
			this.damage = damage;
			this.direction = direction;
			this.loc = loc;
			this.caster = caster;
		}
		
		@Override
		public void run()
		{
			loc.add(direction);
			if(isInWall(loc))
				return;
			try
			{
				fplayer.playFirework(loc.getWorld(), loc, fe);
			}
			catch(Exception e)
			{
			}
			LivingEntity target = getNerbyTarget(1.75);
			if(target == null)
			{
				++counter;
				if(counter >= range)
					return;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 1L);
				return;
			}
			skill.damageEntity(target, caster, damage);
			Hero hero = plugin.getCharacterManager().getHero(caster);
			long duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 10000, false) +
						(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			PotionEffect pe = new PotionEffect(PotionEffectType.SLOW, (int) ((duration/1000)*20), 0);
			target.addPotionEffect(pe);
			if(target instanceof Player)
			{
				Hero hTarget = plugin.getCharacterManager().getHero((Player)target);
				int manaDrain = (int) (SkillConfigManager.getUseSetting(hero, skill, "mana-drain", 5, false) +
						(SkillConfigManager.getUseSetting(hero, skill, "mana-drain-increase", 0.05, false) * hero.getSkillLevel(skill)));
				hTarget.addEffect(new IcespikeEffect(skill, 1000L, duration, 0, caster, false, manaDrain));
			}
		}
		
		private boolean isInWall(Location loc)
		{
			switch(loc.getBlock().getTypeId())
			{
			case 0:
			case 8:
			case 9:
			case 106:
			case 31:
			case 32:
				return false;
			default:
				return true;
			}
		}
		
		private LivingEntity getNerbyTarget(double radius)
		{
			Iterator<Entity> itr = loc.getWorld().getEntities().iterator();
			while(itr.hasNext())
			{
				Entity en = itr.next();
				if(!(en instanceof LivingEntity))
					continue;
				LivingEntity temp = (LivingEntity)en;
				if(temp.equals(caster))
					continue;
				Location tempLoc = temp.getLocation();
				if(loc.getX() - radius < tempLoc.getX() && tempLoc.getX() < loc.getX() + radius && loc.getY() - radius < tempLoc.getY() && tempLoc.getY() < loc.getY() + radius &&
						loc.getZ() - radius < tempLoc.getZ() && tempLoc.getZ() < loc.getZ() + radius)
					return temp;
			}
			return null;
		}
	}
	
	public class IcespikeEffect extends PeriodicDamageEffect
	{
		private int manaDrain;
		
		public IcespikeEffect(Skill skill, long peroid, long duration, double damage, Player applier, boolean knockback, int manaDrain)
		{
			super(skill, "Icespike", peroid, duration, damage, applier, knockback);
			this.manaDrain = manaDrain;
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
			//Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + applier.getName() + ChatColor.GRAY + " is draining your life!");
			//Messaging.send(applier.getPlayer(), "You are no longer draining " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.WHITE + "'s" + ChatColor.GRAY + " life!");
			super.removeFromHero(hero);
		}
		
		@Override
		public void removeFromMonster(Monster monster)
		{
			//Messaging.send(applier.getPlayer(), ChatColor.GRAY + "You are no longer draining the " + ChatColor.DARK_GREEN + monster.getEntity().getType().getName() + ChatColor.WHITE + 
			//		"'s" + ChatColor.GRAY + " life!");
			super.removeFromMonster(monster);
		}
		
		/*@Override
		public void tickMonster(Monster monster)
		{
			if(!monster.getEntity().getWorld().equals(applier.getWorld()))
			{
				monster.removeEffect(this);
				return;
			}
			double distance = applier.getLocation().distance(monster.getEntity().getLocation());
			if(SkillConfigManager.getUseSetting(plugin.getCharacterManager().getHero(applier), skill, "range", 15, false) < distance)
			{
				monster.removeEffect(this);
				return;
			}
			lifeDrainFWE(plugin.getCharacterManager().getHero(applier), monster.getEntity(), tickDamage);
			super.tickMonster(monster);
		}*/
		
		@Override
		public void tickHero(Hero hero)
		{
			int mana = hero.getMana() - manaDrain;
			if(mana < 0)
				mana = 0;
			hero.setMana(mana);
			super.tickHero(hero);
		}
	}
}
