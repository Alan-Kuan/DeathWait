package me.alan.deathwait.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.alan.deathwait.Core;
import me.alan.deathwait.ErrorGen;

public class Data {

	private FileConfiguration data;
	private File file;
	private ErrorGen err;
	private Core core;
	
	public Data(Core core){
		
		this.core = core;
		
		err = new ErrorGen(core);
		
		file = new File(core.getDataFolder(), "data.yml");
		try{
			if(!file.exists()){
				file.createNewFile();
			}
		}catch(Exception e){
			e.printStackTrace();
			err.error("�b�ͦ�data.yml�ɥX�F���D");
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
			err.error("�b�ͦ�data.yml�ɥX�F���D");
		}
		
		try{
			data.load(file);
		}catch(Exception e){
			e.printStackTrace();
			err.error("�bŪ��data.yml�ɥX�F���D");
		}
	}
	
	public void save(){
		
		try{
			data.save(file);
		}catch(IOException e){
			e.printStackTrace();
			err.error("�b�x�sdata.yml�ɥX�F���D");
		}
	}
		
}