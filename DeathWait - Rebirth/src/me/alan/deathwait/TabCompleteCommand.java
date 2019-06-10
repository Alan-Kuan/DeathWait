package me.alan.deathwait;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TabCompleteCommand implements TabCompleter{

	//TAB完成指令
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
					
		if(label.equals("dw")){
			
			if(!(sender instanceof Player)){
				return null;
			}
				
			Player p = (Player)sender;
			
			List<String> arguments = Arrays.asList(new String[] { "reload", "list", "set", "instant", "assistant" });
			List<String> reload = Arrays.asList(new String[] { "reload" });
			List<String> instant = Arrays.asList(new String[] { "instant" });
			List<String> list = Arrays.asList(new String[] { "list" });
			List<String> set = Arrays.asList(new String[] { "set" });
			List<String> assistant = Arrays.asList(new String[] { "assistant" });
			List<String> check = Arrays.asList(new String[] { "check" });
			
				
			if(args.length == 1){
				
				//只有這些需要權限
				if((p.hasPermission("dw.command"))){
					if("".startsWith(args[0])){
						return arguments;
					}
					if("reload".startsWith(args[0])){
						return reload;
					}
					if("list".startsWith(args[0])){
						return list;
					}
					if("set".startsWith(args[0])){
						return set;
					}
					if("instant".startsWith(args[0])){
						return instant;
					}
					if("assistant".startsWith(args[0])){
						return assistant;
					}
				}
				
				if("check".startsWith(args[0])){
					return check;
				}
				
			}
		}
		return null;
	}
}
