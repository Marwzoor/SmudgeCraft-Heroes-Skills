package me.marwzoor.skilladrenaline;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillAdrenaline extends PassiveSkill
{
	public SkillAdrenaline(Heroes instance)
	{
		super(instance, "Adrenaline");
		setDescription("Passive health regeneration during battle. (disabled on the server by default)");
		setTypes(new SkillType[] { SkillType.HEAL, SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		return super.getDescription();
	}
	
	public class SkillHeroListener implements Listener
	{
		public SkillAdrenaline skill;
		
		public SkillHeroListener(SkillAdrenaline skill)
		{
			this.skill=skill;
		}
		
		@EventHandler
		public void onEntityRegainHealthEvent(EntityRegainHealthEvent event)
		{
			if(event.getEntity() instanceof Player)
			{
				Hero hero = plugin.getCharacterManager().getHero((Player) event.getEntity());
				
				if(hero.isInCombat())
				{
					if(!hero.hasAccessToSkill(skill))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}
}
