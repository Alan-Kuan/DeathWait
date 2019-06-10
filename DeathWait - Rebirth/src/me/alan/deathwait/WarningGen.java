package me.alan.deathwait;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

//產生error.txt
public class WarningGen {

	private Core core;
	
	public WarningGen(Core core){
		
		this.core = core;
				
	}
	
	public void Warn(String warning){

		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait] " + warning);
		
		
		String file_path = core.getDataFolder() + "/error.txt";
		
		Date now = new Date();
		
		DateFormat df = new SimpleDateFormat("[yyyy/MM/dd hh:mm:ss]");
		
		String current_time = df.format(now);
		
		try{
			
			PrintStream stream = new PrintStream(new FileOutputStream(file_path, true));
			
			stream.println(current_time + ": " + warning);
			
			stream.flush();
			
			stream.close();
			
		}catch(IOException e){
			
			e.printStackTrace();
			
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait] 在寫入error.txt時出了問題");
			
		}
		
		
	}
	
}
