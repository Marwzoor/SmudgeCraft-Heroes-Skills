package me.marwzoor.skillcounterattack;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;

public class SkillHeroListener implements Listener
{
	private SkillCounterAttack skill;

    public SkillHeroListener(SkillCounterAttack skill)
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
    	Player player = (Player)event.getEntity();
    	if(!skill.unPlayers.contains(player))
    		return;
    	event.setCancelled(true);
    	skill.damageEntity((LivingEntity)event.getAttackerEntity(), player, (int)SkillConfigManager.getUseSetting(skill.plugin.getCharacterManager().getHero(player), skill, SkillSetting.DAMAGE, 100.0D, false));
    	skill.unPlayers.remove(player);
    }
}
