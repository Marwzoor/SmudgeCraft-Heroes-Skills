package net.smudgecraft.heroeslib.commoneffects;

import java.util.Iterator;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.Skill;

public class FreezeEffect extends ExpirableEffect
{
	private PotionEffect pe;
	
	public FreezeEffect(Skill skill, String name, int duration)
	{
		super(skill, name, duration);
		pe = new PotionEffect(PotionEffectType.SLOW,(duration/1000)*20,0);
	}
	
	public void applyToHero(Hero hero)
	{
		if(hero.getPlayer().hasPotionEffect(PotionEffectType.SLOW))
			hero.getPlayer().removePotionEffect(PotionEffectType.SLOW);
		hero.getPlayer().addPotionEffect(pe);
		
		super.applyToHero(hero);
	}
	
	public void removeFromHero(Hero hero)
	{
		if(hero.getPlayer().hasPotionEffect(PotionEffectType.SLOW))
			hero.getPlayer().removePotionEffect(PotionEffectType.SLOW);
		super.removeFromHero(hero);
	}
	
	public static boolean hasFreezeEffect(CharacterTemplate character)
	{
		Iterator<Effect> itr = character.getEffects().iterator();
		while(itr.hasNext())
		{
			Effect effect = itr.next();
			if(effect instanceof FreezeEffect)
				return true;
		}
		return false;
	}
}
