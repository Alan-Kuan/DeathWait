package me.alan.deathwait;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMaker{
	
	public ItemStack createItem(Material m, int damage, String name, List<String> lore, boolean color){
	
		if((m == null) || (m == Material.AIR) || (name == null)){
			return null;
		}
	  
		ItemStack item = new ItemStack(m);
		ItemMeta meta = item.getItemMeta();
		
		if(name != null){
			if(color){
				String show = name.replace('&', '§');
				meta.setDisplayName(show.replace("/§", "&"));
			}else{
				meta.setDisplayName(name);
			}
		}
		if(lore != null){
			if(color) {
				for(int i = 0; i <= lore.size() - 1; i++){
					String show = ((String)lore.get(i)).replace('&', '§');
					lore.set(i, show.replace("/§", "&"));
				}
			}
			meta.setLore(lore);
		}
		
		item.setItemMeta(meta);
		item.setDurability((short) damage);
		return item;
	}
}
