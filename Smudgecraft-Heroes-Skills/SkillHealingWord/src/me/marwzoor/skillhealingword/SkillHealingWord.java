package me.marwzoor.skillhealingword;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillHealingWord extends TargettedSkill
{
	public SkillHealingWord(Heroes instance)
	{
		super(instance, "HealingWord");
		setDescription("You chant the Healing Word to your target, healing them. (Party) H: %H M: %M CD: %CD");
		setUsage("/skill healingword");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill healingword" });
		setTypes(new SkillType[] { SkillType.HEAL, SkillType.BUFF, SkillType.KNOWLEDGE });
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
		int heal = (int) (SkillConfigManager.getUseSetting(hero, this, "heal", Integer.valueOf(50), false) +
				(SkillConfigManager.getUseSetting(hero, this, "heal-increase", Double.valueOf(0.5), false) * hero.getSkillLevel(this)));
		int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, Integer.valueOf(0), false) / 1000);
		int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, Integer.valueOf(0), false);
			return super.getDescription().replace("%H", heal + "").replace("%M", mana + "").replace("%CD", cooldown + "");
		}
		else
		{
			return super.getDescription().replace("%H", "X").replace("%M", "X").replace("%CD", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("heal", Integer.valueOf(50));
		node.set("heal-increase", Double.valueOf(0.5));
		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(0));
		node.set(SkillSetting.MANA.node(), Integer.valueOf(0));
		return node;
	}
	
	public SkillResult use(Hero hero, LivingEntity target, String[] args)
	{
		if(!(target instanceof Player))
			return SkillResult.INVALID_TARGET;
		
		Player tplayer = (Player) target;
		
		if(tplayer.equals(hero.getPlayer()))
		{
			int heal = (int) ((SkillConfigManager.getUseSetting(hero, this, "heal", Integer.valueOf(50), false) + 
					(SkillConfigManager.getUseSetting(hero, this, "heal-increase", Double.valueOf(0.5), false) * hero.getSkillLevel(this))) * 0.77);
			
			if(tplayer.getHealth()+heal>=tplayer.getMaxHealth())
			{
				tplayer.setHealth(tplayer.getMaxHealth());
				
				Location loc = tplayer.getEyeLocation();
				
				for(int i=0;i<5;++i)
				{
					Location tloc = loc;
					tloc.setY(tloc.getY()+((Math.random()/2)-0.25));
					tloc.setX(tloc.getX()+(Math.random()-0.5));
					tloc.setZ(tloc.getZ()+(Math.random()-0.5));
					
					tloc.getWorld().playEffect(tloc, Effect.HEART, 4);
				}
				
				Messaging.send(tplayer, "You whisper the " + ChatColor.WHITE + "Healing Word" + ChatColor.GRAY + " to yourself, reducing its effect by " + ChatColor.WHITE + "33%" + ChatColor.GRAY + ".");
				Messaging.send(tplayer, "healing yourself to " + ChatColor.WHITE + "Full" + ChatColor.GRAY + " Health.");
			}
			else
			{
				tplayer.setHealth(tplayer.getHealth()+heal);
				
				tplayer.getWorld().playEffect(tplayer.getEyeLocation(), Effect.HEART, 4);
				
				Messaging.send(tplayer, "You whisper the " + ChatColor.WHITE + "Healing Word" + ChatColor.GRAY + " to yourself, reducing its effect by " + ChatColor.WHITE + "33%" + ChatColor.GRAY + ".");
				Messaging.send(tplayer, "healing yourself to " + ChatColor.WHITE + tplayer.getHealth() + "/" + tplayer.getMaxHealth() + ChatColor.GRAY + " Health.");
			}
		}
		else
		{
			if(!hero.hasParty())
			{
				Messaging.send(hero.getPlayer(), "You can't heal players if you are not even in a party!");
				return SkillResult.INVALID_TARGET_NO_MSG;
			}
			else
			{
				Hero thero = plugin.getCharacterManager().getHero(tplayer);
				
				if(hero.getParty().getMembers().contains(thero))
				{
					int heal = (int) (SkillConfigManager.getUseSetting(hero, this, "heal", Integer.valueOf(50), false) + 
							(SkillConfigManager.getUseSetting(hero, this, "heal-increase", Double.valueOf(0.5), false) * hero.getSkillLevel(this)));
					
					if(tplayer.getHealth() + heal >= tplayer.getMaxHealth())
					{
						tplayer.setHealth(tplayer.getMaxHealth());
						tplayer.playEffect(EntityEffect.WOLF_HEARTS);
						Location loc = hero.getPlayer().getEyeLocation();
						loc.setY(loc.getY()+0.4);
						loc.getWorld().playEffect(loc, Effect.FLYING_GLYPH, 4);
						tplayer.playEffect(EntityEffect.WOLF_HEARTS);
						Messaging.send(hero.getPlayer(), "You chant the " + ChatColor.WHITE + "Healing Word" + ChatColor.GRAY + " to " + ChatColor.DARK_RED + tplayer.getName() + ChatColor.GRAY + ", healing them to " + ChatColor.WHITE + tplayer.getHealth() + "/" + tplayer.getMaxHealth() + ChatColor.GRAY + ".");
						this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " chanted the " + ChatColor.WHITE + "Healing Word" + ChatColor.GRAY + " to " + ChatColor.DARK_RED + tplayer.getName() + ChatColor.GRAY + ".");
					}
					else
					{
						tplayer.setHealth(tplayer.getHealth()+heal);
						Location loc = hero.getPlayer().getEyeLocation();
						loc.setY(loc.getY()+0.4);
						loc.getWorld().playEffect(loc, Effect.FLYING_GLYPH, 4);
						tplayer.playEffect(EntityEffect.WOLF_HEARTS);
						Messaging.send(hero.getPlayer(), "You chant the " + ChatColor.WHITE + "Healing Word" + ChatColor.GRAY + " to " + ChatColor.DARK_RED + tplayer.getName() + ChatColor.GRAY + ", healing them to " + ChatColor.WHITE + tplayer.getHealth() + "/" + tplayer.getMaxHealth() + ChatColor.GRAY + ".");
						this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " chanted the " + ChatColor.WHITE + "Healing Word" + ChatColor.GRAY + " to " + ChatColor.DARK_RED + tplayer.getName() + ChatColor.GRAY + ".");
					}
				}
				else
				{
					Messaging.send(hero.getPlayer(), "That player is not in your party!");
					return SkillResult.INVALID_TARGET_NO_MSG;
				}
			}
		}
		
		return SkillResult.NORMAL;
	}
}
