package me.marwzoor.skillfrostarrow;

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
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillFrostArrow extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillFrostArrow skill;
	
	public SkillFrostArrow(Heroes instance)
	{
		super(instance, "FrostArrow");
		plugin=instance;
		skill=this;
		setDescription("The next arrow you fire slows your opponent for $1 seconds. You have $2 seconds to fire your arrow.");
		setUsage("/skill frostarrow");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill frostarrow" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT});
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
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
		else
		{
			return super.getDescription().replace("$1", "X").replace("$2", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(30000));
		node.set("slow-duration", Integer.valueOf(2000));
		node.set("slow-duration-increase", Integer.valueOf(10));
		node.set("slow-multiplier", Integer.valueOf(2));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		int slowduration = SkillConfigManager.getUseSetting(hero, skill, "slow-duration", Integer.valueOf(3000), false);
		slowduration += SkillConfigManager.getUseSetting(hero, skill, "slow-duration-increase", Integer.valueOf(10), false) * hero.getSkillLevel(skill);
		int multiplier = SkillConfigManager.getUseSetting(hero, skill, "slow-multiplier", Integer.valueOf(2), false);
		
		if(hero.hasEffect("FrostArrow"))
		{
			hero.removeEffect(hero.getEffect("FrostArrow"));
		}
		
		FrostArrowEffect fEffect = new FrostArrowEffect(skill, duration, hero.getPlayer(), slowduration, multiplier);
		
		hero.addEffect(fEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "FrostArrow" + ChatColor.GRAY + "! Your next arrow will slow your opponent!", new Object());
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onEntityShot(ImbueArrowHitEvent event)
		{
			if(event.getImbueEffect() instanceof FrostArrowEffect)
			{
				FrostArrowEffect fEffect = (FrostArrowEffect) event.getImbueEffect();
				
				SlowEffect sEffect = null;
				
				if(event.getShotEntity() instanceof Player)
				{
					sEffect = new SlowEffect(skill, "Slow", fEffect.getSlowDuration(), 2, true, ((Player) event.getShotEntity()).getDisplayName() + " has been slowed by " +  fEffect.getPlayer().getDisplayName() + ChatColor.WHITE + "!", ((Player) event.getShotEntity()).getDisplayName() + " is no longer slowed!", plugin.getCharacterManager().getHero(fEffect.getPlayer()));
				}
				else
				{
					sEffect = new SlowEffect(skill, "Slow", fEffect.getSlowDuration(), 2, true, ChatColor.WHITE + event.getShotEntity().getType().getName() + ChatColor.GRAY + " has been slowed by " +  fEffect.getPlayer().getDisplayName() + ChatColor.WHITE + "!", ChatColor.WHITE + event.getShotEntity().getType().getName() + ChatColor.GRAY + " is no longer slowed!", plugin.getCharacterManager().getHero(fEffect.getPlayer()));
				}
				CharacterTemplate ct = plugin.getCharacterManager().getCharacter(event.getShotEntity());
				
				ct.addEffect(sEffect);
				
				if(fEffect.getPlayer()!=null)
				{
					Hero hero = plugin.getCharacterManager().getHero(fEffect.getPlayer());
					
					hero.removeEffect(fEffect);
				}
			}
		}
	}
	
	
	public class FrostArrowEffect extends ImbueEffect
	{
		private int slowduration;
		private int multiplier;
		
		public FrostArrowEffect(Skill skill, int duration, Player player, int slowduration, int multiplier)
		{
			super(skill, "FrostArrow", duration, player);
			this.slowduration=slowduration;
			this.multiplier=multiplier;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "FrostArrow" + ChatColor.GRAY + "!", new Object());
		}
		
		public int getSlowDuration()
		{
			return this.slowduration;
		}
		
		public int getSlowMultiplier()
		{
			return this.multiplier;
		}
	}
}
