package me.marwzoor.skillterror;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.smudgecraft.heroeslib.companions.Companion;
import net.smudgecraft.heroeslib.companions.CompanionPlayer;
import net.smudgecraft.heroeslib.companions.Companions;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillTerror extends ActiveSkill
{
	public static SkillTerror skill;	
	
	public SkillTerror(Heroes instance)
	{
		super(instance, "Terror");
		skill=this;
		setDescription("You terrorize your minions, boosting their speed for $X seconds. M: $1 CD: $2");
		setUsage("/skill terror");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill terror" });
		setTypes(new SkillType[] { SkillType.BUFF });
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 15000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			duration = duration / 1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", duration + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 15000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 50);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 15000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		CompanionPlayer cp = Companions.getPlayerManager().getCompanionPlayer(hero.getPlayer());
		List<Companion> cmps = cp.getCompanions();
		hero.addEffect(new TerrorEffect(this, (long) duration, cmps));
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + 
				" terrorize their minions!");
		return SkillResult.NORMAL;
	}
	
	public class TerrorEffect extends ExpirableEffect
	{
		private List<Companion> cmps = new ArrayList<Companion>();
		private PotionEffect pe;
		
		public TerrorEffect(Skill skill, long duration, List<Companion> cmps)
		{
			super(skill, "Terror", duration);
			this.cmps = cmps;
			pe = new PotionEffect(PotionEffectType.SPEED, (int) (duration/1000*20),0);
		}
		
		@Override
		public void applyToHero(Hero hero)
		{
			Iterator<Companion> itr = cmps.iterator();
			while(itr.hasNext())
			{
				Companion cmp = itr.next();
				if(cmp.getLivingEntity().hasPotionEffect(pe.getType()))
				{
					itr.remove();
					continue;
				}
				cmp.getLivingEntity().addPotionEffect(pe);
			}
			super.applyToHero(hero);
		}
		
		@Override
		public void removeFromHero(Hero hero)
		{
			boolean check = false;
			Iterator<Companion> itr = cmps.iterator();
			while(itr.hasNext())
			{
				Companion cmp = itr.next();
				if(!cmp.getLivingEntity().isDead())
				{
					cmp.getLivingEntity().removePotionEffect(pe.getType());
					check = true;
				}
			}
			if(check)
				Messaging.send(hero.getPlayer(), "Your minions are no longer terrified!");
			super.removeFromHero(hero);
		}
	}
}
