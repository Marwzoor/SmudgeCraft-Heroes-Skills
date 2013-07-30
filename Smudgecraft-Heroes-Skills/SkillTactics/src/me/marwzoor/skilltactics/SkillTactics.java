package me.marwzoor.skilltactics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillTactics extends ActiveSkill
{
	public static SkillTactics skill;
	
	public SkillTactics(Heroes instance)
	{
		super(instance, "Tactics");
		skill=this;
		setDescription("Allies under the effect of Morality gains a passive %1 chance to stun their opponent using melee. C: %1 M: %2 CD: %3 D: %4");
		setUsage("/skill tactics");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill tactics" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.INTERRUPT });
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
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
		node.set(SkillSetting.CHANCE.node(), 0.1D);
		node.set(SkillSetting.CHANCE_LEVEL.node(), 0.005D);
		node.set("stun-duration", 1000);
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		if(hero.hasParty())
		{
			boolean hasEffect=false;
			for(Hero h : hero.getParty().getMembers())
			{
				if(!h.equals(hero))
				{
					if(h.hasEffect("Morality"))
					{
						ExpirableEffect effect = (ExpirableEffect) h.getEffect("Morality");
					
						int stunduration = SkillConfigManager.getUseSetting(hero, this, "stun-duration", 1000, false);
						double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE, 0.1D, false) +
								(SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL, 0.005D, false) * hero.getSkillLevel(this)));
						int duration = (int) effect.getRemainingTime();
					
						TacticsEffect tEffect = new TacticsEffect(skill, duration, chance, stunduration);
						
						h.addEffect(tEffect);
						Messaging.send(h.getPlayer(), "You are now under the effect of " + ChatColor.WHITE + "Tactics" + ChatColor.GRAY + "! You have a chance of stunning your opponent with melee hits!");
						hasEffect=true;
					}
				}
				else
				{
					if(h.hasEffect("Morality"))
					{
						ExpirableEffect effect = (ExpirableEffect) h.getEffect("Morality");
					
						int stunduration = SkillConfigManager.getUseSetting(hero, this, "stun-duration", 1000, false);
						double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE, 0.1D, false) +
								(SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL, 0.005D, false) * hero.getSkillLevel(this)));
						int duration = (int) effect.getRemainingTime();
					
						TacticsEffect tEffect = new TacticsEffect(skill, duration, chance, stunduration);
						
						h.addEffect(tEffect);
						Messaging.send(h.getPlayer(), "You used " + ChatColor.WHITE + "Tactics" + ChatColor.GRAY + "! All allies under the effect of morality now have a chance to stun their opponent with melee hits!");
						hasEffect=true;
					}
				}
			}
			
			if(!hasEffect)
			{
				Messaging.send(hero.getPlayer(), "There was no one in your party that had the effect of " + ChatColor.WHITE + "Morality" + ChatColor.GRAY + "!");
			}
			
			return SkillResult.NORMAL;
		}
		else
		{
			if(hero.hasEffect("Morality"))
			{
				ExpirableEffect effect = (ExpirableEffect) hero.getEffect("Morality");
			
				int stunduration = SkillConfigManager.getUseSetting(hero, this, "stun-duration", 1000, false);
				double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE, 0.1D, false) +
						(SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL, 0.005D, false) * hero.getSkillLevel(this)));
				int duration = (int) effect.getRemainingTime();
			
				TacticsEffect tEffect = new TacticsEffect(skill, duration, chance, stunduration);
				
				hero.addEffect(tEffect);
				Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "Tactics" + ChatColor.GRAY + "! You now have a chance to stun their opponent with melee hits!");
				
				return SkillResult.NORMAL;
			}
			else
			{
				Messaging.send(hero.getPlayer(), "You don't have" + ChatColor.WHITE + " Morality" + ChatColor.GRAY + "!");
				return SkillResult.FAIL;
			}
		}
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(event.isProjectile()==false)
			{
				if(event.getDamager() instanceof Hero)
				{
					Hero hero = (Hero) event.getDamager();
					
					if(hero.hasEffect("Tactics"))
					{
						TacticsEffect tEffect = (TacticsEffect) hero.getEffect("Tactics");
						
						if(Math.random()<=tEffect.getChance())
						{
							if(event.getEntity() instanceof Player)
							{
								Hero h = plugin.getCharacterManager().getHero((Player) event.getEntity());
								
								StunEffect sEffect = new StunEffect(skill, (long) tEffect.getStunDuration());
								
								h.addEffect(sEffect);
							}
						}
					}
				}
			}
		}
	}
	
	public class TacticsEffect extends ExpirableEffect
	{
		private double chance;
		private int stunduration;
		
		public TacticsEffect(Skill skill, int duration, double chance, int stunduration)
		{
			super(skill, "Tactics", duration);
			this.chance=chance;
			this.stunduration=stunduration;
		}
		
		public double getChance()
		{
			return this.chance;
		}
		
		public double getStunDuration()
		{
			return this.stunduration;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "The effect of" + ChatColor.WHITE + " Tactics " + ChatColor.GRAY + "has worn out.");
		}
	}
}
