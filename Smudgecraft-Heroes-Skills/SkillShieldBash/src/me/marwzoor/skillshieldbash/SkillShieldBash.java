package me.marwzoor.skillshieldbash;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillShieldBash extends TargettedSkill
{
	public static Heroes plugin;
	public static SkillShieldBash skill;
	public final HashMap<String, LivingEntity> bashingPlayers = new HashMap<String, LivingEntity>();
	
	public SkillShieldBash(Heroes instance)
	{
		super(instance, "ShieldBash");
		plugin=instance;
		skill=this;
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill shieldbash" });
		setDescription("Bash your opponent with your shield (launched towards your enemy) stunning and damaging. (Need to have a shield equipped)");
		setTypes(new SkillType[] { SkillType.BUFF });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("max-distance", Integer.valueOf(5));
		node.set("stun-duration", Integer.valueOf(1000));
		node.set("stun-duration-increase", Integer.valueOf(10));
		node.set("damage", Integer.valueOf(70));
		node.set("damage-increase", Integer.valueOf(1));
		node.set("shield-item", Integer.valueOf(36));
		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(20000));
		node.set(SkillSetting.MANA.node(), Integer.valueOf(20));
		return node;
	}
	
	public String getDescription(Hero hero)
	{
		return super.getDescription();
	}

	public SkillResult use(Hero hero, LivingEntity target, String[] args) 
	{
		int maxdistance = SkillConfigManager.getUseSetting(hero, skill, "max-distance", 5, false);
		
		final Player player = hero.getPlayer();
		
		if(player.getLocation().distance(target.getLocation())>maxdistance)
		{
			Messaging.send(player, "Target is too far away!");
			
			return SkillResult.FAIL;
		}
		
		int shielditem = SkillConfigManager.getUseSetting(hero, skill, "shield-item", 36, false);
		
		if(player.getItemInHand().getTypeId()!=shielditem)
		{
			Messaging.send(player, "You are not wielding a shield!");
			return SkillResult.FAIL;
		}
		
		double xDir = target.getLocation().getX() - player.getLocation().getX();
		double zDir = target.getLocation().getZ() - player.getLocation().getZ();
		
		Vector v = new Vector(xDir / 3.0D, 0.5D, zDir / 3.0D);
		
		player.setVelocity(v);
		
		bashingPlayers.put(player.getName(), target);
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{
			public void run()
			{
				player.setFallDistance(8.0F);
			}
		},2L);
		
		skill.broadcast(player.getLocation(), player.getDisplayName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "ShieldBash" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onEntityDamageEvent(EntityDamageEvent event)
		{
			if((!event.getCause().equals(DamageCause.FALL)) || (!(event.getEntity() instanceof Player)))
			{
				return;
			}
			
			Player player = (Player) event.getEntity();
			Hero hero = plugin.getCharacterManager().getHero(player);
			LivingEntity target = bashingPlayers.get(player.getName());
			event.setDamage(0);
			event.setCancelled(true);
			
			if(target==null)
			{
				return;
			}
			
			int shielditem = SkillConfigManager.getUseSetting(hero, skill, "shield-item", 36, false);
			
			if(player.getItemInHand().getTypeId()!=shielditem)
			{
				Messaging.send(player, "You are not wielding a shield!");
				return;
			}
			
			int stunduration = SkillConfigManager.getUseSetting(hero, skill, "stun-duration", 1000, false);
			stunduration += SkillConfigManager.getUseSetting(hero, skill, "stun-duration-increase", 10, false) * hero.getSkillLevel(skill);
			
			int damage = SkillConfigManager.getUseSetting(hero, skill, "damage", 70, false);
			damage += SkillConfigManager.getUseSetting(hero, skill, "damage-increase", 1, false) * hero.getSkillLevel(skill);
			
			if(target instanceof Player)
			{
				Player tplayer = (Player) target;
				Hero thero = plugin.getCharacterManager().getHero(tplayer);
				
				if(stunduration>0)
				{
					thero.addEffect(new StunEffect(skill, stunduration));
				}
				
				skill.addSpellTarget(target, hero);
				skill.damageEntity(tplayer, player, damage);
			}
			else
			{
				CharacterTemplate ct = plugin.getCharacterManager().getCharacter(target);
				
				if(stunduration>0)
				{
					ct.addEffect(new SlowEffect(skill, stunduration, 2, true, Messaging.getLivingEntityName(target) + ChatColor.GRAY + " has been slowed by " + player.getDisplayName(), Messaging.getLivingEntityName(target) + ChatColor.GRAY +  " is no longer slowed by " + player.getDisplayName(), hero));
				}
				
				skill.addSpellTarget(target, hero);
				skill.damageEntity(target, player, damage);
			}
		}
	}
	
}
