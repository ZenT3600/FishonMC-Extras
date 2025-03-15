package io.github.markassk.fishonmcextras.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.markassk.fishonmcextras.FishOnMCExtrasClient;
import io.github.markassk.fishonmcextras.common.handler.LookTickHandler;
import io.github.markassk.fishonmcextras.config.FishOnMCExtrasConfig;
import io.github.markassk.fishonmcextras.trackers.FishStreakTracker;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.util.Identifier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.text.NumberFormat;
import java.util.Locale;
import java.text.ParseException;

public class HudRenderer implements HudRenderCallback {
    private final MinecraftClient client = MinecraftClient.getInstance();

    // Current tracker caught
    private int fishCaughtCount = 0;
    private float totalXP = 0.0f;
    private float totalValue = 0.0f;
    private Map<String, Integer> variantCounts = new HashMap<>();
    private Map<String, Integer> rarityCounts = new HashMap<>();
    private Map<String, Integer> sizeCounts = new HashMap<>();
    private String newestFish = null;

    //Current active timer stats
    private long activeTime = 0;
    private long lastFishCaughtTime = 0;
    private long lastUpdateTime = System.currentTimeMillis();
    private boolean timerPaused = true;

    // All-time caught
    private int allFishCaughtCount = 0;
    private float allTotalXP = 0.0f;
    private float allTotalValue = 0.0f;
    private Map<String, Integer> allRarityCounts = new HashMap<>();
    private Map<String, Integer> allVariantCounts = new HashMap<>();
    private Map<String, Integer> allSizeCounts = new HashMap<>();

    // Pet
    private static String currentPet = null;
	private static int petSlot = 0;
    private static float xp_cur = 0f;
    private static float xp_need = 0f;


    // Stats to export converter
    private FishStatsData exportStats() {
        FishStatsData data = new FishStatsData();
        data.fishCaughtCount = this.fishCaughtCount;
        data.totalXP = this.totalXP;
        data.totalValue = this.totalValue;
        data.variantCounts = new HashMap<>(this.variantCounts);
        data.rarityCounts = new HashMap<>(this.rarityCounts);
        data.sizeCounts = new HashMap<>(this.sizeCounts);
        data.newestFish = this.newestFish;

        data.activeTime = this.activeTime;
        data.lastFishCaughtTime = this.lastFishCaughtTime;
        data.timerPaused = this.timerPaused;

        data.allFishCaughtCount = this.allFishCaughtCount;
        data.allTotalXP = this.allTotalXP;
        data.allTotalValue = this.allTotalValue;
        data.allVariantCounts = new HashMap<>(this.allVariantCounts);
        data.allRarityCounts = new HashMap<>(this.allRarityCounts);
        data.allSizeCounts = new HashMap<>(this.allSizeCounts);
		data.trackedFish = FishStreakTracker.trackedFish;

        data.equippedPet = currentPet;
		data.petSlot = petSlot;
		data.xp_cur = xp_cur;
		data.xp_need = xp_need;
		
        return data;
    }

    // Save fishHUDConfig to a config file
    public void saveStats() {
        try {
            FishStatsData data = exportStats();
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path filePath = configDir.resolve("FishOnMCExtrasStats.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(data);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load saved fishHUDConfig from previous session
    public void loadStats() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path filePath = configDir.resolve("FishOnMCExtrasStats.json");
            if (!Files.exists(filePath)) return;
            String json = Files.readString(filePath, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            FishStatsData data = gson.fromJson(json, FishStatsData.class);
            // Import fishHUDConfig from loaded data
            this.fishCaughtCount = data.fishCaughtCount;
            this.totalXP = data.totalXP;
            this.totalValue = data.totalValue;
            this.variantCounts = data.variantCounts;
            this.rarityCounts = data.rarityCounts;
            this.sizeCounts = data.sizeCounts;
            this.newestFish = data.newestFish;
			FishStreakTracker.trackedFish = data.trackedFish;

            this.allFishCaughtCount = data.allFishCaughtCount;
            this.allTotalXP = data.allTotalXP;
            this.allTotalValue = data.allTotalValue;
            this.allVariantCounts = data.allVariantCounts;
            this.allRarityCounts = data.allRarityCounts;
            this.allSizeCounts = data.allSizeCounts;

            if (data.equippedPet != null && !data.equippedPet.isEmpty()) {
                currentPet = data.equippedPet;
				xp_cur = data.xp_cur;
				xp_need = data.xp_need;
				petSlot = data.petSlot;
                System.out.println("[EXTRAS] Loaded saved pet: " + currentPet);
            }

            this.activeTime = data.activeTime;
            this.lastFishCaughtTime = data.lastFishCaughtTime;
            this.timerPaused = data.timerPaused;

            // Initialize lastUpdateTime on load
            this.lastUpdateTime = System.currentTimeMillis();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Reset currently tracked fishHUDConfig
    public void resetStats() {
        fishCaughtCount = 0;
        totalXP = 0.0f;
        totalValue = 0.0f;
        variantCounts.clear();
        rarityCounts.clear();
        sizeCounts.clear();
        newestFish = null;
		FishStreakTracker.trackedFish = new HashMap<String, Integer>();

        activeTime = 0;
        lastFishCaughtTime = 0;
        timerPaused = false;
        lastUpdateTime = System.currentTimeMillis();

        FishOnMCExtrasClient.fishTracker.reset();

        System.out.println("RESET FOE TRACKER");
        this.saveStats();
        // Force HUD update after resetting fishHUDConfig
        forceHudUpdate();
    }

    // Forces a hud update after a reset
    private void forceHudUpdate() {
        client.execute(() -> client.inGameHud.setOverlayMessage(Text.of("HUD Updated"), false));
    }


    // Updates whats shown on the HUD
    public void updateFishHUD(float xp, float value, String variant, String rarity, String size, String representation) {
        // Alltime caught
        this.allFishCaughtCount++;
        this.allTotalXP += xp;
        this.allTotalValue += value;
        this.allSizeCounts.put(size, allSizeCounts.getOrDefault(size, 0) + 1);
        this.allVariantCounts.put(variant, allVariantCounts.getOrDefault(variant, 0) + 1);
        this.allRarityCounts.put(rarity, allRarityCounts.getOrDefault(rarity, 0) + 1);

        this.lastFishCaughtTime = System.currentTimeMillis();
        this.timerPaused = false;

        // Current tracker caught
        this.fishCaughtCount++;
        this.totalXP += xp;
        this.totalValue += value;
        this.sizeCounts.put(size, sizeCounts.getOrDefault(size, 0) + 1);
        this.variantCounts.put(variant, variantCounts.getOrDefault(variant, 0) + 1);
        this.rarityCounts.put(rarity, rarityCounts.getOrDefault(rarity, 0) + 1);
        this.newestFish = representation;

        this.saveStats();
    }

    private void drawHudLine(
            DrawContext context,
            TextRenderer textRenderer,
            String prefix, // Symbol or text (e.g., "\uF033", "ᴀᴅᴜʟᴛ")
            int count,
            int totalFish,
            boolean isPercentageEnabled, // e.g., showRarityPercentages
            int color,
            int scaledX,
            int[] scaledYHolder,
            boolean shadows,
            int lineHeight
    ) {
        String text = prefix + " " + count;
        if (isPercentageEnabled && totalFish > 0) {
            float percentage = (count * 100.0f) / totalFish;
            text += String.format(" (%.1f%%)", percentage);
        }

        context.drawText(textRenderer, text, scaledX, scaledYHolder[0], color, shadows);
        scaledYHolder[0] += lineHeight; // Uniform line spacing
    }

    public static void setCurrentPet(String petName) {
        currentPet = petName;
    }

    public static void setXpNeed(float xp) {
        xp_need = xp;
    }

    public static void setPetSlot(int slot) {
        petSlot = slot;
    }

    public static void setXpCur(float xp) {
        xp_cur = xp;
    }

    public static void clearCurrentPet() {
        currentPet = null;
    }

    // In HudRenderer.java

    private void renderNoPetWarning(DrawContext context) {
        FishOnMCExtrasConfig config = FishOnMCExtrasClient.CONFIG;
        if (!config.petWarningHUDConfig.enableWarning || currentPet != null) return;

        TextRenderer textRenderer = client.textRenderer;
        String warningText = "NO PET EQUIPPED!";

        context.getMatrices().push();
        try {
            float scale = config.petWarningHUDConfig.warningFontSize / 10f;
            context.getMatrices().scale(scale, scale, 1f);

            // Gets screen size
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            // Position calculation
            int textWidth = textRenderer.getWidth(warningText);
            int x = (int) (((float) screenWidth / 2 - textWidth * scale / 2) / scale);
            int y = (int) ((screenHeight - 100) / scale);

            // Flashing effect
            int color = config.petWarningHUDConfig.warningColor;
            if (config.petWarningHUDConfig.flashWarning) {
                float alpha = (float) (Math.sin(System.currentTimeMillis() / 200.0) * 0.5 + 0.5);
                color = (color & 0x00FFFFFF) | ((int) (alpha * 255) << 24);
            }

            context.drawText(textRenderer, warningText, x, y, color, config.petWarningHUDConfig.petWarningHUDShadows);
        } finally {
            context.getMatrices().pop();
        }
    }

    private void renderCurrentPet(DrawContext context) {
        if (currentPet == null) return;

        TextRenderer textRenderer = client.textRenderer;
        FishOnMCExtrasConfig config = FishOnMCExtrasClient.CONFIG;
        String text = "Active Pet: " + currentPet;

        context.getMatrices().push();
        try {
            // Use configurable font size
            float scale = config.petActiveHUDConfig.petActiveFontSize / 10f;
            context.getMatrices().scale(scale, scale, 1f);

            // Convert percentage positions
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            int baseX = (int) (screenWidth * (config.petActiveHUDConfig.petHUDX / 100f));
            int baseY = (int) (screenHeight * (config.petActiveHUDConfig.petHUDY / 100f));

            // Adjust for scaling
            int scaledX = (int) (baseX / scale);
            int scaledY = (int) (baseY / scale);

            // Draw pet text with configurable color
            context.drawText(textRenderer, text, scaledX, scaledY, config.petActiveHUDConfig.petActiveColor, config.petActiveHUDConfig.petActiveHUDShadows);
			
			if (config.petActiveHUDConfig.petActiveVerbose) {
				// Draw pet xp
				NumberFormat nf = NumberFormat.getCompactNumberInstance(Locale.UK, NumberFormat.Style.SHORT);
				nf.setMinimumFractionDigits(0);
				nf.setMaximumFractionDigits(3);
				
				int lineHeight = (int) (textRenderer.fontHeight + (2 / scale) + 2);
				Identifier barBackground = Identifier.of("minecraft", "textures/gui/sprites/hud/experience_bar_background.png");
				Identifier barFilled = Identifier.of("minecraft", "textures/gui/sprites/hud/experience_bar_progress.png");
				context.drawTexture(barBackground, scaledX, scaledY + lineHeight, 0, 0, 182, 5, 182, 5);
				int pixels = (int) (182 / (xp_need / xp_cur));
				pixels = pixels > 182 ? 182 : pixels;
				context.drawTexture(barFilled, scaledX, scaledY + lineHeight, pixels, 5, 0, 0, pixels, 5, 182, 5);
            
				String xpText = nf.format(xp_cur) + " / " + nf.format(xp_need);
				int textX = (int) (scaledX + (182 / 2) - (textRenderer.getWidth(xpText) / 2));
				int textY = scaledY + lineHeight - 2;
				/*
				context.drawText(textRenderer, xpText, textX - 1, textY - 1, 0x000000, config.petActiveHUDConfig.petActiveHUDShadows);
				context.drawText(textRenderer, xpText, textX + 1, textY + 1, 0x000000, config.petActiveHUDConfig.petActiveHUDShadows);
				*/
				context.drawText(textRenderer, xpText, textX, textY, 0xFFFFFF, config.petActiveHUDConfig.petActiveHUDShadows);
				
			}
        } finally {
            context.getMatrices().pop();
        }
    }



    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        FishOnMCExtrasConfig config = FishOnMCExtrasConfig.getConfig();

        // Calculate time delta
        long currentTime = System.currentTimeMillis();
        long delta = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        if (!timerPaused) {
            activeTime += delta;
        }

        long timeSinceLastFish = currentTime - lastFishCaughtTime;
        if (timeSinceLastFish > TimeUnit.SECONDS.toMillis(config.fishHUDConfig.fishHUDAutoPause)) {
            timerPaused = true;
        }


        if (config.fishHUD){
            TextRenderer textRenderer = client.textRenderer;

            // First declare display variables
            int displayFishCaughtCount = config.trackTimed ? fishCaughtCount : allFishCaughtCount;
            float displayTotalXP = config.trackTimed ? totalXP : allTotalXP;
            float displayTotalValue = config.trackTimed ? totalValue : allTotalValue;
            Map<String, Integer> displayVariantCounts = config.trackTimed ? variantCounts : allVariantCounts;
            Map<String, Integer> displayRarityCounts = config.trackTimed ? rarityCounts : allRarityCounts;
            Map<String, Integer> displaySizeCounts = config.trackTimed ? sizeCounts : allSizeCounts;


            // Push matrix FIRST
            drawContext.getMatrices().push();
            try {
                // Get screen size
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();

                // Convert percentage config values to screen coordinates
                float xPercent = config.fishHUDConfig.fishHUDX / 100f;
                float yPercent = config.fishHUDConfig.fishHUDY / 100f;

                // Calculate base positions relative to screen size
                int baseX = (int) (screenWidth * xPercent);
                int baseY = (int) (screenHeight * yPercent);

                // Scaling setup
                boolean shadows = config.fishHUDConfig.fishHUDShadows;
                int fontSize = config.fishHUDConfig.fishHUDFontSize;
                float scale = fontSize / 10.0f;
                drawContext.getMatrices().scale(scale, scale, 1f);

                int padding = 2;
                int scaledX = (int) (baseX / scale);
                int[] scaledYHolder = {(int) (baseY / scale)};
                int lineHeight = (int) (textRenderer.fontHeight + (padding / scale));

                // Now use the display variables
                drawContext.drawText(textRenderer,
                        "Fish Caught: " + displayFishCaughtCount,
                        scaledX, scaledYHolder[0], config.fishHUDConfig.fishHUDColorConfig.fishHUDCaughtColor, shadows
                );
                scaledYHolder[0] += lineHeight;

                // Display the timer and fish/hour if enabled in config
                if (config.trackTimed) {
                    long timeSinceResetMillis = activeTime;

                    if (config.fishHUDToggles.showTimeSinceReset) {
                        long hours = TimeUnit.MILLISECONDS.toHours(timeSinceResetMillis);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeSinceResetMillis) % 60;
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeSinceResetMillis) % 60;
                        String timeSinceReset = String.format("Time Counted: %02d:%02d:%02d", hours, minutes, seconds);
                        drawContext.drawText(textRenderer, timeSinceReset, scaledX, scaledYHolder[0], config.fishHUDConfig.fishHUDColorConfig.fishHUDTimerColor, shadows);
                        scaledYHolder[0] += lineHeight;
                    }

                    if (config.fishHUDToggles.showFishPerHour) {
                        double fishPerHour = (fishCaughtCount / (timeSinceResetMillis / 3600000.0));
                        drawContext.drawText(textRenderer, "Fish/Hour: " + String.format("%.1f", fishPerHour), scaledX, scaledYHolder[0], config.fishHUDConfig.fishHUDColorConfig.fishHUDTimerColor, shadows);
                        scaledYHolder[0] += lineHeight;
                    }
                }

                drawContext.drawText(textRenderer, "Total XP: " + displayTotalXP, scaledX, scaledYHolder[0], config.fishHUDConfig.fishHUDColorConfig.fishHUDXPColor, shadows);
                scaledYHolder[0] += lineHeight;
                drawContext.drawText(textRenderer, "Total Value: " + displayTotalValue + "$", scaledX, scaledYHolder[0], config.fishHUDConfig.fishHUDColorConfig.fishHUDValueColor, shadows);
                scaledYHolder[0] += lineHeight;

                // Rarities section
                if (config.fishHUDToggles.showRarities) {
                    scaledYHolder[0] += lineHeight;

                    drawHudLine(drawContext, textRenderer, "\uF033", displayRarityCounts.getOrDefault("common", 0), displayFishCaughtCount, config.fishHUDConfig.showRarityPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                    drawHudLine(drawContext, textRenderer, "\uF034", displayRarityCounts.getOrDefault("rare", 0), displayFishCaughtCount, config.fishHUDConfig.showRarityPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                    drawHudLine(drawContext, textRenderer, "\uF035", displayRarityCounts.getOrDefault("epic", 0), displayFishCaughtCount, config.fishHUDConfig.showRarityPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                    drawHudLine(drawContext, textRenderer, "\uF036", displayRarityCounts.getOrDefault("legendary", 0), displayFishCaughtCount, config.fishHUDConfig.showRarityPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                    drawHudLine(drawContext, textRenderer, "\uF037", displayRarityCounts.getOrDefault("mythical", 0), displayFishCaughtCount, config.fishHUDConfig.showRarityPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);

                    scaledYHolder[0] += lineHeight;
                }
                if ((config.fishHUDToggles.showAdult || config.fishHUDToggles.showLarge || config.fishHUDToggles.showGigantic) && !config.fishHUDToggles.showRarities) {
                    scaledYHolder[0] += lineHeight;
                }

                // Adult section
                if (config.fishHUDToggles.showAdult) {
                    drawHudLine(drawContext, textRenderer, "ᴀᴅᴜʟᴛ", displaySizeCounts.getOrDefault("adult", 0), displayFishCaughtCount, config.fishHUDConfig.showSizePercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                    drawContext.drawText(textRenderer, "ᴀᴅᴜʟᴛ ", scaledX, scaledYHolder[0] - lineHeight, 0x4699C9, shadows);
                }
                // Large section
                if (config.fishHUDToggles.showLarge) {
                    drawHudLine(drawContext, textRenderer, "ʟᴀʀɢᴇ", displaySizeCounts.getOrDefault("large", 0), displayFishCaughtCount, config.fishHUDConfig.showSizePercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                    drawContext.drawText(textRenderer, "ʟᴀʀɢᴇ ", scaledX, scaledYHolder[0] - lineHeight, 0xBF8B27, shadows);
                }

                // Gigantics section
                if (config.fishHUDToggles.showGigantic) {
                    drawHudLine(drawContext, textRenderer, "ɢɪɢᴀɴᴛɪᴄ", displaySizeCounts.getOrDefault("gigantic", 0), displayFishCaughtCount, config.fishHUDConfig.showSizePercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                    drawContext.drawText(textRenderer, "ɢɪɢᴀɴᴛɪᴄ ", scaledX, scaledYHolder[0] - lineHeight, 0xFF0000, shadows);
                }


                // Variants section
                if (config.fishHUDToggles.showVariants) {
                    scaledYHolder[0] += lineHeight;
                    if (displayVariantCounts != null) {
                        // Track which variants we've already displayed
                        Set<String> displayedVariants = new HashSet<>(Arrays.asList(
                                "normal","albino", "melanistic", "trophy", "fabled", "zombie"
                        ));

                        if (config.fishHUDToggles.showAlbino) {
                            drawHudLine(drawContext, textRenderer, "\uF041", displayVariantCounts.getOrDefault("albino", 0), displayFishCaughtCount, config.fishHUDConfig.showVariantPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                        }
                        if (config.fishHUDToggles.showMelanistic){
                            drawHudLine(drawContext, textRenderer, "\uF042", displayVariantCounts.getOrDefault("melanistic", 0), displayFishCaughtCount, config.fishHUDConfig.showVariantPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                        }
                        if (config.fishHUDToggles.showTrophy) {
                            drawHudLine(drawContext, textRenderer, "\uF043", displayVariantCounts.getOrDefault("trophy", 0), displayFishCaughtCount, config.fishHUDConfig.showVariantPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                        }
                        if (config.fishHUDToggles.showFabled) {
                            drawHudLine(drawContext, textRenderer, "\uF044", displayVariantCounts.getOrDefault("fabled", 0), displayFishCaughtCount, config.fishHUDConfig.showVariantPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                        }
                        if (config.fishHUDToggles.showZombie) {
                            drawHudLine(drawContext, textRenderer, "\uF089", displayVariantCounts.getOrDefault("zombie", 0), displayFishCaughtCount, config.fishHUDConfig.showVariantPercentages, 0xFFFFFF, scaledX, scaledYHolder, shadows, lineHeight);
                        }
                        // Calculate others
                        if (config.fishHUDToggles.showUnique) {
                            int othersSum = displayVariantCounts.entrySet().stream()
                                    .filter(entry -> !displayedVariants.contains(entry.getKey()))
                                    .mapToInt(Map.Entry::getValue)
                                    .sum();

                            drawContext.drawText(textRenderer, "ᴜɴɪǫᴜᴇ " + othersSum, scaledX, scaledYHolder[0], 0xFFFFFF, shadows);
                            drawContext.drawText(textRenderer, "ᴜɴɪǫᴜᴇ ", scaledX, scaledYHolder[0], config.fishHUDConfig.fishHUDColorConfig.fishHUDUniqueColor, shadows);
                            scaledYHolder[0] += lineHeight;

                        }
                    }
                }

				if (config.fishHUDToggles.showLatestCatch) {
					scaledYHolder[0] += lineHeight;
					drawContext.drawText(textRenderer, "Latest Catch: " + this.newestFish, scaledX, scaledYHolder[0], config.fishHUDConfig.fishHUDColorConfig.fishHUDXPColor, shadows);
					scaledYHolder[0] += lineHeight;
				}
				
                if (config.otherHUDToggles.showItemFrameTooltip) {
                    if(LookTickHandler.instance().targetedItem != null) {
                        drawContext.drawItemTooltip(textRenderer, LookTickHandler.instance().targetedItem, screenWidth / 2, 50);
                    }
                }
            }finally { // Guaranteed to execute even if exceptions occur
                drawContext.getMatrices().pop();
            }
        }
        if(config.petHUD){
            if (config.petWarningHUDConfig.enableWarning && currentPet == null){
                renderNoPetWarning(drawContext); // Add this line
            } else if (currentPet != null) {
                renderCurrentPet(drawContext);
            }
        }
    }
}
