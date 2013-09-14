package me.marwzoor.skillburningcorpse;

import java.util.Iterator;

import net.smudgecraft.heroeslib.commoneffects.BurnEffect;
import net.smudgecraft.heroeslib.util.FireworkEffectPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.HeroKillCharacterEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillBurningCorpse extends PassiveSkill
{
	public static SkillBurningCorpse skill;	
	
	public SkillBurningCorpse(Heroes instance)
	{
		super(instance, "BurningCorpse");
		skill=this;
		setDescription("When you kill something it has a $X% chance to set all enemies within $Y blocks on fire, dealing $Z burn damage every second for $Q seconds. M: $1 CD: $2");
		//setUsage("/skill scorch");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill burningcorpse" });
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new BurningCorpseListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double burnDamage = (SkillConfigManager.getUseSetting(hero, skill, "burn-damage", 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "burn-damage-increase", 0.5, false) * hero.getSkillLevel(skill)));
			double radius = (SkillConfigManager.getUseSetting(hero, skill, "raidus", 10, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "radius-increase", 0, false) * hero.getSkillLevel(skill)));
			double chance = (SkillConfigManager.getUseSetting(hero, skill, "chance", 0.8, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "chance-increase", 0, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 7000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			duration = duration/1000;
			chance = chance * 100;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", chance + "").replace("$Y", radius + "").replace("$Z", burnDamage + "").replace("$Q", duration + "")
					.replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 7000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 50);
		node.set(SkillSetting.DAMAGE.node(), 50);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 10);
		node.set("brun-damage", 50);
		node.set("burn-damage-increase", 0.5);
		node.set("chance", 0.8);
		node.set("chance-increase", 0);
		node.set("radius", 10);
		node.set("radius-increase", 0);
		return node;
	}
	
	public class BurningCorpseListener implements Listener
	{
		private final FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
		private final FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.RED).withColor(Color.ORANGE).build();
		
		@EventHandler
		public void onHeroKillCharacterEvent(HeroKillCharacterEvent event)
		{
			if(!event.getAttacker().hasAccessToSkill(skill))
				return;
			Hero hero = event.getAttacker();
			double chance = (SkillConfigManager.getUseSetting(hero, skill, "chance", 0.8, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "chance-increase", 0, false) * hero.getSkillLevel(skill)));
			if(Math.random() > chance)
				return;
			LivingEntity target = event.getDefender().getEntity();
			try
			{
				fplayer.playFirework(target.getWorld(), target.getLocation(), fe);
			}
			catch(Exception e)
			{
			}
			double radius = (SkillConfigManager.getUseSetting(hero, skill, "raidus", 10, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "radius-increase", 0, false) * hero.getSkillLevel(skill)));
			double burnDamage = (SkillConfigManager.getUseSetting(hero, skill, "burn-damage", 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "burn-damage-increase", 0.5, false) * hero.getSkillLevel(skill)));
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 10, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 7000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			Iterator<Entity> itr = target.getNearbyEntities(radius, radius, radius).iterator();
			while(itr.hasNext())
			{
				Entity en = itr.next();
				if(!(en instanceof LivingEntity))
					continue;
				LivingEntity inRadius = (LivingEntity)en;
				if(inRadius instanceof Player)
				{
					Player p = (Player)inRadius;
					if(p.equals(hero.getPlayer()))
						continue;
					if(hero.hasParty())
					{
						if(hero.getParty().getMembers().contains(plugin.getCharacterManager().getHero(p)))
							continue;
					}
				}
				skill.damageEntity(inRadius, hero.getPlayer(), damage);
				plugin.getCharacterManager().getCharacter(inRadius).addEffect(new BurnEffect(skill, 1000, (int) duration, burnDamage, hero.getPlayer(), false));
			}
			
			if(target instanceof Player)
				broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + ((Player)target).getName() 
						+ ChatColor.WHITE + "'s" + ChatColor.GRAY + " corpse is burning!");
			else
				broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_GREEN + target.getType().getName()
						+ ChatColor.WHITE + "'s" + ChatColor.GRAY + " corpse is burning!");
		}
	}
}

