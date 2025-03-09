package io.github.markassk.fishonmcextras.common.handler;

import io.github.markassk.fishonmcextras.common.PetStats;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Objects;

public class PetMergeCalculatorHandler {
    private static PetMergeCalculatorHandler INSTANCE = new PetMergeCalculatorHandler();
    public ItemStack[] selectedPets = {null, null};
    public PetStats petOne;
    public PetStats petTwo;
    public PetStats calculatedPet = null;
    public int[] index = {-1, -1};

    public static PetMergeCalculatorHandler instance() {
        if(INSTANCE == null) {
            INSTANCE = new PetMergeCalculatorHandler();
        }
        return INSTANCE;
    }

    public void setPet(ItemStack pet, int list) {
        selectedPets[list] = pet;
        update();
    }

    public void setIndex(int list, int index) {
        this.index[list] = index;
    }

    public void reset() {
        selectedPets = new ItemStack[]{null, null};
        petOne = null;
        petTwo = null;
        this.index = new int[]{-1, -1};
    }

    private void update() {
        if(selectedPets[0] != null) {
            NbtCompound componentOne = getNbt(selectedPets[0]);

            // Pet one data
            assert componentOne != null;
            petOne = new PetStats(
                    capitalize(componentOne.getString("pet")),
                    componentOne.getString("rarity"),
                    (float) componentOne.getList("lbase", NbtElement.COMPOUND_TYPE).getCompound(0).getInt("cur_max"),
                    (float) componentOne.getList("lbase", NbtElement.COMPOUND_TYPE).getCompound(1).getInt("cur_max"),
                    (float) componentOne.getList("cbase", NbtElement.COMPOUND_TYPE).getCompound(0).getInt("cur_max"),
                    (float) componentOne.getList("cbase", NbtElement.COMPOUND_TYPE).getCompound(1).getInt("cur_max")
            );
        }

        if(selectedPets[1] != null) {
            NbtCompound componentTwo = getNbt(selectedPets[1]);

            // Pet two data
            assert componentTwo != null;
            petTwo = new PetStats(
                    capitalize(componentTwo.getString("pet")),
                    componentTwo.getString("rarity"),
                    (float) componentTwo.getList("lbase", NbtElement.COMPOUND_TYPE).getCompound(0).getInt("cur_max"),
                    (float) componentTwo.getList("lbase", NbtElement.COMPOUND_TYPE).getCompound(1).getInt("cur_max"),
                    (float) componentTwo.getList("cbase", NbtElement.COMPOUND_TYPE).getCompound(0).getInt("cur_max"),
                    (float) componentTwo.getList("cbase", NbtElement.COMPOUND_TYPE).getCompound(1).getInt("cur_max")
            );
        }

        if (selectedPets[0] != null && selectedPets[1] != null) {
            this.calculatedPet = calculatePet(petOne, petTwo);
        }
    }

    private PetStats calculatePet(PetStats petOne, PetStats petTwo) {
        if(Objects.equals(petOne.getRarity(), petTwo.getRarity())) {
            String petResultRarity = rarityUpgrade(petOne.getRarity());
            float petMultiplier = rarityMultiplier(petOne.getRarity());
            float petResultMultiplier = rarityMultiplier(rarityUpgrade(petOne.getRarity()));

            float petResultlBaseLuck = (((petOne.getlBaseLuck() / petMultiplier) + (petTwo.getlBaseLuck() / petMultiplier)) / 2) * petResultMultiplier;
            float petResultlBaseScale = (((petOne.getlBaseScale() / petMultiplier) + (petTwo.getlBaseScale() / petMultiplier)) / 2) * petResultMultiplier;
            float petResultcBaseLuck = (((petOne.getcBaseLuck() / petMultiplier) + (petTwo.getcBaseLuck() / petMultiplier)) / 2) * petResultMultiplier;
            float petResultcBaseScale = (((petOne.getcBaseScale() / petMultiplier) + (petTwo.getcBaseScale() / petMultiplier)) / 2) * petResultMultiplier;

            return new PetStats(
                    Objects.equals(petOne.getName(), petTwo.getName()) ? petOne.getName() : petOne.getName() + " + " + petTwo.getName(),
                    petResultRarity,
                    petResultlBaseLuck,
                    petResultlBaseScale,
                    petResultcBaseLuck,
                    petResultcBaseScale
            );

        }
        return null;
    }

    public static String ratingValue(float value) {
        if (value < 10) return "Awful";
        else if (value < 20) return "Bad";
        else if (value < 35) return "Below Average";
        else if (value < 50) return "Average";
        else if (value < 60) return "Good";
        else if (value < 80) return "Great";
        else if (value < 90) return "Excellent";
        else if (value < 100) return "Amazing";
        else if (value < 101) return "Perfect";
        return "Wrong Rarity Selected";
    }

    public static Object[] ratingString(String value) {
        return switch (value) {
            case "Awful" -> new Object[]{"ᴀᴡꜰᴜʟ", 0xFFAA0000};
            case "Bad" -> new Object[]{"ʙᴀᴅ", 0xFFFF5555};
            case "Below Average" -> new Object[]{"ʙᴇʟᴏᴡ ᴀᴠᴇʀᴀɢᴇ", 0xFFFCFC54};
            case "Average" -> new Object[]{"ᴀᴠᴇʀᴀɢᴇ", 0xFFFCA800};
            case "Good" -> new Object[]{"ɢᴏᴏᴅ", 0xFF54FC54};
            case "Great" -> new Object[]{"ɢʀᴇᴀᴛ", 0xFF00A800};
            case "Excellent" -> new Object[]{"ᴇxᴄᴇʟʟᴇɴᴛ", 0xFF54FCFC};
            case "Amazing" -> new Object[]{"ᴀᴍᴀᴢɪɴɢ", 0xFFFC54FC};
            case "Perfect \uD83D\uDCAF" -> new Object[]{"ᴘᴇʀꜰᴇᴄᴛ", 0xFFA800A8};
            default -> new Object[]{"", 0xFFFFFFFF};
        };
    }

    public static float percentageRating(PetStats pet) {
        float total = pet.getcBaseLuck() + pet.getcBaseScale() + pet.getlBaseLuck() + pet.getlBaseScale();
        return total / rarityMultiplier(pet.getRarity());
    }

    public static float percentageStat(float stat, String rarity) {
        float baseStat = stat / rarityMultiplier(rarity);
        return baseStat * 4;
    }

    public static String capitalize(String str) {
        if(str == null || str.length()<=1) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String rarityString(String rarity) {
        return switch (rarity) {
            case "common" -> "\uf033";
            case "rare" -> "\uf034";
            case "epic" -> "\uf035";
            case "legendary" -> "\uf036";
            case "mythical" -> "\uf037";
            default -> "";
        };
    }

    public static String rarityUpgrade(String rarity) {
        return switch (rarity) {
            case "common" -> "rare";
            case "rare" -> "epic";
            case "epic" -> "legendary";
            case "legendary" -> "mythical";
            default -> "";
        };
    }

    public static float rarityMultiplier(String value) {
        return switch (value) {
            case "common" -> 1f;
            case "rare" -> 2f;
            case "epic" -> 3f;
            case "legendary" -> 5f;
            case "mythical" -> 7.5f;
            default -> 1;
        };
    }

    private NbtCompound getNbt(ItemStack itemStack) {
        NbtComponent component = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null ? component.getNbt() : null;
    }
}
