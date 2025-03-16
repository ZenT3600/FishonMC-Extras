package io.github.markassk.fishonmcextras.trackers;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import io.github.markassk.fishonmcextras.hud.HudRenderer;
import io.github.markassk.fishonmcextras.FishOnMCExtrasClient;
import net.minecraft.client.MinecraftClient;

public class InventoryFullTracker {
	private static int pings = 0;
	
	private static int getInventoryFillStatus() {
        int fullslots = 0;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            PlayerInventory inventory = player.getInventory();
            if (inventory != null) {
                for (int i = 0; i < 36; i++) {
                    if (!inventory.getStack(i).isEmpty()) {
                        fullslots++;
                    }
                }
            }
        }
        return 36 - fullslots;
    }
	
	private static void update() {
		HudRenderer.setEmptySlots(getInventoryFillStatus());
	}
	
	public static void ping() {
		pings++;
		if (pings >= FishOnMCExtrasClient.CONFIG.InventoryWarningToggles.delayTps) {
			update();
			pings = 0;
		}
	}
}