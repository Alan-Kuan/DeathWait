package me.alan.deathwait;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class Global {

	public static String Header = ChatColor.GOLD + "[DeathWait]";
	
	public static String version = "";
	
	public static boolean hasEssentials = false;
	
	//用以確認相同復活點名稱是否被連續重複輸入
	private static String temp_name = "";
	
	private static HashMap<Player, GameMode> gamemode = new HashMap<Player, GameMode>();
	
	private static List<Player> nochoose = new ArrayList<Player>();
	
	private static List<Player> turnpage = new ArrayList<Player>();
	
	private static List<Player> ghost = new ArrayList<Player>();
	
	private static HashMap<Player, BukkitTask> countdown_task = new HashMap<Player, BukkitTask>();
	
	private static HashMap<Player, BukkitTask> time_limit_task = new HashMap<Player, BukkitTask>();
	
	//記錄玩家的等待秒數
	private static HashMap<Player, Integer> wait = new HashMap<Player, Integer>();
	
	//記錄玩家困在哪個生物中
	private static HashMap<Entity, Player> target_entity = new HashMap<Entity, Player>();
	
	
	public static String getTempName(){
		return temp_name;
	}
	public static void setTempName(String name){
		temp_name = name;
	}
	
	public static GameMode getGameMode(Player p){
		return gamemode.get(p);
	}
	public static void setGameMode(Player p, GameMode g){
		if(gamemode.containsKey(p)){
			gamemode.replace(p, g);
		}else{
			gamemode.put(p, g);
		}
	}
	public static void removeGameMode(Player p){
		gamemode.remove(p);
	}
	
	public static boolean didNotChoose(Player p){
		return nochoose.contains(p);
	}
	public static void addNoChoose(Player p){
		nochoose.add(p);
	}
	public static void removeNoChoose(Player p){
		nochoose.remove(p);
	}
	
	public static boolean hasTurnedPage(Player p){
		return turnpage.contains(p);
	}
	public static void addTurnPage(Player p){
		turnpage.add(p);
	}
	public static void removeTurnPage(Player p){
		turnpage.remove(p);
	}
	
	public static boolean isGhost(Player p){
		return ghost.contains(p);
	}
	public static void addGhost(Player p){
		ghost.add(p);
	}
	public static void removeGhost(Player p){
		ghost.remove(p);
	}
	
	public static boolean hasCountdownTask(Player p) {
		return countdown_task.containsKey(p);
	}
	public static void addCountdownTask(Player p, BukkitTask task) {
		countdown_task.put(p, task);
	}
	public static void cancelCountdownTask(Player p){
		countdown_task.get(p).cancel();
		countdown_task.remove(p);
	}
	
	public static void addTimeLimitTask(Player p, BukkitTask task) {
		time_limit_task.put(p, task);
	}
	public static void cancelTimeLimitTask(Player p){
		time_limit_task.get(p).cancel();
		time_limit_task.remove(p);
	}
	
	public static boolean hasLeftWaitingTimes(Player p){
		return wait.containsKey(p);
	}
	public static int getLeftWaitingTimes(Player p){
		return wait.get(p);
	}
	public static void setLeftWaitingTimes(Player p, int time){
		if(wait.containsKey(p)){
			wait.replace(p, time);
		}else{
			wait.put(p, time);
		}
	}
	public static void removeLeftWaitingTimes(Player p){
		wait.remove(p);
	}
	
	
	public static boolean isTargetEntity(Entity target){
		return target_entity.containsKey(target);
	}
	public static boolean isInTargetEntity(Player p){
		return target_entity.containsValue(p);
	}
	public static Player getPlayerInTargetEntity(Entity target){
		return target_entity.get(target);
	}
	public static Set<Entity> getTargetEntities(){
		return target_entity.keySet();
	}
	public static void addTargetEntity(Entity target, Player p){
		target_entity.put(target, p);
	}
	public static void removeTargetEntity(Entity target, Player p){
		target_entity.remove(target, p);
	}
	
}
