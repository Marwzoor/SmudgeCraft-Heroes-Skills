package unused;

import org.bukkit.entity.EntityType;

@Deprecated
public enum CompanionType 
{
	WOLF("wolf",  EntityType.WOLF, CompanionWolf.class, true),
	ZOMBIE("zombie", EntityType.ZOMBIE, CompanionZombie.class, true),
	UNKNOWN("unknown", EntityType.UNKNOWN, Companion.class, false);
	
	private String name;
	private boolean monster;
	private EntityType entityType;
	private Class<? extends Companion> clazz;
	
	private CompanionType(String name, EntityType entityType, Class<? extends Companion> clazz, boolean monster)
	{
		this.name=name;
		this.monster=monster;
		this.entityType=entityType;
		this.clazz=clazz;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public EntityType getEntityType()
	{
		return this.entityType;
	}
	
	public Class<? extends Companion> getCompanionClass()
	{
		return this.clazz;
	}
	
	public boolean isMonster()
	{
		return this.monster;
	}
	
	public static CompanionType getByName(String name)
	{
		for(CompanionType ctype : values())
		{
			if(ctype.getName().equalsIgnoreCase(name))
				return ctype;
		}
		return null;
	}
}
