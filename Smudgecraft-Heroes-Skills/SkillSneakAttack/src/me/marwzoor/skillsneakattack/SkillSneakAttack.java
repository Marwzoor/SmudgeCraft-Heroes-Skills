package me.marwzoor.skillsneakattack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillSneakAttack extends PassiveSkill
{
	public SkillSneakAttack(Heroes instance)
	{
		super(instance, "SneakAttack");
		setDescription("Striking your enemy with the back of your blade from behind while stealthed have a chance of blinding them for %1 seconds. (right-click with sword) D: %2 C: %3");
		setIdentifiers(new String[] { "skill sneakattack" });
		setTypes(new SkillType[] { SkillType.STEALTHY, SkillType.DEBUFF});
	
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this, plugin), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 3000, false)/1000;
			
			double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE, 0.5D, false)
					+ (SkillConfigManager.getUseSetting(hero, this, "chance-increase", 0.001D, false) * hero.getSkillLevel(this))) * 100;
			
			return super.getDescription().replace("%1", duration + "").replace("%2", duration + "s").replace("%3", chance + "%");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "Xs").replace("%3", "X").replace("%4", "Xs").replace("%5", "X%");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 3000);
		node.set(SkillSetting.CHANCE.node(), 0.5D);
		node.set("chance-increase", 0.001D);
		return node;
	}
	
	public class SkillHeroListener implements Listener
	{
		private Heroes plugin;
		private SkillSneakAttack skill;
		
		public SkillHeroListener(SkillSneakAttack skill, Heroes plugin)
		{
			this.plugin=plugin;
			this.skill=skill;
		}
		
		@EventHandler
		public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(!(event.getRightClicked() instanceof Player))
				return;
			
			if(event.getPlayer().getLocation().getDirection().dot(event.getRightClicked().getLocation().getDirection()) <= 0.0D)
				return;
			
			if(!isBlade(event.getPlayer().getItemInHand()))
				return;
			
			Hero hero = plugin.getCharacterManager().getHero(event.getPlayer());
			
			if(!hero.hasEffect("Stealth"))
				return;
			
			double chance = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.5D, false)
					+ (SkillConfigManager.getUseSetting(hero, skill, "chance-increase", 0.001D, false) * hero.getSkillLevel(skill));
			
			if(!(Math.random()<=chance))
				return;
			
			Hero tHero = plugin.getCharacterManager().getHero((Player) event.getRightClicked());
			
			if(tHero.hasEffect("Blind"))
			{
				tHero.removeEffect(tHero.getEffect("Blind"));
			}
			
			int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 3000, false);
			
			tHero.addEffect(new BlindEffect(skill, duration));
			
			skill.broadcast(tHero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " surprised " + ChatColor.DARK_RED + tHero.getPlayer().getName() + ChatColor.GRAY + " with a " + ChatColor.WHITE + "SneakAttack" + ChatColor.GRAY + "!");
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
	
	public class BlindEffect extends ExpirableEffect
	{
		public BlindEffect(Skill skill, int duration)
		{
			super(skill, "Blind", duration);
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
			PotionEffect blindEffect = PotionEffectType.BLINDNESS.createEffect((int) this.getDuration(), 10);
			hero.getEntity().addPotionEffect(blindEffect);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You are no longer " + ChatColor.WHITE + "Blinded" + ChatColor.GRAY + "!");
		}
	}
}
