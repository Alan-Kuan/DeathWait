package me.alan.deathwait;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.alan.deathwait.files.Config;
import me.alan.deathwait.files.Data;
import me.alan.deathwait.files.Spawns;
import me.alan.deathwait.nms.NMS;


public class PlayerCommands implements CommandExecutor{
	
	private Config config;
	private Data data;
	private Spawns spawns;
	private NMS nms;
	private ListSpawns list;
	private ItemMaker im;
	
	private Core core;
	
	public PlayerCommands(Core core){
		
		this.core = core;
		
		config = core.getConfigClass();
		data = core.getDataClass();
		spawns = core.getSpawnsClass();
		nms = core.getNMSClass();
		list = new ListSpawns(core, core.getPlayerFunctionsClass());
		im = new ItemMaker();
		
	}
		
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
	    
		if(!label.equals("dw")){
			return false;
		}
		
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.DARK_RED + "[DeathWait] " + "此指令僅能由玩家執行!");
			return false;
		}
		
		final Player p = (Player) sender;
		
		
		if(!p.hasPermission("dw.command")){
			
			//  /dw check 不需要權限
			if(args.length == 0 || !args[0].equals("check")){
				p.sendMessage(Global.Header + ChatColor.DARK_RED + "你沒有權限!");
				return false;
			}
			
		}
		
		boolean enable_custom_location = config.getConfig().getBoolean("config.enable custom location");
		
		ItemStack instant_respawn_item = im.createItem(Material.getMaterial(config.getConfig().getString("config.instant respawn item.type")),
				config.getConfig().getInt("config.instant respawn item.damage"),
				config.getConfig().getString("config.instant respawn item.name"),
				config.getConfig().getStringList("config.instant respawn item.lore"), true);
		
		ItemStack assistant_respawn_item = im.createItem(Material.getMaterial(config.getConfig().getString("config.assistant respawn item.type")),
				config.getConfig().getInt("config.assistant respawn item.damage"),
				config.getConfig().getString("config.assistant respawn item.name"),
				config.getConfig().getStringList("config.assistant respawn item.lore"), true);
				
		if(args.length == 0){
	        	
			p.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "              " + ChatColor.RESET + ChatColor.BLUE + ChatColor.BOLD + "[DeathWait死亡等待]" + ChatColor.RESET + ChatColor.AQUA + ChatColor.STRIKETHROUGH + "            ");
			p.sendMessage(ChatColor.DARK_AQUA + "作者: 小恩AlanKuan");
			p.sendMessage(ChatColor.DARK_GREEN + "版本: " + core.getDescription().getVersion());
			nms.sendCommand(p, "§b/dw reload - 重讀本插件 ", "/dw reload");
			nms.sendCommand(p, "§b/dw list - 查看所有復活點 ", "/dw list");
			nms.sendExample(p);
			nms.sendCommand(p, "§b/dw instant [<數量>] - 獲得免等道具 ", "/dw instant");
			nms.sendCommand(p, "§b/dw assistant [<數量>] - 獲得原地復活道具 ", "/dw assistant");
			nms.sendCommand(p, "§b/dw check - 查詢剩餘幾次免等額度 ", "/dw check");
			p.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "                                                ");
			
		}else if(args[0].equals("reload")){
	        	
			if(args.length > 1){
				p.sendMessage(Global.Header + ChatColor.RED + "用法: /dw reload");
			}else{
				config.load();
				data.load();
				spawns.load();
				
				p.sendMessage(Global.Header + ChatColor.GREEN + "已重讀完畢!");
			}
	        
		}else if(args[0].equals("set")){
	        
			if(enable_custom_location){
	        	  
				if(args.length > 1){
	        		
					String name = "";
	        		  
					for(int i = 1; i < args.length; i++){
						if(name != ""){
							name += " " + args[i].replace('&', '§').replace("/§", "&");
						}else{
							name = args[i].replace('&', '§').replace("/§", "&");
						}
					}
	        		  
					Location loc = p.getLocation();
	        		  
					if(spawns.getConfig().isSet("spawns")){
	        			  
						for(String id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){
	        				  
							if(spawns.getConfig().getString("spawns." + id + ".name").equals(name)){
	        					  
								if(!Global.getTempName().equals(name)){
	        						
									Global.setTempName(name);
									p.sendMessage(Global.Header + ChatColor.DARK_RED + "這個名字的復活點已經有囉! 再次輸入會覆蓋原座標");
									return false;
	        						  
								}
	        					  
								spawns.set("spawns." + id + ".location", loc);
	        						        					
								Global.setTempName("");
								nms.sendLocation(p, name, loc);
								return false;
							}
						}
					}
	        		  
					int id = 0;
					if(spawns.getConfig().isSet("last ID")){
						id = spawns.getConfig().getInt("last ID") + 1;
					}
	        		  
	        		  	spawns.set("spawns." + id + ".name", name);
	        		  	spawns.set("spawns." + id + ".location", loc);
	        		  	spawns.set("spawns." + id + ".icon.type", Material.GRASS.toString());
	        		  	spawns.set("spawns." + id + ".icon.data", 0);
	        		  	spawns.set("spawns." + id + ".icon.glowing", false);
	        		  	spawns.set("last ID", Integer.valueOf(id));
	        		  	
	        		  	Global.setTempName("");
	        		  	nms.sendLocation(p, name, loc);
				}else{
					p.sendMessage(Global.Header + ChatColor.RED + "用法: /dw set <復活點名稱>");
				}
	        	  
			}else{
				p.sendMessage(Global.Header + ChatColor.RED + "你沒有開啟自訂重生點功能");
			}

		}else if(args[0].equals("list")){
	        	
			if(enable_custom_location){
				
				if(args.length > 1){
					p.sendMessage(Global.Header + ChatColor.RED + "用法: /dw list");
				}else{
					
					list.List(p, 1);
					
				}
				
			}else{
				p.sendMessage(Global.Header + ChatColor.RED + "你沒有開啟自訂重生點功能");
			}
	            
		}else if(args[0].equals("instant")){
	        	
			if(args.length == 1){
	        		
				p.getInventory().addItem(instant_respawn_item);
				p.sendMessage(Global.Header + ChatColor.GREEN + "已給1個免等復活道具");
	        	
			}else if(args.length == 2){
	        		
				try{
					
					int i = Integer.parseInt(args[1]);
	        			
					if(i <= 0){
						p.sendMessage(Global.Header + ChatColor.RED + "為何你要輸入無法提供的數量!");
						return false;
					}
					
					while(i > 0){
						p.getInventory().addItem(instant_respawn_item);
						i--;
					}
					
				}catch (NumberFormatException e){
					p.sendMessage(Global.Header + ChatColor.RED + "你確定你輸入的是數字嗎?");
					return false;
				}
				
				p.sendMessage(Global.Header + ChatColor.GREEN + "已給" + args[1] + "個免等復活道具");
			}else{
				p.sendMessage(Global.Header + ChatColor.RED + "用法: /dw instant [<數量>]");
			}
	        
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
	        
		}else if(args[0].equals("assistant")){
	        	
			if(args.length == 1){
				p.getInventory().addItem(assistant_respawn_item);
				p.sendMessage(Global.Header + ChatColor.GREEN + "已給1個原地復活道具");
			}else if(args.length == 2){
	        	
				try{
					
					int i = Integer.parseInt(args[1]);
					
					if(i <= 0){
						p.sendMessage(Global.Header + ChatColor.RED + "為何你要輸入無法提供的數量!");
						return false;
					}
	        			
					while(i > 0){
						p.getInventory().addItem(assistant_respawn_item);
						i--;
					}
					
				}catch (NumberFormatException e){
					p.sendMessage(Global.Header + ChatColor.RED + "你確定你輸入的是數字嗎?");
					return false;
				}
				
				p.sendMessage(Global.Header + ChatColor.GREEN + "已給" + args[1] + "個原地復活道具");
				
			}else{
				p.sendMessage(Global.Header + ChatColor.RED + "用法: /dw assistant [<數量>]");
			}
			
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
	        	
		}else if(args[0].equals("check")){
			
			if(args.length > 1){
				p.sendMessage(Global.Header + ChatColor.RED + "用法: /dw check");
			}else{
				p.sendMessage(Global.Header + ChatColor.GREEN + "剩餘" + data.getConfig().getInt("players." + p.getUniqueId() + ".quota") + "次免等額度");
			}
			
		}else{
			
			p.sendMessage(Global.Header + ChatColor.RED + "指令錯誤!輸入/dw 獲取幫助");
			
		}
		
		return false;
	}
}
