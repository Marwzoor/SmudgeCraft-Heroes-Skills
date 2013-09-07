package me.marwzoor.skilldarkwhip;

import net.smudgecraft.heroeslib.whip.WhipDamageEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillDarkWhip extends ActiveSkill
{
	public static SkillDarkWhip skill;	
	
	public SkillDarkWhip(Heroes instance)
	{
		super(instance, "DarkWhip");
		skill=this;
		setDescription("Your next fling with your whip instantly kills a monster, or deals $X damage and cause confusion for $Y seconds to an enemy. M: $1 CD: $2");
		setUsage("/skill darkwhip");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill darkwhip" });
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new DarkWhipLinstener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 60, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 1, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, "confusion-duration", 5000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "confusion-duration-increase", 1, false) * hero.getSkillLevel(skill)));
			duration = duration / 1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", damage + "").replace("$Y", duration + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(), 60);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 1);
		node.set("confusion-duration", 5000);
		node.set("confusion-duration-increase", 1);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 60, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 1, false) * hero.getSkillLevel(skill)));
		double duration = (SkillConfigManager.getUseSetting(hero, skill, "confusion-duration", 5000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "confusion-duration-increase", 1, false) * hero.getSkillLevel(skill)));
		hero.addEffect(new DarkWhipEffect(this, duration, damage));
		return SkillResult.NORMAL;
	}
	
	public class DarkWhipEffect extends Effect
	{
		public final PotionEffect pe;
		public final double damage;
		
		public DarkWhipEffect(Skill skill, double duration, double damage)
		{
			super(skill, "DarkWhip");
			pe = new PotionEffect(PotionEffectType.CONFUSION, (int) (duration/1000*20), 0);
			this.damage = damage;
		}
		
		@Override
		public void applyToHero(Hero hero)
		{
			Messaging.send(hero.getPlayer(), "Your next attack with your whip will fling darkness at your target!");
			super.applyToHero(hero);
		}
		
		@Override
		public void applyToMonster(Monster monster)
		{
			super.applyToMonster(monster);
		}
		
		@Override
		public void removeFromHero(Hero hero)
		{
			Messaging.send(hero.getPlayer(), "Your will no longer fling darkness with your next attack!");
			super.removeFromHero(hero);
		}
	}
	
	public class DarkWhipLinstener implements Listener
	{
		@EventHandler(priority = EventPriority.HIGH)
		public void onWhipDamageEvent(WhipDamageEvent event)
		{
			Hero hero = plugin.getCharacterManager().getHero(event.getAttacker());
			if(!hero.hasEffect("DarkWhip"))
				return;
			DarkWhipEffect dwe = (DarkWhipEffect)hero.getEffect("DarkWhip");
			if(event.getTarget() instanceof Player)
			{
				event.getTarget().addPotionEffect(dwe.pe);
				event.setDamage(dwe.damage);
				skill.broadcast(event.getTarget().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() +
						ChatColor.GRAY + " flings darkness with his whip at " + ChatColor.DARK_RED + ((Player)event.getTarget()).getName() + ChatColor.GRAY + " !");
			}
			else
			{
				event.setDamage(((Damageable)event.getTarget()).getHealth());
			}
			skill.broadcast(event.getTarget().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() +
					ChatColor.GRAY + " flings darkness with his whip at " + ChatColor.DARK_GREEN + event.getTarget().getType().getName() + ChatColor.GRAY + " !");
			hero.removeEffect(dwe);
		}
	}
}
