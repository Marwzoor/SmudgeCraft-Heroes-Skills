package net.smudgecraft.heroeslib.commoneffects;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.Skill;

public class BleedEffect extends PeriodicDamageEffect
{
	private boolean particleeffects;
	
	public BleedEffect(Skill skill, int period, int duration, double tickdamage, Player applier, boolean knockback, boolean particleeffects)
	{
		super(skill, "Bleed", period, duration, tickdamage, applier, knockback);
		this.particleeffects=particleeffects;
	}
	
	public BleedEffect(Skill skill, int period, int duration, double tickdamage, Player applier, boolean knockback)
	{
		super(skill, "Bleed", period, duration, tickdamage, applier, knockback);
		this.particleeffects=false;
	}
	
	public BleedEffect(Skill skill, int period, int duration, double tickdamage, Player applier)
	{
		super(skill, "Bleed", period, duration, tickdamage, applier);
		this.particleeffects=false;
	}
	
	public void applyToHero(Hero hero)
	{
		super.applyToHero(hero);
	}

	public void removeFromHero(Hero hero)
	{
		super.removeFromHero(hero);
	}

	public void applyToMonster(Monster monster)
	{
		super.applyToMonster(monster);
	}

	public void removeFromMonster(Monster monster)
	{
		super.removeFromMonster(monster);
	}
	
	public void tickMonster(Monster monster)
	{
		super.tickMonster(monster);
		if(particleeffects && !monster.getEntity().isDead() && monster.hasEffect("Bleed"))
			monster.getEntity().getWorld().playEffect(monster.getEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
	}
	
	public void tickHero(Hero hero)
	{
		super.tickHero(hero);
		if(particleeffects && !hero.getEntity().isDead() && hero.hasEffect("Bleed"))
			hero.getEntity().getWorld().playEffect(hero.getEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
	}
}
