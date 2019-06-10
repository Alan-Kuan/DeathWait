package me.alan.deathwait.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import me.alan.deathwait.Core;
import me.alan.deathwait.WarningGen;

public class Config {
	
	private FileConfiguration config;
	private File file;
	private WarningGen warn;
	private Core core;
	
	public Config(Core core){
		
		this.core = core;
		
		warn = new WarningGen(core);
		
		file = new File(core.getDataFolder(), "config.yml");
		try{
			if(!file.exists())
			core.saveResource("config.yml", false);
		}catch (Exception e){
			e.printStackTrace();
			warn.Warn("�b�ͦ�config.yml�ɥX�F���D");
		}
		
		config = new YamlConfiguration();
		
		load();
	}
		
	public FileConfiguration getConfig(){
		return config;
	}
	
	public void load(){
		
		file = new File(core.getDataFolder(), "config.yml");
		try{
			if(!file.exists())
			core.saveResource("config.yml", false);
		}catch (Exception e){
			e.printStackTrace();
			warn.Warn("�b�ͦ�config.yml�ɥX�F���D");
		}
		
		try{
			config.load(file);
		}catch(Exception e){
			e.printStackTrace();
			warn.Warn("�bŪ��config.yml�ɥX�F���D");
		}
	}
	
	public void save(){
		
		try{
			config.save(file);
		}catch(IOException e){
			e.printStackTrace();
			warn.Warn("�b�x�sconfig.yml�ɥX�F���D");
		}
	}
	
}
