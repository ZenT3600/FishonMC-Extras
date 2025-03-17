package io.github.markassk.fishonmcextras.common.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.component.DataComponentTypes;

public class MinecraftHelper {
	public static PlayerEntity getPlayer() {
		return MinecraftClient.getInstance().player;
	}
	
	public static PlayerInventory getPlayerInventory() {
		return getPlayer().getInventory();
	}
	
	public static ItemStack getPlayerSlot(int slot) {
		return getPlayerInventory().getStack(slot);
	}
	
	public static NbtCompound getNbt(ItemStack stack) {
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (component != null) {
			return component.getNbt();
		}
		return null;
	}
}