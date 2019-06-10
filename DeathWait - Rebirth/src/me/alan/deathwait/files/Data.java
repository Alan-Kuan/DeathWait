package me.alan.deathwait.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.alan.deathwait.Core;
import me.alan.deathwait.WarningGen;

public class Data {

	private FileConfiguration data;
	private File file;
	private WarningGen warn;
	private Core core;
	
	public Data(Core core){
		
		this.core = core;
		
		warn = new WarningGen(core);
		
		file = new File(core.getDataFolder(), "data.yml");
		try{
			if(!file.exists()){
				file.createNewFile();
			}
		}catch(Exception e){
			e.printStackTrace();
			warn.Warn("在生成data.yml時出了問題");
		}
		
		data = new YamlConfiguration();
		
		load();
	}
	
	public FileConfiguration getConfig(){
		return data;
	}
	
	public void set(String path, Object object){
		data.set(path, object);
		
		save();
	}
	
	public void load(){
		
		file = new File(core.getDataFolder(), "data.yml");
		try{
			if(!file.exists()){
				file.createNewFile();
			}
		}catch(Exception e){
			e.printStackTrace();
			warn.Warn("在生成data.yml時出了問題");
		}
		
		try{
			data.load(file);
		}catch(Exception e){
			e.printStackTrace();
			warn.Warn("在讀取data.yml時出了問題");
		}
	}
	
	public void save(){
		
		try{
			data.save(file);
		}catch(IOException e){
			e.printStackTrace();
			warn.Warn("在儲存data.yml時出了問題");
		}
	}
		
}
