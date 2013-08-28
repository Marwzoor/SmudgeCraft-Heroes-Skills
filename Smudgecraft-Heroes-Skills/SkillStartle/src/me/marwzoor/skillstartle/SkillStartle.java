package me.marwzoor.skillstartle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillStartle extends PassiveSkill
{
	public SkillStartle(Heroes instance)
	{
		super(instance, "Startle");
		setDescription("When striking your enemy straight on with your blade while in stealth, you will stun them for %1 seconds and confuse them for %2 seconds.");
		setIdentifiers(new String[] { "skill startle" });
		setTypes(new SkillType[] { SkillType.STEALTHY, SkillType.DEBUFF, SkillType.INTERRUPT});
	
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this, plugin), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int confuseDuration = (SkillConfigManager.getUseSetting(hero, this, "confuse-duration", 3000, false)
					+ (SkillConfigManager.getUseSetting(hero, this, "confuse-duration-increase", 50, false) * hero.getSkillLevel(this)))/1000;
			
			int stunDuration = (SkillConfigManager.getUseSetting(hero, this, "stun-duration", 3000, false)
					+ (SkillConfigManager.getUseSetting(hero, this, "stun-duration-increase", 50, false) * hero.getSkillLevel(this)))/1000;
			
			return super.getDescription().replace("%1", stunDuration + "").replace("%2", confuseDuration + "s");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("stun-duration", 3000);
		node.set("stun-duration-increase", 50);
		node.set("confuse-duration", 3000);
		node.set("confuse-duration-increase", 50);
		return node;
	}
	
	public class SkillHeroListener implements Listener
	{
		private Heroes plugin;
		private SkillStartle skill;
		
		public SkillHeroListener(SkillStartle skill, Heroes plugin)
		{
			this.plugin=plugin;
			this.skill=skill;
		}
		
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(!(event.getAttackerEntity() instanceof Player))
				return;
			
			if(!(event.getEntity() instanceof Player))
				return;
			
			if(event.getAttackerEntity().getLocation().getDirection().dot(event.getEntity().getLocation().getDirection()) > 0.0D)
				return;
			
			if(!isBlade(((Player)event.getAttackerEntity()).getItemInHand()))
				return;
			
			Hero hero = plugin.getCharacterManager().getHero((Player) event.getAttackerEntity());
			
			if(!hero.hasEffect("Stealth"))
				return;
			
			Hero tHero = plugin.getCharacterManager().getHero((Player) event.getEntity());
			
			if(tHero.hasEffect("Blind"))
			{
				tHero.removeEffect(tHero.getEffect("Blind"));
			}
			
			int confuseDuration = (SkillConfigManager.getUseSetting(hero, skill, "confuse-duration", 3000, false)
					+ (SkillConfigManager.getUseSetting(hero, skill, "confuse-duration-increase", 50, false) * hero.getSkillLevel(skill)));
			
			int stunDuration = (SkillConfigManager.getUseSetting(hero, skill, "stun-duration", 3000, false)
					+ (SkillConfigManager.getUseSetting(hero, skill, "stun-duration-increase", 50, false) * hero.getSkillLevel(skill)));
			
			tHero.addEffect(new	StartleEffect(skill, stunDuration, confuseDuration));
		}
		
		public boolean isBlade(ItemStack is)
		{
			int id = is.getTypeId();
		
			switch(id)
			{
				case 268: return true;
				case 272: return true;
				case 276: return true;
				case 283: return true;
				case 267: return true;
				default: return false;
			}
		}
	}
	
	public class StartleEffect extends ExpirableEffect
	{
		private int confuseDuration;
		private int stunDuration;
		
		public StartleEffect(Skill skill, int stunDuration, int confuseDuration)
		{
			super(skill, "Startle", stunDuration);
			this.confuseDuration=confuseDuration;
			this.stunDuration=stunDuration;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
			hero.addEffect(new StunEffect(skill, stunDuration));
			this.skill.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " is now " + ChatColor.WHITE + "Startled" + ChatColor.GRAY + "!");
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			StartleConfuseEffect sEffect = new StartleConfuseEffect(this.skill, confuseDuration);
			hero.addEffect(sEffect);
		}
	}
	
	public class StartleConfuseEffect extends ExpirableEffect
	{
		public StartleConfuseEffect(Skill skill, int duration)
		{
			super(skill, "StartleConfuse", duration);
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
			PotionEffect confuseEffect = PotionEffectType.CONFUSION.createEffect((int) this.getDuration(), 10);
			hero.getEntity().addPotionEffect(confuseEffect);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			this.skill.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " is no longer " + ChatColor.WHITE + "Startled" + ChatColor.GRAY + "!");
		}
	}
}
