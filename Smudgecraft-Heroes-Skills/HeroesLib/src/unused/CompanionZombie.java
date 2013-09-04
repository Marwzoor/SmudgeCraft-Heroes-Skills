package unused;

import org.bukkit.entity.Zombie;

@Deprecated
public class CompanionZombie extends CompanionMonster
{
	public CompanionZombie(Zombie zombie, String owner, String name, double damage, String id, boolean shouldSave)
	{
		super(zombie, owner, name, damage, id, shouldSave);
		this.ctype=CompanionType.ZOMBIE;
	}
	
	public CompanionZombie(Zombie zombie, String owner, String name, String id, boolean shouldSave)
	{
		super(zombie, owner, name, id, shouldSave);
		this.ctype=CompanionType.ZOMBIE;
	}
	
	public Zombie getZombie()
	{
		return (Zombie) this.getLivingEntity();
	}
}
