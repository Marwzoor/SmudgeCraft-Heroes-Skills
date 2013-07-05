package me.marwzoor.skillcounterattack;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillCounterAttack extends ActiveSkill {
	protected List<Player> unPlayers = new ArrayList<Player>();
	
	public SkillCounterAttack(Heroes plugin) {
		super(plugin,"CounterAttack");
		setDescription("Block an incoming hit and counter it with a blow dealing. DMG:%1 CD:%2 M:%3");
		setTypes(new SkillType[]{SkillType.BUFF});
		setIdentifiers(new String[]{"skill counterattack"});		
		Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this, plugin), plugin);
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(),Double.valueOf(100.0));
		node.set(SkillSetting.DAMAGE_INCREASE.node(),Double.valueOf(1.0));
		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(0));
		node.set(SkillSetting.MANA.node(), Integer.valueOf(0));
        return node;
	}
	
	@Override
	public String getDescription(Hero hero) {
		String description = super.getDescription();
		if (hero.hasAccessToSkill(this)) {
			double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Double.valueOf(100), false) + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Double.valueOf(1), false) * hero.getSkillLevel(this)));
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, Integer.valueOf(0), false) / 1000;
	        int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, Integer.valueOf(0), false);
	        return description.replace("%1", damage + "").replace("%2", cooldown + "").replace("%3", mana + "");
		} else {
			return description.replace("%1", "X").replace("%2", "X").replace("%3", "X");
		}
	}
	
	@Override
	public SkillResult use(Hero hero,String[] args) {
		Player player = hero.getPlayer();
		if(unPlayers.contains(player))
		{
			return SkillResult.FAIL;
		}
		unPlayers.add(player);
		return SkillResult.NORMAL;
	}
	
	public List<Player> getUnPlayers() {
		return unPlayers;
	}
	
	public class SkillHeroListener implements Listener {
		private SkillCounterAttack skill;
		private Heroes heroes;

	    public SkillHeroListener(SkillCounterAttack skill, Heroes heroes) {
	        this.skill = skill;
	        this.heroes = heroes;
	    }
	    
	    @EventHandler
	    public void onWeaponDamageEvent(WeaponDamageEvent event) {
	    	if(event.isCancelled()) {
	    		return;
	    	}
	    	if(!(event.getEntity() instanceof Player)) {
	    		return;
	    	}
	    	Player player = (Player)event.getEntity();
	    	if(!skill.getUnPlayers().contains(player)) {
	    		return;
	    	}
	    	event.setCancelled(true);
	    	skill.damageEntity((LivingEntity)event.getAttackerEntity(), player, (int)SkillConfigManager.getUseSetting(heroes.getCharacterManager().getHero(player), skill, SkillSetting.DAMAGE, Double.valueOf(100.0), false));
	    	skill.getUnPlayers().remove(player);
	    }
	}
}
