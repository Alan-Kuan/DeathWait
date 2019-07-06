package me.alan.deathwait.nms;

import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutCamera;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_12_R1.PlayerConnection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class v1_12_R1 implements NMS{
	
	public void sendTitle(Player p, String text, int fade_in, int duration, int fade_out){
		PlayerConnection c = ((CraftPlayer)p).getHandle().playerConnection;
    
	    PacketPlayOutTitle packetPlayOutTimes = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fade_in, duration, fade_out);
	    c.sendPacket(packetPlayOutTimes);
    
	    IChatBaseComponent title = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + text + "\"}");
	    PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, title);
	    c.sendPacket(packetPlayOutTitle);
	}
  
	public void sendSubTitle(Player p, String text, int fade_in, int duration, int fade_out){
		PlayerConnection c = ((CraftPlayer)p).getHandle().playerConnection;
    
		PacketPlayOutTitle packetPlayOutTimes = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fade_in, duration, fade_out);
		c.sendPacket(packetPlayOutTimes);
    
		IChatBaseComponent subtitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + text + "\"}");
		PacketPlayOutTitle packetPlayOutSubTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitle);
		c.sendPacket(packetPlayOutSubTitle);
	}
  
	public void sendCommand(Player p, String text, String command){
		PlayerConnection c = ((CraftPlayer)p).getHandle().playerConnection;
		IChatBaseComponent json = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + text + "\",\"extra\":[{\"text\":\"[\",\"bold\":\"true\"},{\"text\":\"����\",\"bold\":\"true\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + command + "\"}},{\"text\":\"]\",\"bold\":\"true\"}]}");
		PacketPlayOutChat packet = new PacketPlayOutChat(json);
		c.sendPacket(packet);
	}
  
	public void sendExample(Player p, String text, String command){
		PlayerConnection c = ((CraftPlayer)p).getHandle().playerConnection;
		IChatBaseComponent json = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + text + " \",\"extra\":[{\"text\":\"[\",\"bold\":\"true\"},{\"text\":\"�d��\",\"bold\":\"true\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + command + "\"}},{\"text\":\"]\",\"bold\":\"true\"}]}");
		PacketPlayOutChat packet = new PacketPlayOutChat(json);
		c.sendPacket(packet);
	}
  
	public void sendLocation(Player p, String name, Location loc){
		PlayerConnection c = ((CraftPlayer)p).getHandle().playerConnection;
		IChatBaseComponent json = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + ChatColor.GOLD + "[DeathWait]" + "��a���\�]�m�_���I \",\"extra\":[{\"text\":\"[\",\"bold\":\"true\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"��6�ҳB�@��: " + loc.getWorld().getName() + " \n��aX�y��: " + loc.getX() + " \n��aY�y��: " + loc.getY() + " \n��aZ�y��: " + loc.getZ() + "\"}},{\"text\":\"��r" + name + "\",\"bold\":\"true\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"��6�ҳB�@��: " + loc.getWorld().getName() + " \n��aX�y��: " + loc.getX() + " \n��aY�y��: " + loc.getY() + " \n��aZ�y��: " + loc.getZ() + "\"}},{\"text\":\"]\",\"bold\":\"true\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"��6�ҳB�@��: " + loc.getWorld().getName() + " \n��aX�y��: " + loc.getX() + " \n��aY�y��: " + loc.getY() + " \n��aZ�y��: " + loc.getZ() + "\"}}]}");
		PacketPlayOutChat packet = new PacketPlayOutChat(json);
		c.sendPacket(packet);
	}
  
	public void setSpectate(Player p, Entity ent){
		PacketPlayOutCamera packet = new PacketPlayOutCamera(((CraftEntity)ent).getHandle());
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
	}
	
}
