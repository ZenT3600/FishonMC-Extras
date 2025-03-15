package io.github.markassk.fishonmcextras.trackers;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.MinecraftClient;
import java.util.*;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.*;

public class FishStreakTracker {
	public static Map<String, Integer> trackedFish = new HashMap<>();
	
	public static void showOutput(CommandContext<FabricClientCommandSource> context, String text) {
		context.getSource().sendFeedback(Text.literal(text));
	}
	
	public static void processFish(String fishCaught) {
		trackedFish.forEach((fish, count) -> {
			if (fishCaught.toLowerCase().contains(fish.toLowerCase())) reset(fish);
			else increment(fish);
		});
	}
	
	public static void increment(String fish) {
		if (trackedFish.get(fish) != null) {
			trackedFish.replace(fish, trackedFish.get(fish) + 1);
		}
	}
	
	public static void reset(String fish) {
		if (trackedFish.get(fish) != null) {
			MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.of("'" + fish + "' drystreak ended! It took " + (trackedFish.get(fish) + 1) + " catches."), false);
			trackedFish.replace(fish, 0);
		}
	}
	
    public static void showDrystreak(CommandContext<FabricClientCommandSource> context) {
		showOutput(context, "--- Fish Drystreak Tracker ---");
		trackedFish.forEach((fish, count) -> showOutput(context, fish + " : " + count + " catches dry streak"));
    }
	
    public static void trackFish(CommandContext<FabricClientCommandSource> context) {
		String fish = StringArgumentType.getString(context, "keyword");
		trackedFish.put(fish, 0);
		showOutput(context, "Began tracking drystreak for '" + fish + "'");
	}
	
    public static void deleteFish(CommandContext<FabricClientCommandSource> context) {
		String fish = StringArgumentType.getString(context, "keyword");
		if (trackedFish.get(fish) != null) {
			trackedFish.remove(fish);
			showOutput(context, "Stopped tracking drystreak for '" + fish + "'");
		} else {
			showOutput(context, "Unable to stop tracking '" + fish + "' as it was not tracked prior");
		}
	}
}
