package me.alan.deathwait;

import java.io.File;
import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

//����error.yml
public class ErrorGen {

	public YamlConfiguration error = new YamlConfiguration();
	
	private Core Core;
	
	public ErrorGen(Core Core){
		
		this.Core = Core;
				
	}
	
	public void error(String warn){
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait] " + warn);
		
		File errorfile = new File(Core.getDataFolder(), "error.yml");
		if(!errorfile.exists()){
			try{
				errorfile.createNewFile();
			}catch(Exception e){
				e.printStackTrace();
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait] " + "�b�ͦ�error.yml�ɥX�F���D");
			}
		}
		error = YamlConfiguration.loadConfiguration(errorfile);

		error.set(LocalDateTime.now()+"", warn);
		try{
			error.save(errorfile);
		}catch(Exception e){
			e.printStackTrace();
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait] " + "�b�B�zerror.yml�ɥX�F���D");
		}
		
	}
		
}
