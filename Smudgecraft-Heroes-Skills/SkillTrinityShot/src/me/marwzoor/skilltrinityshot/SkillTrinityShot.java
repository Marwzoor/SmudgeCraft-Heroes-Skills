package me.marwzoor.skilltrinityshot;

import net.smudgecraft.companions.events.ImbueArrowLaunchEvent;
import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillTrinityShot extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillTrinityShot skill;
	
	public SkillTrinityShot(Heroes instance)
	{
		super(instance, "TrinityShot");
		plugin=instance;
		skill=this;
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill trinityshot", "skill trishot", "skill tripleshot" });
		setDescription("You fire three arrows instead of one next shot.");
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(30000));
		node.set(SkillSetting.REAGENT.node(), Integer.valueOf(262));
		node.set(SkillSetting.REAGENT_COST.node(), Integer.valueOf(2));
		return node;
	}
	
	public String getDescription(Hero hero)
	{
		return super.getDescription();
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, Integer.valueOf(30000), false);
		
		if(hero.hasEffect("TrinityShot"))
		{
			hero.removeEffect(hero.getEffect("TrinityShot"));
		}
		
		TrinityShotEffect dsEffect = new TrinityShotEffect(skill, duration, hero.getPlayer());
		
		hero.addEffect(dsEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "TrinityShot" + ChatColor.GRAY + "! You will shoot three arrows on your next shot!", new Object());
		
		return SkillResult.NORMAL;
	}
	
	public class TrinityShotEffect extends ImbueEffect
	{
		public TrinityShotEffect(Skill skill, int duration, Player player)
		{
			super(skill, "TrinityShot", duration, player);
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "TrinityShot" + ChatColor.GRAY + "!", new Object());
		}
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onPlayerShootArrowEvent(ImbueArrowLaunchEvent event)
		{
			if(event.getImbueEffect() instanceof TrinityShotEffect)
			{
				final TrinityShotEffect dEffect = (TrinityShotEffect) event.getImbueEffect();
				final Arrow arrow = event.getArrow();
				final Vector vec = arrow.getVelocity();
				plugin.getCharacterManager().getHero(dEffect.getPlayer()).removeEffect(dEffect);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
						Arrow ar = dEffect.getPlayer().launchProjectile(Arrow.class);
						ar.setVelocity(vec);
					}
				}, 10L);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
						Arrow ar = dEffect.getPlayer().launchProjectile(Arrow.class);
						ar.setVelocity(vec);
					}
				}, 20L);
			}
		}
	}
}
