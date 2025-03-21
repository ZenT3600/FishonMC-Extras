package io.github.markassk.fishonmcextras.commands;

import com.mojang.brigadier.CommandDispatcher;
import io.github.markassk.fishonmcextras.FishOnMCExtrasClient;
import io.github.markassk.fishonmcextras.hud.HudRenderer;
import io.github.markassk.fishonmcextras.trackers.FishStreakTracker;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.*;

public class CommandRegistration {
    private static final HudRenderer HUD_RENDERER = FishOnMCExtrasClient.HUD_RENDERER;
    private static final FishStreakTracker FISH_STREAK = FishOnMCExtrasClient.FISH_STREAK;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("foe")
                .then(ClientCommandManager.literal("reset")
                        .executes(context -> {
                            HUD_RENDERER.resetStats();
                            context.getSource().sendFeedback(
                                    Text.literal("Fish fishHUDConfig reset locally")
                            );
                            return 1;
                        })
                )
                .then(ClientCommandManager.literal("reload")
                        .executes(context -> {
                            HUD_RENDERER.loadStats();
                            context.getSource().sendFeedback(
                                    Text.literal("HUD stats reloaded from config.")
                            );
                            return 1;
                        })
                )
				.then(ClientCommandManager.literal("dry")
						.executes(context -> {
							FISH_STREAK.showDrystreak(context);
							return 1;
						})
				)
				.then(ClientCommandManager.literal("track")
						.then(ClientCommandManager.literal("add")
								.then(ClientCommandManager.argument("keyword", StringArgumentType.string())
										.executes(context -> {
											FISH_STREAK.trackFish(context);
											return 1;
										})
								)
						)
						.then(ClientCommandManager.literal("delete")
								.then(ClientCommandManager.argument("keyword", StringArgumentType.string())
										.executes(context -> {
											FISH_STREAK.deleteFish(context);
											return 1;
										})
								)
						)
				)
        );
    }

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerCommands(dispatcher);
        });
    }
}
