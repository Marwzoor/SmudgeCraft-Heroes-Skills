package me.marwzoor.skillfuriousstrike;

import net.smudgecraft.companions.imbuearrows.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillFuriousStrike extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillFuriousStrike skill;
	
	public SkillFuriousStrike(Heroes instance)
	{
		super(instance, "FuriousStrike");
		plugin=instance;
		skill=this;
		setDescription("Strike with your blade, stunning your target and gaining more fury, but reducing the damage for that hit by $X%.");
		setIdentifiers(new String[] { "skill furiousstrike", "skill fstrike" });
		setArgumentRange(0, 0);
		setTypes(new SkillType[] { SkillType.BUFF });
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		String desc = super.getDescription();
        double damagedecrease = SkillConfigManager.getUseSetting(hero, skill, "damage-decrease", 0.5, false);
        damagedecrease = damagedecrease*100;
        int dd = (int) damagedecrease;
		desc = desc.replace("$X", dd + "");
		return desc;
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("duration", Integer.valueOf(15000));
		node.set("slow-duration", Integer.valueOf(700));
		node.set("slow-duration-increase", Integer.valueOf(10));
		node.set("damage-decrease", Double.valueOf(0.5));
		node.set("mana-regain", Double.valueOf(15));
		node.set("mana-regain-increase", Double.valueOf(0.05));
		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(10000));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(15000), false);
		int slowduration = SkillConfigManager.getUseSetting(hero, skill, "slow-duration", Integer.valueOf(500), false);
		double damagedecrease = SkillConfigManager.getUseSetting(hero, skill, "damage-decrease", Double.valueOf(0.5), false);
		double manareg = SkillConfigManager.getUseSetting(hero, skill, "mana-regain", Double.valueOf(15), false);
		manareg += SkillConfigManager.getUseSetting(hero, skill, "mana-regain-increase", Double.valueOf(0.05), false) * hero.getSkillLevel(skill);
		
		int manaregain = (int) manareg;
		
		if(hero.hasEffect("FuriousStrike"))
		{
			hero.removeEffect(hero.getEffect("FuriousStrike"));
		}
		
		FuriousStrikeEffect fse = new FuriousStrikeEffect(skill, duration, slowduration, damagedecrease, manaregain);
		
		hero.addEffect(fse);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "FuriousStrike" + ChatColor.GRAY + "! You will slow the next opponent you hit and regain fury!");

		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(!event.getCause().equals(DamageCause.ENTITY_ATTACK))
				return;
			
			if(event.getDamager() instanceof Hero)
			{
				Hero hero = (Hero) event.getDamager();
				
				if(hero.hasEffect("FuriousStrike"))
				{
					if(event.getEntity() instanceof LivingEntity)
					{
						FuriousStrikeEffect fse = (FuriousStrikeEffect) hero.getEffect("FuriousStrike");
						
						CharacterTemplate ct = plugin.getCharacterManager().getCharacter((LivingEntity) event.getEntity());
						
						SlowEffect sEffect = new SlowEffect(skill, fse.getSlowDuration(), 2, false, "", "", hero);
						ct.addEffect(sEffect);
						
						double damage = event.getDamage()*fse.getDamageDecrease();
						
						event.setDamage((int) damage);
						
						int manareg = fse.getManaRegain();
						
						if(hero.getMana()+manareg>=hero.getMaxMana())
						{
							hero.setMana(hero.getMaxMana());
						}
						else
						{
							hero.setMana(hero.getMana()+manareg);
						}
						
						hero.removeEffect(fse);
						
						LivingEntity le = (LivingEntity) event.getEntity();
						
						if(le instanceof Player)
						{
							skill.broadcast(le.getLocation(), ChatColor.GOLD + ((Player) le).getName() + ChatColor.GRAY + " was struck by " + hero.getPlayer().getDisplayName() + ChatColor.WHITE + " FuriousStrike" + ChatColor.GRAY + "!");
						}
						else
						{
							skill.broadcast(le.getLocation(), ChatColor.GOLD + Messaging.getLivingEntityName(le) + ChatColor.GRAY + " was struck by " + hero.getPlayer().getDisplayName() + ChatColor.WHITE + " FuriousStrike" + ChatColor.GRAY + "!");
						}
					}
				}
			}
		}
	}
	
	public class FuriousStrikeEffect extends ImbueEffect
	{
		private int slowduration;
		private double damagedecrease;
		private int manaregain;
		
		public FuriousStrikeEffect(Skill skill, int duration, int slowduration, double damagedecrease, int manaregain)
		{
			super(skill, "FuriousStrike", duration);
			this.slowduration=slowduration;
			this.damagedecrease=damagedecrease;
			this.manaregain=manaregain;
		}
		
		public double getDamageDecrease()
		{
			return damagedecrease;
		}
		
		public int getManaRegain()
		{
			return this.manaregain;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "Stab" + ChatColor.GRAY + "!", new Object());
		}
		
		public int getSlowDuration()
		{
			return this.slowduration;
		}
	}
}
