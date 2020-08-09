package me.alan.deathwait.anvilgui;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.alan.deathwait.ItemMaker;
import net.minecraft.server.v1_13_R2.ChatMessage;
import net.minecraft.server.v1_13_R2.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.ContainerAnvil;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;

public class AnvilGUI_v1_13_R2 implements AnvilGUI{

	ItemMaker im = new ItemMaker();
  
	private class FakeAnvil extends ContainerAnvil{
		
		public FakeAnvil(EntityHuman entityHuman){
			super(entityHuman.inventory, entityHuman.world, new BlockPosition(0, 0, 0), entityHuman);

			this.checkReachable = false;
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

		inv.setItem(0, im.createItem(Material.NAME_TAG, 0, name.replace("&", "/&").replace('¡±', '&'), lore, false));
		
	}
}
