package me.marwzoor.skillshield;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.util.Messaging;

public class SkillHeroListener implements Listener
{
	private SkillShield skill;

    public SkillHeroListener(SkillShield skill)
    {
    	super();
        this.skill = skill;
    }
    
    @EventHandler
    public void onWeaponDamageEvent(WeaponDamageEvent event)
    {
    	if(event.isCancelled())
    		return;
    	if(!(event.getEntity() instanceof Player))
    		return;
    	Hero hero = skill.plugin.getCharacterManager().getHero(((Player)event.getEntity()));
    	if(!hero.hasAccessToSkill(skill))
    		return;
    	if(hero.getPlayer().getItemInHand().getTypeId() != 347)
    		return;
    	event.setDamage((int)(event.getDamage()*SkillConfigManager.getUseSetting(hero, skill, "percent", 0.5D, false)));
    	return;
    }
    
    @EventHandler
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event)
    {
    	Player player = event.getPlayer();
    	Hero hero = skill.plugin.getCharacterManager().getHero(player);
    	
    	int shielditem = SkillConfigManager.getUseSetting(hero, skill, "shield-item", 36, false);
    	
    	if(event.isCancelled())
    		return;
    	if(!hero.hasAccessToSkill(skill))
    		return;
    	if(event.getPlayer().getInventory().getItem(event.getNewSlot()) == null)
    		return;
    	if(event.getPlayer().getInventory().getItem(event.getNewSlot()).getTypeId() != shielditem)
    		return;
    	Messaging.send(event.getPlayer(), "You are now wielding a shield.");
    }
}
