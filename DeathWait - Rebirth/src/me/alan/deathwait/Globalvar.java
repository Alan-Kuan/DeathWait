package me.alan.deathwait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Globalvar {
	
	public boolean hasEssentials = false;
	
	public boolean hasEssentials(){
		return hasEssentials;
	}
	public void setEssentials(boolean hasEssentials){
		this.hasEssentials = hasEssentials;
	}
	
	public String Same = "";
	
	public boolean isSame(String s){
		if(s.equals(Same)){
			return true;
		}else{
			return false;
		}
	}
	public void setSame(String s){
		Same = s;
	}
	public void removeSame(){
		Same = "";
	}
	
	/************************************************************************************/
	public HashMap<Player, GameMode> gamemode = new HashMap<Player, GameMode>();
	public List<Player> nochoose = new ArrayList<Player>();
	public List<Player> turnpage = new ArrayList<Player>();
	public List<Player> killed = new ArrayList<Player>();
	public List<Player> isghost = new ArrayList<Player>();
	public HashMap<Player, Integer> ids = new HashMap<Player, Integer>();
	public HashMap<Player, Integer> limit_ids = new HashMap<Player, Integer>();
	public HashMap<Entity, Player> targetentity = new HashMap<Entity, Player>();
	/************************************************************************************/

	
	public GameMode getGameMode(Player p){
		return gamemode.get(p);
	}
	public void setGameMode(Player p, GameMode g){
		if(gamemode.containsKey(p)){
			gamemode.replace(p, g);
		}else{
			gamemode.put(p, g);
		}
	}
	public void removeGameMode(Player p){
		gamemode.remove(p);
	}
	
	public boolean isNoChoose(Player p){
		if(nochoose.contains(p)){
			return true;
		}else{
			return false;
		}
	}
	public void addNoChoose(Player p){
		nochoose.add(p);
	}
	public void removeNoChoose(Player p){
		nochoose.remove(p);
	}
	
	public boolean hasTurnedPage(Player p){
		if(turnpage.contains(p)){
			return true;
		}else{
			return false;
		}
	}
	public void addTurnPage(Player p){
		turnpage.add(p);
	}
	public void removeTurnPage(Player p){
		turnpage.remove(p);
	}
	
	public boolean isKilled(Player p){
		if(killed.contains(p)){
			return true;
		}else{
			return false;
		}
	}
	public void addKilled(Player p){
		killed.add(p);
	}
	public void removeKilled(Player p){
		killed.remove(p);
	}
	
	public boolean isGhost(Player p){
		if(isghost.contains(p)){
			return true;
		}else{
			return false;
		}
	}
	public void addGhost(Player p){
		isghost.add(p);
	}
	public void removeGhost(Player p){
		isghost.remove(p);
	}
	
	public boolean hasIds(Player p){
		if(ids.containsKey(p)){
			return true;
		}else{
			return false;
		}
	}
	public int getIds(Player p){
		return ids.get(p);
	}
	public void setIds(Player p, int id){
		ids.put(p, id);
	}
	public void removeIds(Player p){
		ids.remove(p);
	}
	
	public int getChoosingCountingDownId(Player p){
		return limit_ids.get(p);
	}
	public boolean haveChoosingCountingDownId(Player p){
		if(limit_ids.containsKey(p)){
			return true;
		}else{
			return false;
		}
	}
	public void giveChoosingCountingDownId(Player p, int id){
		if(limit_ids.containsKey(p)){
			limit_ids.replace(p, id);
		}else{
			limit_ids.put(p, id);
		}
	}
	public void removeChoosingCountingDownId(Player p){
		limit_ids.remove(p);
	}
		
	public boolean isTargetEntity(Entity target){
		if(targetentity.containsKey(target)){
			return true;
		}else{
			return false;
		}
	}
	public boolean isInTargetEntity(Player p){
		if(targetentity.containsValue(p)){
			return true;
		}else{
			return false;
		}
	}
	public Player getPlayerInTargetEntity(Entity target){
		return targetentity.get(target);
	}
	public Set<Entity> getTargetEntities(){
		return targetentity.keySet();
	}
	public void addTargetEntity(Entity target, Player p){
		targetentity.put(target, p);
	}
	public void removeTargetEntity(Entity target, Player p){
		targetentity.remove(target, p);
	}
	
}
