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
	
	String Header = ChatColor.GOLD + "[DeathWait]";
	
	private Config config;
	private Data data;
	private Spawns spawns;
	private NMS nms;
	private ListSpawns list;
	private ItemMaker im;
	private Globalvar GV;
	
	private Core core;
	
	public PlayerCommands(Core core){
		
		this.core = core;
		
		config = core.getConfigClass();
		data = core.getDataClass();
		spawns = core.getSpawnsClass();
		nms = core.getNMSClass();
		list = new ListSpawns(core, core.getPlayerFunctionsClass());
		im = new ItemMaker();
		GV = core.getGlobalvarClass();
		
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
		
		//  /dw check 不需要權限
		if(!p.hasPermission("dw.command")){
			
			if(args.length == 0 || !args[0].equals("check")){
				p.sendMessage(Header + ChatColor.DARK_RED + "你沒有權限!");
				return false;
			}
			
		}
		
		Boolean CL = config.getConfig().getBoolean("config.custom location");
		ItemStack Respawn = im.createItem(Material.getMaterial(config.getConfig().getString("config.respawn item.type")),
				config.getConfig().getInt("config.respawn item.damage"),
				config.getConfig().getString("config.respawn item.name"),
				config.getConfig().getStringList("config.respawn item.lore"), true);
		ItemStack Here = im.createItem(Material.getMaterial(config.getConfig().getString("config.respawn right here item.type")),
				config.getConfig().getInt("config.respawn right here item.damage"),
				config.getConfig().getString("config.respawn right here item.name"),
				config.getConfig().getStringList("config.respawn right here item.lore"), true);
				
		if(args.length == 0){
	        	
			p.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "              " + ChatColor.RESET + ChatColor.BLUE + ChatColor.BOLD + "[DeathWait死亡等待]" + ChatColor.RESET + ChatColor.AQUA + ChatColor.STRIKETHROUGH + "            ");
			p.sendMessage(ChatColor.DARK_AQUA + "作者: 小恩AlanKuan");
			p.sendMessage(ChatColor.DARK_GREEN + "版本: " + core.getDescription().getVersion());
			nms.sendCommand(p, "§b/dw reload - 重讀本插件 ", "/dw reload");
			nms.sendCommand(p, "§b/dw list - 查看所有復活點 ", "/dw list");
			nms.sendExample(p);
			nms.sendCommand(p, "§b/dw respawnitem [<數量>] - 獲得免等道具 ", "/dw respawnitem");
			nms.sendCommand(p, "§b/dw hereitem [<數量>] - 獲得原地復活道具 ", "/dw hereitem");
			nms.sendCommand(p, "§b/dw check - 查詢剩餘幾次免等額度 ", "/dw check");
			p.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "                                                ");
						
		}else if(args[0].equals("reload")){
	        	
			if(args.length > 1){
				p.sendMessage(Header + ChatColor.RED + "用法: /dw reload");
			}else{
				config.load();
				data.load();
				spawns.load();
				
				p.sendMessage(Header + ChatColor.GREEN + "已重讀完畢!");
			}
	        
		}else if(args[0].equals("set")){
	        
			if(CL.booleanValue()){
	        	  
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
	        					  
								if(!GV.isSame(name)){
	        						  
									GV.setSame(name);
									p.sendMessage(Header + ChatColor.DARK_RED + "這個名字的復活點已經有囉! 再次輸入會覆蓋原座標");
									return false;
	        						  
								}
	        					  
								spawns.set("spawns." + id + ".location", loc);
	        						        					
								GV.removeSame();
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
	        		  	
	        		  	GV.removeSame();
	        		  	nms.sendLocation(p, name, loc);
				}else{
					p.sendMessage(Header + ChatColor.RED + "用法: /dw set <復活點名稱>");
				}
	        	  
			}else{
				p.sendMessage(Header + ChatColor.RED + "你沒有開啟自訂重生點功能");
			}

		}else if(args[0].equals("list")){
	        	
			if(CL.booleanValue()){
				
				if(args.length > 1){
					p.sendMessage(Header + ChatColor.RED + "用法: /dw list");
				}else{
					
					list.List(p, 1);
					
				}
				
			}else{
				p.sendMessage(Header + ChatColor.RED + "你沒有開啟自訂重生點功能");
			}
	            
		}else if(args[0].equals("respawnitem")){
	        	
			if(args.length == 1){
	        		
				p.getInventory().addItem(Respawn);
				p.sendMessage(this.Header + ChatColor.GREEN + "已給1個免等復活道具");
	        		
			}else if(args.length == 2){
	        		
				try{
					
					int i = Integer.parseInt(args[1]);
	        			
					if(i <= 0){
						p.sendMessage(this.Header + ChatColor.RED + "為何你要輸入無法提供的數量!");
						return false;
					}
					
					while(i > 0){
						p.getInventory().addItem(Respawn);
						i--;
					}
					
				}catch (NumberFormatException e){
					p.sendMessage(this.Header + ChatColor.RED + "你確定你輸入的是數字嗎?");
					return false;
				}
				
				p.sendMessage(this.Header + ChatColor.GREEN + "已給" + args[1] + "個免等復活道具");
			}else{
				p.sendMessage(Header + ChatColor.RED + "用法: /dw respawnitem [<數量>]");
			}
	        
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
	        
		}else if(args[0].equals("hereitem")){
	        	
			if(args.length == 1){
				p.getInventory().addItem(Here);
				p.sendMessage(Header + ChatColor.GREEN + "已給1個原地復活道具");
			}else if(args.length == 2){
	        		
				try{
					
					int i = Integer.parseInt(args[1]);
					
					if(i <= 0){
						p.sendMessage(Header + ChatColor.RED + "為何你要輸入無法提供的數量!");
						return false;
					}
	        			
					while(i > 0){
						p.getInventory().addItem(Here);
						i--;
					}
					
				}catch (NumberFormatException e){
					p.sendMessage(Header + ChatColor.RED + "你確定你輸入的是數字嗎?");
					return false;
				}
				
				p.sendMessage(Header + ChatColor.GREEN + "已給" + args[1] + "個原地復活道具");
				
			}else{
				p.sendMessage(Header + ChatColor.RED + "用法: /dw hereitem [<數量>]");
			}
			
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
	        	
		}else if(args[0].equals("check")){
			
			if(args.length > 1){
				p.sendMessage(Header + ChatColor.RED + "用法: /dw check");
			}else{
				p.sendMessage(Header + ChatColor.GREEN + "剩餘" + data.getConfig().getInt("players." + p.getUniqueId() + ".quota") + "次免等額度");
			}
			
		}else{
			
			p.sendMessage(Header + ChatColor.RED + "指令錯誤!輸入/dw 獲取幫助");
			
		}
		
		return false;
	}
}
