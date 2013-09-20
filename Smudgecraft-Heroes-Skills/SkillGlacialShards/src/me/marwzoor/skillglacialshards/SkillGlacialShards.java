package me.marwzoor.skillglacialshards;

import java.util.Iterator;

import net.smudgecraft.heroeslib.util.FireworkEffectPlayer;
import net.smudgecraft.heroeslib.util.PlayerUtils;

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
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillGlacialShards extends ActiveSkill
{
	public static SkillGlacialShards skill;	
	
	public SkillGlacialShards(Heroes instance)
	{
		super(instance, "GlacialShards");
		skill=this;
		setDescription("For $X seconds you send out glacial shards in a rapid fire, draining $Y mana every hit. M: $1 CD: $2");
		setUsage("/skill glacialshards");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill glacialshards" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new DragonsBreathListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double manaDrain = (SkillConfigManager.getUseSetting(hero, skill, "mana-drain", 20, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "mana-drain-increase", 0, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 10000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(skill)));
			duration = duration/1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", duration + "").replace("$Y", manaDrain + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 4000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 50);
		node.set("mana-drain", 4);
		node.set("mana-drain-increase", 0.05);
		node.set("range", 20);
		node.set("range-increase", 0);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double manaDrain = (SkillConfigManager.getUseSetting(hero, skill, "mana-drain", 20, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "mana-drain-increase", 0, false) * hero.getSkillLevel(skill)));
		long duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 10000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(skill)));
		int range = (SkillConfigManager.getUseSetting(hero, skill, "range", 20, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "range-increase", 0, false) * hero.getSkillLevel(skill)));
		hero.addEffect(new GlacialShardsEffect(duration, manaDrain, range));
		
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
				" is launching " + ChatColor.WHITE + "Glacial Shards" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	public class GlacialShardsEffect extends PeriodicExpirableEffect
	{
		private double manaDrain;
		private int range;
		
		public GlacialShardsEffect(long duration, double manaDrain, int range)
		{
			super(SkillGlacialShards.skill, "GlacialShards", 500, duration);
			this.manaDrain = manaDrain;
			this.range = range;
		}

		@Override
		public void tickHero(Hero hero)
		{
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new GlacialShardsRunnable(range, manaDrain, hero.getPlayer().getLocation().getDirection(),
					hero.getPlayer().getLocation().add(0,0.5,0), hero.getPlayer()));
		}

		@Override
		public void tickMonster(Monster monster)
		{
		}
	}
	
	public class GlacialShardsRunnable implements Runnable
	{
		private Player caster;
		private int range;
		private double manaDrain;
		private int counter = 0;
		private Vector direction;
		private Location loc;
		private final FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
		private final FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.AQUA).withColor(Color.BLUE).build();
		
		public GlacialShardsRunnable(int range, double manaDrain, Vector direction, Location loc, Player caster)
		{
			this.range = range;
			this.manaDrain = manaDrain;
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
			if(target == null || !(target instanceof Player) || !PlayerUtils.damageCheck(caster, target))
			{
				++counter;
				if(counter >= range)
					return;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 1L);
				return;
			}
			plugin.getCharacterManager().getHero((Player)target).setMana((int) (plugin.getCharacterManager().getHero((Player)target).getMana()-manaDrain));
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
