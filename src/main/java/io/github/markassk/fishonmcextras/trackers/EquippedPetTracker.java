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
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import io.github.markassk.fishonmcextras.FishOnMCExtrasClient;
import io.github.markassk.fishonmcextras.config.FishOnMCExtrasConfig;

public class EquippedPetTracker implements ClientReceiveMessageEvents.Game {
    private static String currentPet = null;
    private static long lastPetChangeTime = 0;
	private static ItemStack petItem = null;
	private static int petSlot = 0;

    private static final Pattern PET_EQUIP_PATTERN =
            Pattern.compile("PETS\\s*[»:]\\s*Equipped your (.+?)\\.?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PET_UNEQUIP_PATTERN =
            Pattern.compile("PETS\\s*[»:]\\s*Pet unequipped!$", Pattern.CASE_INSENSITIVE);
	private static final Pattern PET_LEVELUP_PATTERN =
			Pattern.compile("PETS » Your .+ is now level \\[(.+?)\\]", Pattern.CASE_INSENSITIVE);

    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register(new EquippedPetTracker());
    }
	
	private String capitalizeFirstletter(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
	
	public static void updateXp() {
		try {
			NbtCompound cNbt = MinecraftClient.getInstance().player.getInventory().getStack(petSlot).get(DataComponentTypes.CUSTOM_DATA).getNbt();
			
			HudRenderer.setXpNeed(cNbt.getFloat("xp_need"));
			HudRenderer.setXpCur(cNbt.getFloat("xp_cur"));
		} catch (Exception e) {} 
	}

    @Override
    public void onReceiveGameMessage(Text message, boolean overlay) {
        String rawMessage = message.getString();

        FishOnMCExtrasConfig config = FishOnMCExtrasClient.CONFIG;

        Matcher equipMatcher = PET_EQUIP_PATTERN.matcher(rawMessage);
        Matcher unequipMatcher = PET_UNEQUIP_PATTERN.matcher(rawMessage);
        Matcher levelupMatcher = PET_LEVELUP_PATTERN.matcher(rawMessage);

        String currentLevel = "NaN";
        String currentRarity = "NaN";
		ItemStack petItem = null;
		try {
			petItem = MinecraftClient.getInstance().player.getMainHandStack();
			NbtComponent component = petItem.get(DataComponentTypes.CUSTOM_DATA);
			if (component != null) {
				NbtCompound petNbt = component.getNbt();
				currentLevel = String.valueOf(petNbt.getInt("level"));
				currentRarity = petNbt.getString("rarity");
			}
		} catch (Exception e) {}	// Messages get sent during connection to the server.
									// Not handling the exception would render your client
									// unable to connect to the server.
        
		if (levelupMatcher.find() && config.petActiveHUDConfig.petActiveVerbose) {
			handePetLevelup(levelupMatcher.group(1));
		} else if (equipMatcher.find()) {
			petSlot = MinecraftClient.getInstance().player.getInventory().getSlotWithStack(petItem);
			updateXp();
			if (config.petActiveHUDConfig.petActiveVerbose) {
				handlePetEquip(capitalizeFirstletter(currentRarity) + " " + equipMatcher.group(1) + " [lvl. " + currentLevel + "]");
			} else {
				handlePetEquip(equipMatcher.group(1));
			}
        } else if (unequipMatcher.find()) {
            handlePetUnequip();
        }
    }

    private void handlePetEquip(String petName) {
        // Update to directly use HudRenderer's storage
        HudRenderer.setCurrentPet(petName.trim());
		updateXp();
        lastPetChangeTime = System.currentTimeMillis();
		currentPet = petName;
        FishOnMCExtrasClient.HUD_RENDERER.saveStats(); // Auto-save on change
    }
	
	private void handePetLevelup(String level) {
		String previousPet = getCurrentPet();
		handlePetUnequip();
		
		String newPet = previousPet.split(" \\[lvl\\.")[0] + " [lvl. " + level + "]";
		handlePetEquip(newPet);
	}

    private void handlePetUnequip() {
        HudRenderer.clearCurrentPet();
        lastPetChangeTime = System.currentTimeMillis();
		currentPet = null;
        FishOnMCExtrasClient.HUD_RENDERER.saveStats(); // Auto-save on change
    }

    public static String getCurrentPet() {
        return currentPet;
    }

    public static long getLastPetChangeTime() {
        return lastPetChangeTime;
    }
}
