package me.alan.deathwait.nms;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public abstract interface NMS{
	
	public abstract void sendTitle(Player paramPlayer, String paramString);
  
	public abstract void sendSubTitle(Player paramPlayer, String paramString, int fade_in, int duration, int fade_out);
  
	public abstract void sendCommand(Player paramPlayer, String paramString1, String paramString2);
  
	public abstract void sendExample(Player paramPlayer);
  
	public abstract void sendLocation(Player paramPlayer, String paramString, Location paramLocation);
  
	public abstract void setSpectate(Player paramPlayer, Entity paramEntity);

}
