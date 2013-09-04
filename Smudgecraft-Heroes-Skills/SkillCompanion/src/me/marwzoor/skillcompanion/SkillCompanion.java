package me.marwzoor.skillcompanion;

import java.util.Random;

import net.smudgecraft.heroeslib.companions.Companion;
import net.smudgecraft.heroeslib.companions.CompanionPlayer;
import net.smudgecraft.heroeslib.companions.Companions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.ClassChangeEvent;
import com.herocraftonline.heroes.api.events.HeroChangeLevelEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillCompanion extends ActiveSkill
{
	public static SkillCompanion skill;
	
	public SkillCompanion(Heroes instance)
	{
		super(instance, "Companion");
		skill=this;
		setDescription("You spawn your wolf companion to aid you in battle. HP: %1 DMG: %2");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill companion" });
		setTypes(new SkillType[] { SkillType.SUMMON });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			String desc = super.getDescription();
			int health = SkillConfigManager.getUseSetting(hero, skill, "wolfhealth", Integer.valueOf(300), false);
			health += SkillConfigManager.getUseSetting(hero, skill, "wolfhealth-increase", Integer.valueOf(5), false) * hero.getSkillLevel(skill);
			double damage = SkillConfigManager.getUseSetting(hero, skill, "wolfdamage", Double.valueOf(40), false);
			damage += SkillConfigManager.getUseSetting(hero, skill, "wolfdamage-increase", Double.valueOf(0.2), false) * hero.getSkillLevel(skill);
			desc = desc.replace("%1", health + "");
			desc = desc.replace("%2", damage + "");
			return desc;
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "X");
		}
	}
	
	public ConfigurationSection getDefaultDescription()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("wolfhealth", Integer.valueOf(300));
		node.set("wolfhealth-increase", Integer.valueOf(5));
		node.set("wolfdamage", Integer.valueOf(40));
		node.set("wolfdamage-increase", Double.valueOf(0.2));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		String[] names = new String[]{ "§4Aslan", "§4Raiku", "§bTyrion", "§bMerez", "§2Mundu", "§2Roof", "§eDanion", "§eHowleth", "§0Syric", "§0Mandrew", "§5Undion", "§5Quaz", "§4Wereth", "§4Fury", "§bAxlith", "§bOrion" };
		
		Random rand = new Random();
		
		String name = names[rand.nextInt(names.length)];
		
		Player player = hero.getPlayer();
		
		if(!Companions.getPlayerManager().contains(player))
		{
			player.sendMessage(ChatColor.RED + "You are not a CompanionPlayer, tell the admins about this.");
			return SkillResult.FAIL;
		}
		
		CompanionPlayer cplayer = Companions.getPlayerManager().getCompanionPlayer(player);
		
		if(cplayer.hasCompanionOfType(EntityType.WOLF))
		{
			Messaging.send(player, ChatColor.RED + "You already have your companion spawned!");
			
			return SkillResult.CANCELLED;
		}
		
		Location loc = hero.getPlayer().getLocation();
		
		int maxhealth = SkillConfigManager.getUseSetting(hero, skill, "wolfhealth", Integer.valueOf(300), false);
		maxhealth += SkillConfigManager.getUseSetting(hero, skill, "wolfhealth-increase", Integer.valueOf(5), false) * hero.getSkillLevel(skill);
		
		double damage = SkillConfigManager.getUseSetting(hero, skill, "wolfdamage", Double.valueOf(40), false);
		damage += SkillConfigManager.getUseSetting(hero, skill, "wolfdamage-increase", Double.valueOf(0.2), false) * hero.getSkillLevel(skill);
		
		Companion cwolf = new Companion(EntityType.WOLF.toString(), player.getName(), "0", name, damage, maxhealth, maxhealth, loc, randomDyeColor(), true);
		
		cplayer.addCompanion(cwolf);
		
		Messaging.send(hero.getPlayer(), "You have summoned your" + ChatColor.WHITE + " Companion " + ChatColor.GRAY + "to aid you in battle!", new Object());
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onHeroGainLevelEvent(HeroChangeLevelEvent event)
		{
			if(event.getFrom()<event.getTo())
			{
				Hero hero = event.getHero();
				Player player = hero.getPlayer();
				
				if(!Companions.getPlayerManager().contains(player))
					return;
				
				CompanionPlayer cplayer = Companions.getPlayerManager().getCompanionPlayer(player);
				
				if(cplayer.hasCompanionOfType(EntityType.WOLF))
				{				
					Companion cwolf = null;
					
					for(Companion c : cplayer.getCompanions())
					{
						if(c.getLivingEntity()!=null && c.getLivingEntity() instanceof Wolf)
						{
							cwolf=c;
							continue;
						}
					}
					
					if(cwolf==null)
						return;
					
					int maxhealth = SkillConfigManager.getUseSetting(hero, skill, "wolfhealth", Integer.valueOf(300), false);
					maxhealth += SkillConfigManager.getUseSetting(hero, skill, "wolfhealth-increase", Integer.valueOf(5), false) * hero.getSkillLevel(skill);
					
					double damage = SkillConfigManager.getUseSetting(hero, skill, "wolfdamage", Double.valueOf(40), false);
					damage += SkillConfigManager.getUseSetting(hero, skill, "wolfdamage-increase", Double.valueOf(0.2), false) * hero.getSkillLevel(skill);
					
					cwolf.setMaxHealth(maxhealth);
					cwolf.setHealth(maxhealth);
					
					cwolf.setDamage((int) damage);
				}
			}
		}
		
		@EventHandler
		public void onHeroChangeClassEvent(ClassChangeEvent event)
		{
			final Hero hero = event.getHero();
			
			if(Companions.getPlayerManager().contains(hero.getPlayer()))
				return;
			
			if(event.getFrom().getSkillNames().contains("companion"))
			{
				if(event.getTo().isPrimary())
				{
					if(Companions.getPlayerManager().getCompanionPlayer(hero.getPlayer()).hasCompanionOfType(EntityType.WOLF))
					{
						if(!event.getTo().getSkillNames().contains("companion"))
						{
							for(Companion c : Companions.getPlayerManager().getCompanionPlayer(hero.getPlayer()).getCompanions())
							{
								c.getLocation().getWorld().playSound(c.getLocation(), Sound.WOLF_WHINE, 10, 1);
								c.getLivingEntity().remove();
								hero.getPlayer().sendMessage(ChatColor.GRAY + "Your " + ChatColor.WHITE + "Companion" + ChatColor.GRAY + " is very sad because it has to leave you now...");
								Companions.getPlayerManager().getCompanionPlayer(hero.getPlayer()).removeCompanion(c);
							}
						}
					}
				}
			}
		}
	}
	
	public DyeColor randomDyeColor()
	{
		int i = DyeColor.values().length;
		int r = new Random().nextInt(i);
		
		return DyeColor.values()[r];
	}
}
