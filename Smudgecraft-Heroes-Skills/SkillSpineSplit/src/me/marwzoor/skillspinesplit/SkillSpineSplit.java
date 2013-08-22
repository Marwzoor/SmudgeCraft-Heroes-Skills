package me.marwzoor.skillspinesplit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillSpineSplit extends PassiveSkill
{
	public SkillSpineSplit(Heroes instance)
	{
		super(instance, "SpineSplit");
		setDescription("When you attack your enemy from behind, you have a %1 chance to split their spine, dealing %2 more damage. DMG: %3 M: %4 CD: %5 C: %6");
		setUsage("/skill spinesplit");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill spinesplit" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT, SkillType.COUNTER });
	
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this, plugin), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE, 0.5D, false)
					+ (SkillConfigManager.getUseSetting(hero, this, "chance-increase", 0.005D, false) * hero.getSkillLevel(this))) * 100;
			double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 15.0D, false)
					+ (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, 0.5D, false))*hero.getSkillLevel(this));
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000);
			
			return super.getDescription().replace("%1", chance + "%").replace("%2", damage + "").replace("%3", damage + "").replace("%4", mana + "").replace("%5", cooldown + "s").replace("%6", chance + "%");
		}
		else
		{
			return super.getDescription().replace("%1", "X%").replace("%2", "X%").replace("%3", "X%").replace("%4", "X").replace("%5", "Xs").replace("%6", "X%");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.CHANCE.node(), 0.5D);
		node.set("chance-increase", 0.005D);
		node.set(SkillSetting.MANA.node(), 0);
		node.set(SkillSetting.COOLDOWN.node(), 0);
		node.set(SkillSetting.DAMAGE.node(), 15);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 0.5D);
		return node;
	}
	
	public class SkillHeroListener implements Listener
	{
		private SkillSpineSplit skill;
		private Heroes plugin;
		
		public SkillHeroListener(SkillSpineSplit skill, Heroes plugin)
		{
			this.skill=skill;
			this.plugin=plugin;
		}
		
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(!(event.getEntity() instanceof LivingEntity))
				return;
			
			if(!(event.getAttackerEntity() instanceof Player))
				return;
			
			if(event.getEntity().getLocation().getDirection().dot(event.getAttackerEntity().getLocation().getDirection()) <= 0.0D)
				return;
			
			if(!isBlade(((Player)event.getAttackerEntity()).getItemInHand()))
				return;
			
			Hero hero = plugin.getCharacterManager().getHero((Player) event.getAttackerEntity());
				
			double chance = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE, 0.5D, false)
					+ (SkillConfigManager.getUseSetting(hero, skill, "chance-increase", 0.005D, false) * hero.getSkillLevel(skill)));
				
			if(chance>=Math.random())
			{
				double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 15.0D, false)
						+ (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0.5D, false))*hero.getSkillLevel(skill));
					
				if(event.getEntity() instanceof Player)
				{
					Messaging.send((Player) event.getAttackerEntity(), "You split " + ChatColor.DARK_RED + ((Player) event.getEntity()).getName() + "'s " + ChatColor.GRAY + "spine! Dealing an additional " + ChatColor.WHITE + ((int) damage) + ChatColor.GRAY + " damage!");
					Messaging.send((Player) event.getEntity(), ChatColor.DARK_RED + ((Player) event.getAttackerEntity()).getName() + ChatColor.GRAY + " split your spine! Dealing an additional " + ChatColor.WHITE + ((int) damage) + ChatColor.GRAY + " damage!");
					event.setDamage(event.getDamage()+damage);
				}
				else
				{
					//Because you have to have a proper language!
					if(isVowel((event.getEntity().getType().getName().charAt(0))))
						Messaging.send((Player) event.getAttackerEntity(), "You split an " + ChatColor.WHITE + event.getEntity().getType().getName() + "'s " + ChatColor.GRAY + "spine! Dealing an additional " + ChatColor.WHITE + ((int) damage) + ChatColor.GRAY + " damage!");
					else
						Messaging.send((Player) event.getAttackerEntity(), "You split a " + ChatColor.WHITE + event.getEntity().getType().getName() + "'s " + ChatColor.GRAY + "spine! Dealing an additional " + ChatColor.WHITE + ((int) damage) + ChatColor.GRAY + " damage!");
						
						event.setDamage(event.getDamage()+damage);
				}
			}
		}
		
		public boolean isVowel(char ch) 
		{ 
			return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
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
}
