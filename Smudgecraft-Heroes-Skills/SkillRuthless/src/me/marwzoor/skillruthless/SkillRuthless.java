package me.marwzoor.skillruthless;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.CharacterDamageEvent;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillRuthless extends ActiveSkill
{
	public static SkillRuthless skill;
	public SkillRuthless(Heroes instance)
	{
		super(instance, "Ruthless");
		skill=this;
		setDescription("Allies under the effect of Morality gains a passive %1 chance to stun their opponent using melee. C: %1 M: %2 CD: %3 D: %4");
		setUsage("/skill ruthless");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill ruthless" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.INTERRUPT });
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int chance = (int) ((SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE, 0.1D, false) +
					(SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL, 0.005D, false) * hero.getSkillLevel(this)))*100);
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000;
			int stunduration = SkillConfigManager.getUseSetting(hero, this, "stun-duration", 1000, false)/1000;
			
			return super.getDescription().replace("%1", chance + "%").replace("%2", mana + "").replace("%3", cooldown + "s").replace("%4", stunduration + "s");
		}
		else
		{
			return super.getDescription().replace("%1", "X%").replace("%2", "X").replace("%3", "Xs").replace("%4", "Xs");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("tick-damage", 60);
		node.set("period", 1000);
		node.set("bleed-duration", 5000);
		node.set(SkillSetting.DURATION.node(), 10000);
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int tickdamage = SkillConfigManager.getUseSetting(hero, this, "tick-damage", 60, false);
		int period = SkillConfigManager.getUseSetting(hero, this, "period", 1000, false);
		int bleedduration = SkillConfigManager.getUseSetting(hero, this, "bleed-duration", 5000, false);
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
		
		RuthlessEffect rEffect = new RuthlessEffect(this, duration, tickdamage, bleedduration, period);
		
		if(hero.hasEffect("Ruthless"))
		{
			hero.removeEffect(hero.getEffect("Ruthless"));
		}
		
		hero.addEffect(rEffect);
		
		return SkillResult.NORMAL;
	}
	
	public class BleedEffect extends PeriodicDamageEffect
	{		
		public BleedEffect(Skill skill, int period, int duration, int tickdamage, Player applier, boolean bool)
		{
			super(skill, "BleedRuthless", period, duration, tickdamage, applier, bool);
		}
		
		public BleedEffect(Skill skill, int period, int duration, int tickdamage, Player applier)
		{
			super(skill, "BleedRuthless", period, duration, tickdamage, applier);
		}
		
		public void applyToHero(final Hero hero)
		{
			super.applyToHero(hero);
		}

		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
		}

		public void applyToMonster(Monster monster)
		{
			super.applyToMonster(monster);
		}

		public void removeFromMonster(Monster monster)
		{
			super.removeFromMonster(monster);
		}
	}
	
	public class RuthlessEffect extends ExpirableEffect
	{
		private int bleeddamage;
		private int bleedduration;
		private int bleedperiod;
		
		public RuthlessEffect(Skill skill, int duration, int bleeddamage, int bleedduration, int bleedperiod)
		{
			super(skill, "Ruthless", duration);
			this.bleeddamage=bleeddamage;
			this.bleedduration=bleedduration;
			this.bleedperiod=bleedperiod;
		}
		
		public int getBleedDamage()
		{
			return this.bleeddamage;
		}
		
		public int getBleedDuration()
		{
			return this.bleedduration;
		}
		
		public int getBleedPeriod()
		{
			return this.bleedperiod;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			skill.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.WHITE + hero.getPlayer().getName() + ChatColor.GRAY + " is no longer " + ChatColor.WHITE + "Ruthless" + ChatColor.GRAY + "!");
		}
	}
	
	public class SkillHeroListener implements Listener
	{
		public SkillRuthless skill;
		
		public SkillHeroListener(SkillRuthless skill)
		{
			this.skill=skill;
		}
		
		@EventHandler
		public void onCharacterDamageEvent(CharacterDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(event.getEntity() instanceof Player)
			{
				if(plugin.getCharacterManager().getHero((Player)event.getEntity()).hasEffect("Ruthless"))
				{
					event.setCancelled(true);
				}
			}
		}
		
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(!(event.getEntity() instanceof LivingEntity))
				return;
			
			if(event.getEntity() instanceof Player)
			{
				if(plugin.getCharacterManager().getHero((Player)event.getEntity()).hasEffect("Ruthless"))
				{
					event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ITEM_BREAK, 10, 0f);
					event.setCancelled(true);
					return;
				}
			}
			
			if(event.getAttackerEntity() instanceof Player)
			{
				Hero hero = plugin.getCharacterManager().getHero((Player) event.getAttackerEntity());
				
				if(hero.hasEffect("Ruthless"))
				{
					RuthlessEffect rEffect = (RuthlessEffect) hero.getEffect("Ruthless");
					
					BleedEffect pEffect = new BleedEffect(skill, rEffect.getBleedPeriod(), rEffect.getBleedDuration(), rEffect.getBleedDamage(), hero.getPlayer(), true);

					CharacterTemplate ct = plugin.getCharacterManager().getCharacter((LivingEntity) event.getEntity());
					
					if(ct.hasEffect("BleedRuthless"))
					{
						ct.removeEffect(ct.getEffect("BleedRuthless"));
					}
					
					ct.addEffect(pEffect);
					
					event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
				}
			}
		}
	}
}
