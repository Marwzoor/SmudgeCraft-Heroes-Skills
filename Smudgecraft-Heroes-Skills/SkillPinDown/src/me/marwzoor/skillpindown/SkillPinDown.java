package me.marwzoor.skillpindown;

import net.smudgecraft.companions.events.ImbueArrowHitEvent;
import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillPinDown extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillPinDown skill;
	
	public SkillPinDown(Heroes instance)
	{
		super(instance, "PinDown");
		plugin=instance;
		skill=this;
		setDescription("The next arrow you fire stuns your opponent for $1 seconds. You have $2 seconds to fire your arrow.");
		setUsage("/skill pindown");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill pindown" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.INTERRUPT});
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
		double duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		double stunduration = SkillConfigManager.getUseSetting(hero, skill, "stun-duration", Integer.valueOf(2000), false);
		stunduration += SkillConfigManager.getUseSetting(hero, skill, "stun-duration-increase", Integer.valueOf(10), false) * hero.getSkillLevel(skill);
		stunduration = stunduration/1000;
		duration = duration/1000;
		desc = desc.replace("$1", stunduration + "");
		desc = desc.replace("$2", duration + "");
		return desc;
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(30000));
		node.set("stun-duration", Integer.valueOf(2000));
		node.set("stun-duration-increase", Integer.valueOf(10));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		int stunduration = SkillConfigManager.getUseSetting(hero, skill, "stun-duration", Integer.valueOf(2000), false);
		stunduration += SkillConfigManager.getUseSetting(hero, skill, "stun-duration-increase", Integer.valueOf(10), false) * hero.getSkillLevel(skill);
		
		if(hero.hasEffect("PinDown"))
		{
			hero.removeEffect(hero.getEffect("PinDown"));
		}
		
		PinDownEffect pdEffect = new PinDownEffect(skill, duration, stunduration, hero.getPlayer());
		
		hero.addEffect(pdEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "PinDown" + ChatColor.GRAY + "! Your next arrow will stun your opponent!", new Object());
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{	
		@EventHandler
		public void onEntityShotEvent(ImbueArrowHitEvent event)
		{
			if(event.getImbueEffect() instanceof PinDownEffect)
			{
				PinDownEffect pde = (PinDownEffect) event.getImbueEffect();
								
				if(event.getShotEntity() instanceof Player)
				{
					Player tplayer = (Player) event.getShotEntity();
					Hero hero = plugin.getCharacterManager().getHero(tplayer);
					
					hero.addEffect(new StunEffect(skill, pde.getStunDuration()));
				}
				else
				{
					if(pde.getPlayer() != null)
					{
					CharacterTemplate ct = plugin.getCharacterManager().getCharacter(event.getShotEntity());

					ct.addEffect(new SlowEffect(skill, pde.getStunDuration(), 2, true, Messaging.getLivingEntityName(event.getShotEntity()) + " has been slowed by " + pde.getPlayer().getDisplayName(), Messaging.getLivingEntityName(event.getShotEntity()) + " is no longer slowed by " + pde.getPlayer().getDisplayName(), plugin.getCharacterManager().getHero(pde.getPlayer())));
					}
				}
				
				if(pde.getPlayer() !=null)
				{
					Hero hero = plugin.getCharacterManager().getHero(pde.getPlayer());
					
					hero.removeEffect(pde);
				}
			}
		}
	}
	
	public class PinDownEffect extends ImbueEffect
	{
		int stunduration;
		
		public PinDownEffect(Skill skill, int duration, int stunduration, Player player)
		{
			super(skill, "PinDown", duration, player);
			this.stunduration=stunduration;
		}

		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "PinDown" + ChatColor.GRAY + "!", new Object());
		}
		
		public int getStunDuration()
		{
			return this.stunduration;
		}
	}
}
