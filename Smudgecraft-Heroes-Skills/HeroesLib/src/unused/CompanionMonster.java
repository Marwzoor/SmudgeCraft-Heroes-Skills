package unused;


import org.bukkit.entity.LivingEntity;

@Deprecated
public abstract class CompanionMonster extends Companion
{
	private double damage;
	
	protected CompanionMonster(LivingEntity livingEntity, String owner, String name, double damage, String id, boolean shouldSave) 
	{
		super(livingEntity, owner, name, id, shouldSave);
		this.damage=damage;
		this.ctype=CompanionType.UNKNOWN;
	}
	
	protected CompanionMonster(LivingEntity livingEntity, String owner, String name, String id, boolean shouldSave)
	{
		super(livingEntity, owner, name, id, shouldSave);
		this.ctype=CompanionType.UNKNOWN;
	}
	
	public double getDamage()
	{
		return this.damage;
	}
	
	public void setDamage(double damage)
	{
		this.damage=damage;
	}
}
