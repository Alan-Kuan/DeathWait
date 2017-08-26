package me.alan.deathwait.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.alan.deathwait.Core;
import me.alan.deathwait.ErrorGen;


public class Spawns {

	private FileConfiguration spawns;
	private File file;
	private ErrorGen err;
	private Core core;
	
	public Spawns(Core core){
		
		this.core = core;
		
		err = new ErrorGen(core);
		
		file = new File(core.getDataFolder(), "spawns.yml");
		try{
			if(!file.exists()){
				file.createNewFile();
			}
		}catch(Exception e){
			e.printStackTrace();
			err.error("�b�ͦ�spawns.yml�ɥX�F���D");
		}
		
		spawns = new YamlConfiguration();
		
		load();
	}
	
	public FileConfiguration getConfig(){
		return spawns;
	}
	
	public void set(String path, Object object){
		spawns.set(path, object);
		
		save();
	}
	
	public void load(){
		
		file = new File(core.getDataFolder(), "spawns.yml");
		try{
			if(!file.exists()){
				file.createNewFile();
			}
		}catch(Exception e){
			e.printStackTrace();
			err.error("�b�ͦ�spawns.yml�ɥX�F���D");
		}
		
		try{
			spawns.load(file);
		}catch(Exception e){
			e.printStackTrace();
			err.error("�bŪ��spawns.yml�ɥX�F���D");
		}
	}
	
	public void save(){
		
		try{
			spawns.save(file);
		}catch(IOException e){
			e.printStackTrace();
			err.error("�b�x�sspawns.yml�ɥX�F���D");
		}
	}
		
}
