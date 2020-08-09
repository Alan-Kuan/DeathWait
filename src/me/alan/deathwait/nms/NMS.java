package me.alan.deathwait.nms;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public abstract interface NMS{
	
	public abstract void sendTitle(Player p, String text, int fade_in, int duration, int fade_out);
  
	public abstract void sendSubTitle(Player p, String text, int fade_in, int duration, int fade_out);
  
	public abstract void sendCommand(Player p, String text, String command);
  
	public abstract void sendExample(Player p, String text, String command);
  
	public abstract void sendLocation(Player p, String name, Location loc);
	
	//§ï¥Î Spigot ªº method
	public abstract void setSpectate(Player p, Entity ent);

}
