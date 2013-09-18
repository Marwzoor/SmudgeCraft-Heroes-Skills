package net.smudgecraft.heroeslib.util;

import net.smudgecraft.heroeslib.HeroesLib;
import net.smudgecraft.heroeslib.companions.CompanionPlayer;
import net.smudgecraft.heroeslib.companions.Companions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;
import com.massivecraft.factions.FFlag;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.mcore.ps.PS;

public class PlayerUtils 
{
	
	//Checks if a player can damage another player, does not check if the target is invulnerable, that damage will be removed automatically by the invulnerability skill
	public static boolean damageCheck(Player attacker, LivingEntity target)
	{
		if(target instanceof Player)
		{
			Player pTarget = (Player)target;
			if(attacker.equals(pTarget))
				return false;
			//Check if any of the involved players are in a non-pvp zone
			if(BoardColls.get().getFactionAt(PS.valueOf(attacker.getLocation())).getFlag(FFlag.PVP)==false || BoardColls.get().getFactionAt(PS.valueOf(pTarget.getLocation())).getFlag(FFlag.PVP)==false)
				return false;
			
			Hero aHero = HeroesLib.heroes.getCharacterManager().getHero(attacker);
			Hero tHero = HeroesLib.heroes.getCharacterManager().getHero(pTarget);
			
			//Check if the players are in the same party
			if(aHero.hasParty() && tHero.hasParty())
			{
				if(aHero.getParty().isPartyMember(tHero))
					return false;
			}
			
			UPlayer aplayer = UPlayer.get(attacker);
			UPlayer tplayer = UPlayer.get(pTarget);
			
			//Do a check if the players factions are enemies
			if(aplayer.getFaction().getRelationTo(tplayer.getFaction()).equals(Rel.ENEMY))
				return true;
		}
		else
		{
			CompanionPlayer cp = Companions.getPlayerManager().getCompanionPlayer(attacker);
			if(!cp.hasSpecificCompanion(target))
				return true;
		}
		return false;
	}
	
	public static boolean areEnemies(Player aPlayer, Player bPlayer)
	{
		UPlayer aplayer = UPlayer.get(aPlayer);
		UPlayer tplayer = UPlayer.get(bPlayer);
		
		//Do a check if the players factions are enemies
		if(aplayer.getFaction().getRelationTo(tplayer.getFaction()).equals(Rel.ENEMY))
			return true;
		
		return false;
	}
	
	public static boolean isPvPZone(Location loc)
	{
		if(BoardColls.get().getFactionAt(PS.valueOf(loc)).getFlag(FFlag.PVP)==false)
			return true;
		return false;
	}
}
