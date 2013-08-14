package me.marwzoor.skillrollout;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.CharacterDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillRollout extends ActiveSkill
{
	public SkillRollout(Heroes instance)
	{
		super(instance, "Rollout");
		setDescription("You roll out of all falls for %1 seconds, avoiding all fall damage. D: %2 M: %3 CD: %4");
		setUsage("/skill rollout");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill rollout" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT, SkillType.COUNTER });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this, plugin), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int duration = ((SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 20000, false) +
					(SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(this)))/1000);
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000);
			
			return super.getDescription().replace("%1", duration + "").replace("%2", duration + "s").replace("%3", mana + "").replace("%4", cooldown + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "Xs").replace("%3", "X").replace("%4", "Xs");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 20000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 100);
		node.set(SkillSetting.MANA.node(), 0);
		node.set(SkillSetting.COOLDOWN.node(), 0);
		return node;
	}
	
	public SkillResult use(final Hero hero, String[] args)
	{
		int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 20000, false) +
				(SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(this)));
		
		if(hero.hasEffect("RolloutEffect"))
			hero.removeEffect(hero.getEffect("RolloutEffect"));
		
		RolloutEffect rEffect = new RolloutEffect(this, duration);
				
		this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "Rollout!");
		hero.addEffect(rEffect);
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		public Heroes plugin;
		public SkillRollout skill;
		
		public SkillHeroListener(SkillRollout skill, Heroes plugin)
		{
			this.skill=skill;
			this.plugin=plugin;
		}
		
		@EventHandler
		public void onCharacterDamageEvent(CharacterDamageEvent event)
		{
			if(event.isCancelled() || !event.getCause().equals(DamageCause.FALL) || !(event.getEntity() instanceof Player))
				return;
			
			Hero hero = plugin.getCharacterManager().getHero((Player) event.getEntity());
			
			if(!hero.hasEffect("RolloutEffect") || !hero.hasAccessToSkill(skill))
				return;
			
			hero.getPlayer().sendMessage(ChatColor.GRAY + "You roll graciously!");
			event.setCancelled(true);
		}
	}
	
	public class RolloutEffect extends ExpirableEffect
	{
		public RolloutEffect(SkillRollout skill, int duration)
		{
			super(skill, "RolloutEffect", duration);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You are no longer under the effect of " + ChatColor.WHITE + "Rollout" + ChatColor.GRAY + "!");
		}
	}
}
