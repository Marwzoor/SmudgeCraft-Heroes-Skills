package net.smudgecraft.heroeslib.commoneffects;

import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.Skill;

public class ImbueEffect extends ExpirableEffect
{
	Player player;
	
	public ImbueEffect(Skill skill, String name, int duration, Player player)
	{
		super(skill, name, duration);
		this.player=player;
	}
	
	public ImbueEffect(Skill skill, String name, int duration)
	{
		super(skill, name, duration);
		this.player=null;
	}
	
	public Player getPlayer()
	{
		return this.player;
	}
	
	public void applyToHero(Hero hero, ImbueEffect ie)
	{
		for(Effect effect : hero.getEffects())
		{
			if(effect instanceof ImbueEffect && !effect.equals(ie))
			{
				hero.removeEffect(effect);
			}
		}
		
		super.applyToHero(hero);
	}
	
	public void removeFromHero(Hero hero)
	{
		super.removeFromHero(hero);
	}
}
