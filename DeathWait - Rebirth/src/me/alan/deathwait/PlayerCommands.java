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
			sender.sendMessage(ChatColor.DARK_RED + "[DeathWait] " + "�����O�ȯ�Ѫ��a����!");
			return false;
		}
		
		final Player p = (Player) sender;
		
		
		if(!p.hasPermission("dw.command")){
			
			//  /dw check ���ݭn�v��
			if(args.length == 0 || !args[0].equals("check")){
				p.sendMessage(Global.Header + ChatColor.DARK_RED + "�A�S���v��!");
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
	        	
			p.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "              " + ChatColor.RESET + ChatColor.BLUE + ChatColor.BOLD + "[DeathWait���`����]" + ChatColor.RESET + ChatColor.AQUA + ChatColor.STRIKETHROUGH + "            ");
			p.sendMessage(ChatColor.DARK_AQUA + "�@��: �p��AlanKuan");
			p.sendMessage(ChatColor.DARK_GREEN + "����: " + core.getDescription().getVersion());
			nms.sendCommand(p, "��b/dw reload - ��Ū������ ", "/dw reload");
			nms.sendCommand(p, "��b/dw list - �d�ݩҦ��_���I ", "/dw list");
			nms.sendExample(p);
			nms.sendCommand(p, "��b/dw instant [<�ƶq>] - ��o�K���D�� ", "/dw instant");
			nms.sendCommand(p, "��b/dw assistant [<�ƶq>] - ��o��a�_���D�� ", "/dw assistant");
			nms.sendCommand(p, "��b/dw check - �d�߳Ѿl�X���K���B�� ", "/dw check");
			p.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "                                                ");
			
		}else if(args[0].equals("reload")){
	        	
			if(args.length > 1){
				p.sendMessage(Global.Header + ChatColor.RED + "�Ϊk: /dw reload");
			}else{
				config.load();
				data.load();
				spawns.load();
				
				p.sendMessage(Global.Header + ChatColor.GREEN + "�w��Ū����!");
			}
	        
		}else if(args[0].equals("set")){
	        
			if(enable_custom_location){
	        	  
				if(args.length > 1){
	        		
					String name = "";
	        		  
					for(int i = 1; i < args.length; i++){
						if(name != ""){
							name += " " + args[i].replace('&', '��').replace("/��", "&");
						}else{
							name = args[i].replace('&', '��').replace("/��", "&");
						}
					}
	        		  
					Location loc = p.getLocation();
	        		  
					if(spawns.getConfig().isSet("spawns")){
	        			  
						for(String id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){
	        				  
							if(spawns.getConfig().getString("spawns." + id + ".name").equals(name)){
	        					  
								if(!Global.getTempName().equals(name)){
	        						
									Global.setTempName(name);
									p.sendMessage(Global.Header + ChatColor.DARK_RED + "�o�ӦW�r���_���I�w�g���o! �A����J�|�л\��y��");
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
					p.sendMessage(Global.Header + ChatColor.RED + "�Ϊk: /dw set <�_���I�W��>");
				}
	        	  
			}else{
				p.sendMessage(Global.Header + ChatColor.RED + "�A�S���}�Ҧۭq�����I�\��");
			}

		}else if(args[0].equals("list")){
	        	
			if(enable_custom_location){
				
				if(args.length > 1){
					p.sendMessage(Global.Header + ChatColor.RED + "�Ϊk: /dw list");
				}else{
					
					list.List(p, 1);
					
				}
				
			}else{
				p.sendMessage(Global.Header + ChatColor.RED + "�A�S���}�Ҧۭq�����I�\��");
			}
	            
		}else if(args[0].equals("instant")){
	        	
			if(args.length == 1){
	        		
				p.getInventory().addItem(instant_respawn_item);
				p.sendMessage(Global.Header + ChatColor.GREEN + "�w��1�ӧK���_���D��");
	        	
			}else if(args.length == 2){
	        		
				try{
					
					int i = Integer.parseInt(args[1]);
	        			
					if(i <= 0){
						p.sendMessage(Global.Header + ChatColor.RED + "����A�n��J�L�k���Ѫ��ƶq!");
						return false;
					}
					
					while(i > 0){
						p.getInventory().addItem(instant_respawn_item);
						i--;
					}
					
				}catch (NumberFormatException e){
					p.sendMessage(Global.Header + ChatColor.RED + "�A�T�w�A��J���O�Ʀr��?");
					return false;
				}
				
				p.sendMessage(Global.Header + ChatColor.GREEN + "�w��" + args[1] + "�ӧK���_���D��");
			}else{
				p.sendMessage(Global.Header + ChatColor.RED + "�Ϊk: /dw instant [<�ƶq>]");
			}
	        
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
	        
		}else if(args[0].equals("assistant")){
	        	
			if(args.length == 1){
				p.getInventory().addItem(assistant_respawn_item);
				p.sendMessage(Global.Header + ChatColor.GREEN + "�w��1�ӭ�a�_���D��");
			}else if(args.length == 2){
	        	
				try{
					
					int i = Integer.parseInt(args[1]);
					
					if(i <= 0){
						p.sendMessage(Global.Header + ChatColor.RED + "����A�n��J�L�k���Ѫ��ƶq!");
						return false;
					}
	        			
					while(i > 0){
						p.getInventory().addItem(assistant_respawn_item);
						i--;
					}
					
				}catch (NumberFormatException e){
					p.sendMessage(Global.Header + ChatColor.RED + "�A�T�w�A��J���O�Ʀr��?");
					return false;
				}
				
				p.sendMessage(Global.Header + ChatColor.GREEN + "�w��" + args[1] + "�ӭ�a�_���D��");
				
			}else{
				p.sendMessage(Global.Header + ChatColor.RED + "�Ϊk: /dw assistant [<�ƶq>]");
			}
			
			p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
	        	
		}else if(args[0].equals("check")){
			
			if(args.length > 1){
				p.sendMessage(Global.Header + ChatColor.RED + "�Ϊk: /dw check");
			}else{
				p.sendMessage(Global.Header + ChatColor.GREEN + "�Ѿl" + data.getConfig().getInt("players." + p.getUniqueId() + ".quota") + "���K���B��");
			}
			
		}else{
			
			p.sendMessage(Global.Header + ChatColor.RED + "���O���~!��J/dw ������U");
			
		}
		
		return false;
	}
}
