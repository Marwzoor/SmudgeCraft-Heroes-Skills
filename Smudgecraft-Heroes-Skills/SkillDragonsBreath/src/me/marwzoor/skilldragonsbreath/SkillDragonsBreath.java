package me.marwzoor.skilldragonsbreath;

import java.util.Iterator;

import net.smudgecraft.heroeslib.util.FireworkEffectPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillDragonsBreath extends ActiveSkill
{
	public static SkillDragonsBreath skill;	
	
	public SkillDragonsBreath(Heroes instance)
	{
		super(instance, "DragonsBreath");
		skill=this;
		setDescription("You breath fire in the direction you are facing for $X seconds, dealing $Y damage with every flame. M: $1 CD: $2");
		setUsage("/skill dragonsbreath");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill dragonsbreath" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new DragonsBreathListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 20, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 10000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(skill)));
			duration = duration/1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", duration + "").replace("$Y", damage + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(), 20);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 0);
		node.set(SkillSetting.DURATION.node(), 10000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 0);
		node.set("range", 20);
		node.set("range-increase", 0);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 20, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0, false) * hero.getSkillLevel(skill)));
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 10000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(skill)));
		int range = (SkillConfigManager.getUseSetting(hero, skill, "range", 20, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "range-increase", 0, false) * hero.getSkillLevel(skill)));
		duration = duration/1000;
		duration = duration*20;
		duration = duration/5;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DragonsBreathRunnable(hero.getPlayer(), (int) duration, damage, range));
		
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
				" is breathing fire like a dragon!");
		
		return SkillResult.NORMAL;
	}
	
	public class DragonsBreathRunnable implements Runnable
	{
		private int times;
		private int counter=0;
		private double damage;
		private Player caster;
		private int range;
		
		public DragonsBreathRunnable(Player caster, int times, double damage, int range)
		{
			this.caster = caster;
			this.times = times;
			this.damage = damage;
			this.range = range;
		}
		
		@Override
		public void run()
		{
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DragonsBreathFireRunnable(range, damage, caster.getLocation().getDirection(), caster.getLocation().add(0,0.5,0), caster));
			//SmallFireball fireball = caster.launchProjectile(SmallFireball.class);
			//fireball.setIsIncendiary(false);
			++counter;
			if(counter >= times)
			{
				broadcast(caster.getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + caster.getName() + ChatColor.GRAY +
						" is no longer breathing fire!");
				return;
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 4L);
			return;
		}
	}
	
	public class DragonsBreathFireRunnable implements Runnable
	{
		private Player caster;
		private int range;
		private double damage;
		private int counter = 0;
		private Vector direction;
		private Location loc;
		private final FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
		private final FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.RED).withColor(Color.ORANGE).build();
		
		public DragonsBreathFireRunnable(int range, double damage, Vector direction, Location loc, Player caster)
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
			return;
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
}
