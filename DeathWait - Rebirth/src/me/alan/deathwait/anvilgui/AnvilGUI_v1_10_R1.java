package me.alan.deathwait.anvilgui;

import java.util.List;
import me.alan.deathwait.ItemMaker;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.ChatMessage;
import net.minecraft.server.v1_10_R1.ContainerAnvil;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.PacketPlayOutOpenWindow;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AnvilGUI_v1_10_R1 implements AnvilGUI{
	
	ItemMaker im = new ItemMaker();
  
	private class FakeAnvil extends ContainerAnvil{
		
		public FakeAnvil(EntityHuman entityHuman){
			super(entityHuman.inventory, entityHuman.world, new BlockPosition(0, 0, 0), entityHuman);
		}
    
		public boolean a(EntityHuman entityHuman){
			return true;
		}
	}
  
	public void openAnvil(Player p, String name, List<String> lore){
		EntityPlayer entityPlayer = ((CraftPlayer)p).getHandle();
		FakeAnvil fakeAnvil = new FakeAnvil(entityPlayer);
		int containerId = entityPlayer.nextContainerCounter();
    
		entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage("Repairing", new Object[0]), 0));
    
		entityPlayer.activeContainer = fakeAnvil;
		entityPlayer.activeContainer.windowId = containerId;
		entityPlayer.activeContainer.addSlotListener(entityPlayer);
		entityPlayer.activeContainer = fakeAnvil;
		entityPlayer.activeContainer.windowId = containerId;
    
		Inventory inv = fakeAnvil.getBukkitView().getTopInventory();
		
		inv.setItem(0, im.createItem(Material.NAME_TAG, 0, name.replace("&", "/&").replace('§', '&'), lore, false));
		
	}
}
