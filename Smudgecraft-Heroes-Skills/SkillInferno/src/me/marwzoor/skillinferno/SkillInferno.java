package me.marwzoor.skillinferno;

import java.util.Iterator;

import net.smudgecraft.heroeslib.commoneffects.BurnEffect;
import net.smudgecraft.heroeslib.util.CuboidArea;
import net.smudgecraft.heroeslib.util.FireworkEffectPlayer;
import net.smudgecraft.heroeslib.util.ParticleEffects;
import net.smudgecraft.heroeslib.util.PlayerUtils;
import net.smudgecraft.heroeslib.util.Utils;

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

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillInferno extends ActiveSkill
{
	public static SkillInferno skill;	
	
	public SkillInferno(Heroes instance)
	{
		super(instance, "Inferno");
		skill=this;
		setDescription("You unleash an inferno with $X blocks radius for $Y seconds, dealing $Z damage to players inside it when it is created and setting them on fire while inside the" +
				" inferno and for $Q seconds after leaving it. While on fire, they take $U damage every second. M: $1 CD: $2");
		setUsage("/skill inferno");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill inferno" });
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
			double burnDamage = (SkillConfigManager.getUseSetting(hero, skill, "burn-damage", 70, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "burn-damage-increase", 0, false) * hero.getSkillLevel(skill)));
			double burnDuration = (SkillConfigManager.getUseSetting(hero, skill, "burn-duration", 4000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "burn-duration-increase", 50, false) * hero.getSkillLevel(skill)));
			int radius = SkillConfigManager.getUseSetting(hero, skill, "radius", 3, false);
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", radius + "").replace("$Y", duration + "").replace("$Z", damage + "").replace("$Q", burnDuration + "").replace("$U", burnDamage + "")
					.replace("$1", mana + "").replace("$2", cd + "");
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
		node.set("burn-damage", 70);
		node.set("burn-damage-increase", 0);
		node.set("brun-duration", 4000);
		node.set("brun-duration-increase", 50);
		node.set("range", 15);
		node.set("range-increase", 0);
		node.set("radius", 3);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 30, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0.05, false) * hero.getSkillLevel(skill)));
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 10000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		double burnDamage = (SkillConfigManager.getUseSetting(hero, skill, "burn-damage", 70, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "burn-damage-increase", 0, false) * hero.getSkillLevel(skill)));
		double burnDuration = (SkillConfigManager.getUseSetting(hero, skill, "burn-duration", 4000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "burn-duration-increase", 50, false) * hero.getSkillLevel(skill)));
		int radius = SkillConfigManager.getUseSetting(hero, skill, "radius", 3, false);
		int range = (SkillConfigManager.getUseSetting(hero, skill, "range", 15, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "range-increase", 0, false) * hero.getSkillLevel(skill)));
		
		Location target = Utils.getTargetLocation(hero.getPlayer(), range);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new InfernoRunnable(hero.getPlayer(), (int) (duration/1000), burnDamage,burnDuration, radius, damage, target));
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY +
				" unleashed an " + ChatColor.WHITE + "Inferno" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	public class InfernoRunnable implements Runnable
	{
		private int duration;
		private int counter = 0;
		private double burnDamage;
		private double burnDuration;
		private CuboidArea area;
		//private int radius;
		private Player caster;
		private Location center;
		private final FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
		private final FireworkEffect fe = FireworkEffect.builder().with(Type.BALL_LARGE).withColor(Color.RED).withColor(Color.ORANGE).build();
		
		public InfernoRunnable(Player caster, int duration, double burnDamage, double burnDuration, int radius, double damage, Location center)
		{
			this.duration = duration;
			this.burnDamage = burnDamage;
			this.burnDuration = burnDuration;
			this.caster = caster;
			this.center = center;
			area = new CuboidArea(center, radius);
			Iterator<Location> itr = area.getSphereLocations(true).iterator();
			while(itr.hasNext())
			{
				try
				{
					ParticleEffects.FLAME.sendToLocation(itr.next(),0.3f,0.3f,0.3f, 0.025F, 10);
				}
				catch(Exception e)
				{
				}
			}
			try
			{
				fplayer.playFirework(center.getWorld(), center, fe);
			}
			catch(Exception e)
			{
			}
			Iterator<Entity> itr2 = area.getWorld().getEntities().iterator();
			while(itr2.hasNext())
			{
				Entity en = itr2.next();
				if(!(en instanceof LivingEntity))
					continue;
				LivingEntity target = (LivingEntity)en;
				if(!area.containsLoc(target.getLocation()))
					continue;
				if(!PlayerUtils.damageCheck(caster, target))
					continue;
				skill.damageEntity(target, caster, damage);
			}
			//Bukkit.broadcastMessage(area.getCenter().toString());
		}
		
		@Override
		public void run()
		{
			//Bukkit.broadcastMessage(area.getCenter().toString());
			try
			{
				fplayer.playFirework(center.getWorld(), center, fe);
			}
			catch(Exception e)
			{
			}
			Iterator<Location> itr2 = area.getSphereLocations(true).iterator();
			while(itr2.hasNext())
			{
				try
				{
					ParticleEffects.FLAME.sendToLocation(itr2.next(),0.3f,0.3f,0.3f, 0.025F, 10);
				}
				catch(Exception e)
				{
				}
			}
			Iterator<Entity> itr = area.getWorld().getEntities().iterator();
			while(itr.hasNext())
			{
				Entity en = itr.next();
				if(!(en instanceof LivingEntity))
					continue;
				LivingEntity target = (LivingEntity)en;
				if(!PlayerUtils.damageCheck(caster, target))
					continue;
				if(!area.containsLoc(target.getLocation()))
					continue;
				plugin.getCharacterManager().getCharacter(target).addEffect(new BurnEffect(skill, 1000, (int) burnDuration, burnDamage, caster, false));
			}
			++counter;
			if(counter >= duration)
			{
				return;
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 20L);
			return;
		}
	}
}
