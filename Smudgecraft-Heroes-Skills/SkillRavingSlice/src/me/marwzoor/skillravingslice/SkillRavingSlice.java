package me.marwzoor.skillravingslice;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillRavingSlice extends PassiveSkill
{	
	public SkillRavingSlice(Heroes instance)
	{
		super(instance, "RavingSlice");
		setDescription("If you sneak for more than %1 seconds your next blade attack will deal %2 more damage, the effect will wear off after %3 seconds. (Passive) D: %4 W: %5 DMG: %6");
		setIdentifiers(new String[] { "skill ravingslice" });
		setEffectTypes(new EffectType[] { EffectType.BENEFICIAL } );
		setTypes(new SkillType[] { SkillType.BUFF });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this, plugin), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			double warmup = (SkillConfigManager.getUseSetting(hero, this, "warmup", 1500, false)/1000);
			int damageperc = (int) ((SkillConfigManager.getUseSetting(hero, this, "damage-percent", 0.5D, false) +
					(SkillConfigManager.getUseSetting(hero, this, "damage-percent-increase", 0.005D, false)* hero.getSkillLevel(this)))*100);
			int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 1000, false)/1000);
			return super.getDescription().replace("%1", warmup + "").replace("%2", damageperc + "%").replace("%3", duration + "").replace("%4", duration + "s").replace("%5", warmup + "s").replace("%6", damageperc + "%");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "X%").replace("%3", "X").replace("%4", "Xs").replace("%5", "Xs").replace("%6", "X%");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("warmup", 1500);
		node.set("damage-percent", Double.valueOf(0.5));
		node.set("damage-percent-increase", Double.valueOf(0.005));
		node.set(SkillSetting.DURATION.node(), 1000);
		return node;
	}
	
	public class SkillHeroListener implements Listener
	{
		public final HashMap<Player, Integer> sneakingplayers = new HashMap<Player, Integer>();
		SkillRavingSlice skill;
		Heroes plugin;
		
		public SkillHeroListener(SkillRavingSlice skill, Heroes plugin)
		{
			this.skill=skill;
			this.plugin=plugin;
		}
		
		@EventHandler
		public void onToggleSneakEvent(PlayerToggleSneakEvent event)
		{
			if(event.isCancelled())
				return;
			
			Player player = event.getPlayer();
			
			final Hero hero = plugin.getCharacterManager().getHero(player);
			
			if(!hero.hasAccessToSkill(skill))
				return;
			
			if(event.isSneaking()==false)
			{
				if(sneakingplayers.containsKey(event.getPlayer()))
				{
					Bukkit.getScheduler().cancelTask(sneakingplayers.get(event.getPlayer()));
					sneakingplayers.remove(event.getPlayer());
				}
				return;
			}
			
			if(sneakingplayers.containsKey(player))
			{
				Bukkit.getScheduler().cancelTask(sneakingplayers.get(player));
				sneakingplayers.remove(player);
			}
			
			if(hero.hasEffect("RavingSliceEffect")==false)
			{
				double warmup = (SkillConfigManager.getUseSetting(hero, skill, "warmup", 1500, false)/1000);
				long warm = (long) (20 * warmup);
				int id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
						if(hero!=null && hero.getPlayer()!=null && hero.getPlayer().isOnline())
						{
							double damageperc = (SkillConfigManager.getUseSetting(hero, skill, "damage-percent", 0.5D, false) +
									(SkillConfigManager.getUseSetting(hero, skill, "damage-percent-increase", 0.005D, false)* hero.getSkillLevel(skill)));
							int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 1000, false);
							
							RavingSliceEffect rEffect = new RavingSliceEffect(skill, duration, damageperc);
							hero.addEffect(rEffect);
							Messaging.send(hero.getPlayer(), "You are now under the effect of " + ChatColor.WHITE + "RavingSlice" + ChatColor.GRAY + "! Your next blade attack will deal more damage!");
						}
					}
				},warm);				
				sneakingplayers.put(hero.getPlayer(), id);
			}
		}
		
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(event.getDamager() instanceof Hero)
			{
				Hero hero = (Hero) event.getDamager();
					
				if(hero.hasAccessToSkill(skill) && hero.hasEffect("RavingSliceEffect"))
				{
					//Reminder - Maybe do a check if the hero is wielding a blade?
					RavingSliceEffect rEffect = (RavingSliceEffect) hero.getEffect("RavingSliceEffect");
					
					double damageperc = rEffect.getDamagePercent() + 1;
					event.setDamage(event.getDamage()*damageperc);
					
					hero.removeEffect(rEffect);
				}
			}
		}
	}
	
	public class RavingSliceEffect extends ExpirableEffect
	{
		private final double damageperc;
		
		public RavingSliceEffect(SkillRavingSlice skill, int duration, double damageperc)
		{
			super(skill, "RavingSliceEffect", duration);
			this.damageperc=damageperc;
		}
		
		public double getDamagePercent()
		{
			return this.damageperc;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You are no longer under the effect of " + ChatColor.WHITE + "RavingSlice" + ChatColor.GRAY + "!");
		}
	}
}
