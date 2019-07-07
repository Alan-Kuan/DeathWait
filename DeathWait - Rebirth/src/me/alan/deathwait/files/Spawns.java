package me.alan.deathwait.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.alan.deathwait.Core;
import me.alan.deathwait.WarningGen;


public class Spawns {

	private FileConfiguration spawns;
	private File file;
	private Core core;
	
	public Spawns(Core core){
		
		this.core = core;
		
		file = new File(core.getDataFolder(), "spawns.yml");
		try{
			if(!file.exists()){
				file.createNewFile();
			}
		}catch(Exception e){
			e.printStackTrace();
			WarningGen.Warn("在生成spawns.yml時出了問題");
		}
		
		spawns = new YamlConfiguration();
		
		load();
	}
	
	public FileConfiguration getConfig(){
		return spawns;
	}
	
	public void load(){
		
		file = new File(core.getDataFolder(), "spawns.yml");
		try{
			if(!file.exists()){
				file.createNewFile();
			}
		}catch(Exception e){
			e.printStackTrace();
			WarningGen.Warn("在生成spawns.yml時出了問題");
		}
		
		try{
			spawns.load(file);
		}catch(Exception e){
			e.printStackTrace();
			WarningGen.Warn("在讀取spawns.yml時出了問題");
		}
	}
	
	public void save(){
		
		try{
			spawns.save(file);
		}catch(IOException e){
			e.printStackTrace();
			WarningGen.Warn("在儲存spawns.yml時出了問題");
		}
	}

	public void set(String path, Object obj){
		spawns.set(path, obj);
		
		save();
	}
	
}
