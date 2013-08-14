package me.marwzoor.skillsever;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillSever extends TargettedSkill
{
	public SkillSever(Heroes instance)
	{
		super(instance, "Sever");
		setDescription("You sever a limb from your enemy, dealing %1 damage and making them bleed for %2 seconds. DMG: %3 D: %4 M: %5 CD: %6 R: %7");
		setUsage("/skill sever");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill sever" });
		setTypes(new SkillType[] { SkillType.DAMAGING, SkillType.HARMFUL });
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(plugin), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			double duration = (((double)SkillConfigManager.getUseSetting(hero, this, "bleed-duration", 7000, false))/1000);
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000);
			double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 100, false) + 
					(SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0.1, false)*hero.getSkillLevel(this)));
			int range = SkillConfigManager.getUseSetting(hero, this, "range", 6, false);
			return super.getDescription().replace("%1", damage + "").replace("%2", duration + "s").replace("%3", damage + "").replace("%4", duration + "s").replace("%5", mana + "").replace("%6", cooldown + "s").replace("%7", range + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "X").replace("%3", "X").replace("%4", "Xs").replace("%5", "X").replace("%6", "Xs").replace("%7", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("bleed-duration", 30000);
		node.set("period", 1000);
		node.set("tick-damage", 20);
		node.set(SkillSetting.MANA.node(), 0);
		node.set(SkillSetting.COOLDOWN.node(), 0);
		node.set("range", 5);
		return node;
	}
	
	public SkillResult use(Hero hero, LivingEntity target, String[] args)
	{
		if(target.getLocation().distance(hero.getPlayer().getLocation())>SkillConfigManager.getUseSetting(hero, this, "range", 6, false))
		{
			Messaging.send(hero.getPlayer(), "That target is too far away!");
			return SkillResult.FAIL;
		}
		
		if(target instanceof Player && ((Player) target).getName().equalsIgnoreCase(hero.getPlayer().getName()))
		{
			Messaging.send(hero.getPlayer(), "You can't target yourself!");
			return SkillResult.INVALID_TARGET_NO_MSG;
		}
		
		double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 100, false) + 
				(SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0.1, false)*hero.getSkillLevel(this)));
		
		this.addSpellTarget(target, hero);
		Skill.damageEntity(target, hero.getPlayer(), damage, DamageCause.MAGIC);
		target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);

		double tickdamage = SkillConfigManager.getUseSetting(hero, this, "tick-damage", 20D, false);
		int period = SkillConfigManager.getUseSetting(hero, this, "period", 1000, false);
		int duration = SkillConfigManager.getUseSetting(hero, this, "bleed-duration", 7000, false);
		
		SeverEffect sEffect = new SeverEffect(this, period, duration, tickdamage, hero.getPlayer());
		
		CharacterTemplate ctarget = plugin.getCharacterManager().getCharacter(target);
		
		if(ctarget.hasEffect("SeverEffect"))
		{
			ctarget.removeEffect(ctarget.getEffect("SeverEffect"));
		}
		
		ctarget.addEffect(sEffect);
		
		if(target instanceof Player)
		{
			this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skills" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.WHITE + " Severed " + ChatColor.GRAY + ((Player) target).getName() + "'s limbs!");
		}
		else
		{
			Messaging.send(hero.getPlayer(), "You " + ChatColor.WHITE + "Severed" + ChatColor.GRAY + " a " + target.getType().getName() + "'s limbs!");
		}
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		public Heroes plugin;
		
		public SkillHeroListener(Heroes plugin)
		{
			this.plugin=plugin;
		}
		
		@EventHandler
		public void onEntityDeath(EntityDeathEvent event)
		{	
			CharacterTemplate character = plugin.getCharacterManager().getCharacter(event.getEntity());
			
			if(character.hasEffect("SeverEffect"))
			{
				character.removeEffect(character.getEffect("SeverEffect"));
			}
		}
	}
	
	public class SeverEffect extends PeriodicDamageEffect
	{
		public SeverEffect(SkillSever skill, int period, int duration, double tickDamage, Player applier)
		{
			super(skill, "SeverEffect", period, duration, tickDamage, applier);
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
		}
		
		public void applyToMonster(Monster monster)
		{
			super.applyToMonster(monster);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You are no longer " + ChatColor.WHITE + "Severed" + ChatColor.GRAY + "!");
		}
		
		public void tickMonster(Monster monster)
		{
			super.tickMonster(monster);
			if(!monster.getEntity().isDead() && monster.hasEffect("SeverEffect"))
			{
				monster.getEntity().getWorld().playEffect(monster.getEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
			}
		}
		
		public void tickHero(Hero hero)
		{
			super.tickHero(hero);
			if(!hero.getPlayer().isDead() && hero.hasEffect("SeverEffect"))
			{
				hero.getEntity().getWorld().playEffect(hero.getEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
			}
		}
	}
}
