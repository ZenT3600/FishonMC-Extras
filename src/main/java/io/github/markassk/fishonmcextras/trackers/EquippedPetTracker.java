package io.github.markassk.fishonmcextras.trackers;

import io.github.markassk.fishonmcextras.FishOnMCExtrasClient;
import io.github.markassk.fishonmcextras.hud.HudRenderer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EquippedPetTracker implements ClientReceiveMessageEvents.Game {
    private static String currentPet = null;
    private static long lastPetChangeTime = 0;
    private UUID playerUUID;
    private String currentWorldId = "";

    private static final Pattern PET_EQUIP_PATTERN =
            Pattern.compile("PETS\\s*[»:]\\s*Equipped your (.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PET_UNEQUIP_PATTERN =
            Pattern.compile("PETS\\s*[»:]\\s*Pet unequipped!$", Pattern.CASE_INSENSITIVE);

    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register(new EquippedPetTracker());
    }

    @Override
    public void onReceiveGameMessage(Text message, boolean overlay) {
        String rawMessage = message.getString();

        Matcher equipMatcher = PET_EQUIP_PATTERN.matcher(rawMessage);
        Matcher unequipMatcher = PET_UNEQUIP_PATTERN.matcher(rawMessage);

        NbtComponent component = MinecraftClient.getInstance().player.getMainHandStack().getItem().get(DataComponentTypes.CUSTOM_DATA);
        int level = -1;
        if (component != null) {
            level = component.getNbt().getInt("level");
        }
        
        if (equipMatcher.find()) {
            handlePetEquip(equipMatcher.group(1) + " [" + level + "]");
        } else if (unequipMatcher.find()) {
            handlePetUnequip();
        }
    }

    private void handlePetEquip(String petName) {
        // Update to directly use HudRenderer's storage
        HudRenderer.setCurrentPet(petName.trim());
        lastPetChangeTime = System.currentTimeMillis();
        FishOnMCExtrasClient.HUD_RENDERER.saveStats(); // Auto-save on change
    }

    private void handlePetUnequip() {
        HudRenderer.clearCurrentPet();
        lastPetChangeTime = System.currentTimeMillis();
        FishOnMCExtrasClient.HUD_RENDERER.saveStats(); // Auto-save on change
    }

    public static String getCurrentPet() {
        return currentPet;
    }

    public static long getLastPetChangeTime() {
        return lastPetChangeTime;
    }
}
