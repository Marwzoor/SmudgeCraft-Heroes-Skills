package me.marwzoor.skillstormarrow;

import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillStormArrow extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillStormArrow skill;
	
	public SkillStormArrow(Heroes instance)
	{
		super(instance, "StormArrow");
		plugin=instance;
		skill=this;
		setDescription("The next arrow you fire sends a lightning bolt on your opponent for $1 damage. You have $2 seconds to fire your arrow.");
		setUsage("/skill stormarrow");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill stormarrow" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT});
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
		String desc = super.getDescription();
		double duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		double damage = SkillConfigManager.getUseSetting(hero, skill, "damage", Integer.valueOf(120), false);
		damage += SkillConfigManager.getUseSetting(hero, skill, "damage-increase", Integer.valueOf(2), false) * hero.getSkillLevel(skill);
		duration = duration/1000;
		desc = desc.replace("$1", damage + "");
		desc = desc.replace("$2", duration + "");
		return desc;
		}
		else
		{
			return super.getDescription().replace("$1", "X").replace("$2", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(30000));
		node.set("damage", Integer.valueOf(120));
		node.set("damage-increase", Integer.valueOf(2));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		double duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		double damage = SkillConfigManager.getUseSetting(hero, skill, "damage", Integer.valueOf(120), false);
		damage += SkillConfigManager.getUseSetting(hero, skill, "damage-increase", Integer.valueOf(2), false) * hero.getSkillLevel(skill);
		
		if(hero.hasEffect("StormArrow"))
		{
			hero.removeEffect(hero.getEffect("StormArrow"));
		}
		
		StormArrowEffect sEffect = new StormArrowEffect(skill, (int) duration, hero.getPlayer(), (int) damage);
		
		hero.addEffect(sEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "StormArrow" + ChatColor.GRAY + "! Your next arrow will bring lightning on to your opponent!", new Object());
		
		return SkillResult.NORMAL;
	}
	
	public class StormArrowEffect extends ImbueEffect
	{
		private int damage;
		public StormArrowEffect(Skill skill, int duration, Player player, int damage)
		{
			super(skill, "StormArrow", duration, player);
			this.damage=damage;
		}
		
		public int getDamage()
		{
			return this.damage;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "StormArrow" + ChatColor.GRAY + "!", new Object());
		}
	}
	
	public class SkillHeroListener implements Listener
	{
		
		@EventHandler
		public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event)
		{
			if(event.getDamager() instanceof Arrow)
			{
				Arrow arrow = (Arrow) event.getDamager();
				if(arrow.getShooter() instanceof Player)
				{
					Player player = (Player) arrow.getShooter();
					Hero hero = plugin.getCharacterManager().getHero(player);
					
					if(hero.hasEffect("StormArrow"))
					{
						StormArrowEffect sEffect = (StormArrowEffect) hero.getEffect("StormArrow");
						event.getEntity().getWorld().strikeLightningEffect(event.getEntity().getLocation());
						event.setDamage(sEffect.getDamage());
						
						hero.removeEffect(sEffect);
					}
				}
			}
		}
	}
}
