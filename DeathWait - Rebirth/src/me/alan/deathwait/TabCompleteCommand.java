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
			
			List<String> arguments = Arrays.asList(new String[] { "reload", "list", "set", "respawnitem", "hereitem" });
			List<String> re = Arrays.asList(new String[] { "reload", "respawnitem" });
			List<String> reload = Arrays.asList(new String[] { "reload" });
			List<String> respawnitem = Arrays.asList(new String[] { "respawnitem" });
			List<String> list = Arrays.asList(new String[] { "list" });
			List<String> set = Arrays.asList(new String[] { "set" });
			List<String> here = Arrays.asList(new String[] { "hereitem" });
			List<String> check = Arrays.asList(new String[] { "check" });
			
				
			if(args.length == 1){
				
				//只有這些需要權限
				if((p.hasPermission("dw.command"))){
					if("".startsWith(args[0])){
						return arguments;
					}
					if("re".startsWith(args[0])){
						return re;
					}
					if("reload".startsWith(args[0])){
						return reload;
					}
					if("respawnitem".startsWith(args[0])){
						return respawnitem;
					}
					if("list".startsWith(args[0])){
						return list;
					}
					if("set".startsWith(args[0])){
						return set;
					}
					if("hereitem".startsWith(args[0])){
						return here;
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
