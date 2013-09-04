package unused;


import org.bukkit.entity.Wolf;

@Deprecated
public class CompanionWolf extends CompanionMonster
{	
	public CompanionWolf(Wolf wolf, String owner, String name, double damage, String id, boolean shouldSave) 
	{
		super(wolf, owner, name, damage, id, shouldSave);
		this.ctype=CompanionType.WOLF;
	}
	
	public CompanionWolf(Wolf wolf, String owner, String name, String id, boolean shouldSave)
	{
		super(wolf, owner, name, id, shouldSave);
		this.ctype=CompanionType.WOLF;
	}
	
	public Wolf getWolf()
	{
		return (Wolf) this.getLivingEntity();
	}
}
