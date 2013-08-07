package me.marwzoor.skillrevitalizingword;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.*;
import com.herocraftonline.heroes.util.Messaging;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillRevitalizingWord extends ActiveSkill{
    FireworkEffectPlayer fireworkPlayer = new FireworkEffectPlayer();

    public SkillRevitalizingWord(Heroes plugin) {
        super(plugin, "RevitalizingWord");
        setDescription("You whisper the revitalizing word in the wind, healing every ally close to you");
        setUsage("/skill revitalizingword");
        setArgumentRange(0,0);
        setIdentifiers("skill revitalizingword");
        setTypes(SkillType.HEAL, SkillType.BUFF);

    }

    public ConfigurationSection getDefaultConfig(){
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.HEALTH.node(), 50);
        node.set(SkillSetting.RADIUS.node(), 10);
        node.set(SkillSetting.HEALTH_INCREASE.node(), 1);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] strings) {
        // Ensure that the hero is both in a party, and said party has members in it
        if(!hero.hasParty() || hero.getParty() == null){
            Messaging.send(hero.getPlayer(), "You need to be in a party with members to cast that");
            return SkillResult.CANCELLED;
        }
        Player player = hero.getPlayer();

        Location loc = player.getEyeLocation();
        FireworkEffect fireworkEffect = FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.PURPLE).build();
        try {
            fireworkPlayer.playFirework(loc.getWorld(), loc, fireworkEffect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(Hero iHero : hero.getParty().getMembers()){
            // Do not heal the caster, only the party members.
            if(iHero == hero){
                continue;
            }
            if(hero.getPlayer().getLocation().distance(iHero.getPlayer().getLocation()) > SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false)){
                continue;
            }
            double heal = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH, Integer.valueOf(50), false) +
                    (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_INCREASE, Double.valueOf(0.5), false) * hero.getSkillLevel(this)));            // I was unsure about "HEALTH_INCREASE" - currently this increases healing by one every level, meaning a level 50 would heal for 100hp.
            iHero.heal(heal);
            iHero.getPlayer().getWorld().playEffect(iHero.getPlayer().getLocation(), org.bukkit.Effect.HEART, 2000);
        }
        broadcast(hero.getPlayer().getLocation(), "[" + ChatColor.RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.GOLD + hero.getName()+ ChatColor.GRAY + " whispers the revitalizing word");
        return SkillResult.NORMAL;
    }

    @Override
    public String getDescription(Hero hero) {
        if(hero.hasAccessToSkill(this))
        {
            int heal = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH, Integer.valueOf(50), false) +
                    (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_INCREASE, Double.valueOf(0.5), false) * hero.getSkillLevel(this)));
            int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, Integer.valueOf(0), false) / 1000);
            int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, Integer.valueOf(0), false);
            return super.getDescription().replace("%H", heal + "").replace("%M", mana + "").replace("%CD", cooldown + "");
        }
        else{
            return super.getDescription().replace("%H", "X").replace("%M", "X").replace("%CD", "X");
        }
    }

    @Override
    public void init() {
        super.init();
    }
}
