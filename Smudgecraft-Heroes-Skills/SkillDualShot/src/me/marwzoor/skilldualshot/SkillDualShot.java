package me.marwzoor.skilldualshot;

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

public class SkillDualShot extends ActiveSkill
{
	public static Heroes plugin;
	
	public SkillDualShot(Heroes instance)
	{
		super(instance, "DualShot");
		plugin=instance;
		setIdentifiers(new String[] { "skill dualshot" });
		setDescription("You fire two arrows instead of one next shot. D:%1s");
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
		if (hero.hasAccessToSkill(this)) {
			String desc = super.getDescription();
			int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(30000), false) / 1000;
			return desc.replace("%1", duration + "");
		} else {
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(30000), false);
		
		if(hero.hasEffect("DualShot"))
		{
			hero.removeEffect(hero.getEffect("DualShot"));
		}
		
		DualShotEffect dsEffect = new DualShotEffect(this, duration, hero.getPlayer());
		
		hero.addEffect(dsEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "DualShot" + ChatColor.GRAY + "! You will shoot two arrows on your next shot!", new Object());
		
		return SkillResult.NORMAL;
	}
	
	public class DualShotEffect extends ImbueEffect
	{
		public DualShotEffect(Skill skill, int duration, Player player)
		{
			super(skill, "DualShot", duration, player);
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "DualShot" + ChatColor.GRAY + "!", new Object());
		}
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onPlayerShootArrowEvent(ImbueArrowLaunchEvent event)
		{
			if(event.getImbueEffect() instanceof DualShotEffect)
			{
				final DualShotEffect dEffect = (DualShotEffect) event.getImbueEffect();
				final Arrow arrow = event.getArrow();
				final Vector vec = arrow.getVelocity();
				plugin.getCharacterManager().getHero(dEffect.getPlayer()).removeEffect(dEffect);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
				{
					public void run()
					{
						Arrow ar = dEffect.getPlayer().launchProjectile(Arrow.class);
						ar.setCritical(arrow.isCritical());
						ar.setVelocity(vec);
						ar.setDamage(arrow.getDamage());
					}
				}, 10L);
			}
		}
	}
}
