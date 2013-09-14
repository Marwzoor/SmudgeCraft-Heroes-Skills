package net.smudgecraft.heroeslib.commoneffects;

import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.Skill;

public class BurnEffect extends PeriodicDamageEffect
{
	private int burn;
	
	public BurnEffect(Skill skill, int period, int duration, double tickdamage, Player applier, boolean knockback)
	{
		super(skill, "Burn", period, duration, tickdamage, applier, knockback);
		burn = duration;
	}
	
	@Override
	public void applyToHero(Hero hero)
	{
		hero.getPlayer().setFireTicks(burn);
		super.applyToHero(hero);
	}
	
	@Override
	public void applyToMonster(Monster monster)
	{
		monster.getEntity().setFireTicks(burn);
		super.applyToMonster(monster);
	}
	
	@Override
	public void removeFromHero(Hero hero)
	{
		hero.getPlayer().setFireTicks(0);
		super.removeFromHero(hero);
	}
	
	@Override
	public void removeFromMonster(Monster monster)
	{
		monster.getEntity().setFireTicks(0);
		super.removeFromMonster(monster);
	}
}
