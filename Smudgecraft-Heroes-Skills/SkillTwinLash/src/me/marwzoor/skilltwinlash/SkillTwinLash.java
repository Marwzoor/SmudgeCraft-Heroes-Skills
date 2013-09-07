package me.marwzoor.skilltwinlash;

import net.smudgecraft.heroeslib.HeroesLib;
import net.smudgecraft.heroeslib.whip.WhipDamageEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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

public class SkillTwinLash extends ActiveSkill
{
	public static SkillTwinLash skill;	
	
	public SkillTwinLash(Heroes instance)
	{
		super(instance, "TwinLash");
		skill=this;
		setDescription("Your lash with your whip will hit your enemy twice, dealing $X for every lash. M: $1 CD: $2");
		setUsage("/skill twinlash");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill twinlash" });
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new TwinLashLinstener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 60, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 1, false) * hero.getSkillLevel(skill)));
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", damage + "").replace("$1", mana + "").replace("$2", cd + "");
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
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 60, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 1, false) * hero.getSkillLevel(skill)));
		hero.addEffect(new TwinLashEffect(this, damage));
		return SkillResult.NORMAL;
	}
	
	public class TwinLashEffect extends Effect
	{
		public final double damage;
		
		public TwinLashEffect(Skill skill, double damage)
		{
			super(skill, "TwinLash");
			this.damage = damage;
		}
		
		@Override
		public void applyToHero(Hero hero)
		{
			Messaging.send(hero.getPlayer(), "Your next attack with your whip will be a Twin Lash!");
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
			Messaging.send(hero.getPlayer(), "Your next attack will no longer be a Twin Lash!");
			super.removeFromHero(hero);
		}
	}
	
	public class TwinLashLinstener implements Listener
	{
		@EventHandler(priority = EventPriority.HIGH)
		public void onWhipDamageEvent(final WhipDamageEvent event)
		{
			final Hero hero = plugin.getCharacterManager().getHero(event.getAttacker());
			if(!hero.hasEffect("TwinLash"))
				return;
			event.setCancelled(true);
			final TwinLashEffect twe = (TwinLashEffect)hero.getEffect("TwinLash");
			if(event.getTarget() instanceof Player)
			{
				((LivingEntity)event.getTarget()).setLeashHolder(hero.getPlayer());
				damageEntity((LivingEntity)event.getTarget(), hero.getPlayer(), twe.damage);
				Bukkit.getScheduler().scheduleSyncDelayedTask(HeroesLib.plugin, new Runnable()
				{
					@Override
					public void run()
					{
						((LivingEntity)event.getTarget()).setLeashHolder(null);
						Bukkit.getScheduler().scheduleSyncDelayedTask(HeroesLib.plugin, new Runnable()
						{
							@Override
							public void run()
							{
								((LivingEntity)event.getTarget()).setLeashHolder(hero.getPlayer());
								damageEntity((LivingEntity)event.getTarget(), hero.getPlayer(), twe.damage);
								Bukkit.getScheduler().scheduleSyncDelayedTask(HeroesLib.plugin, new Runnable()
								{
									@Override
									public void run()
									{
										((LivingEntity)event.getTarget()).setLeashHolder(null);
									}
								},5L);
							}
						},1L);
						
					}
				},5L);
				skill.broadcast(event.getTarget().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() +
						ChatColor.GRAY + " performed a " + ChatColor.WHITE + "TwinLash" + ChatColor.GRAY + " on " + ChatColor.DARK_RED + 
						((Player)event.getTarget()).getName() + ChatColor.GRAY + "!");
			}
			else
			{
				event.setDamage(((Damageable)event.getTarget()).getHealth());
				skill.broadcast(event.getTarget().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() +
						ChatColor.GRAY + " performed a " + ChatColor.WHITE + "TwinLash" + ChatColor.GRAY + " on " + 
						ChatColor.DARK_GREEN + event.getTarget().getType().getName() + ChatColor.GRAY + "!");
			}
			
			hero.removeEffect(twe);
		}
	}
}
