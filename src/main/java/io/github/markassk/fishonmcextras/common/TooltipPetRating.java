package io.github.markassk.fishonmcextras.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.List;
import java.util.stream.Stream;

public class TooltipPetRating {
    private static final Gson gson = new Gson();

    private static float getRarityMultiplier(String rarity) {
        String lastChar = "\\u" + Integer.toHexString(rarity.charAt(rarity.length() - 1) | 0x10000).substring(1);
        return switch (lastChar) {
            case "\\uf033" -> 1f; // Common
            case "\\uf034" -> 2f; // Rare
            case "\\uf035" -> 3f; // Epic
            case "\\uf036" -> 5f; // Legendary
            case "\\uf037" -> 7.5f; // Mythical
            default -> 1;
        };
    }

    private static float clampRarity(float rating, String json) {
        if (json.contains("ᴀᴡꜰᴜʟ")) return Math.clamp(rating, 0, 9);
        else if (json.contains("ʙᴀᴅ")) return Math.clamp(rating, 10, 19);
        else if (json.contains("ʙᴇʟᴏᴡ ᴀᴠᴇʀᴀɢᴇ")) return Math.clamp(rating, 20, 34);
        else if (json.contains("ᴀᴠᴇʀᴀɢᴇ")) return Math.clamp(rating, 35, 49);
        else if (json.contains("ɢᴏᴏᴅ")) return Math.clamp(rating, 50, 59);
        else if (json.contains("ɢʀᴇᴀᴛ")) return Math.clamp(rating, 60, 79);
        else if (json.contains("ᴇxᴄᴇʟʟᴇɴᴛ")) return Math.clamp(rating, 80, 89);
        else if (json.contains("ᴀᴍᴀᴢɪɴɢ")) return Math.clamp(rating, 90, 99);
        return rating;
    }

    public static List<Text> appendTooltipRating(List<Text> textList) {
        if(textList.size() >= 3 && textList.get(1).getString().contains(" Pet") && textList.get(3).getString().contains(" ᴘᴇᴛ")) {
            String petClimateLuck = getMaxFromString(textList.get(9).copy().getString());
            String petClimateScale = getMaxFromString(textList.get(10).copy().getString());
            String petLocationLuck = getMaxFromString(textList.get(13).copy().getString());
            String petLocationScale = getMaxFromString(textList.get(14).copy().getString());

            float multiplier = getRarityMultiplier(textList.get(2).getString());
            float total = Stream.of(petClimateLuck, petClimateScale, petLocationLuck, petLocationScale).mapToInt(Integer::parseInt).sum();

            String json = textToJson(textList.get(16));
            StringBuilder builder = new StringBuilder(json);
            float rating = clampRarity(total / multiplier, json);
            String newJson = builder.insert(ordinalIndexOf(json, "\",\"italic\"", 2)," (" + String.format("%.0f", rating) + "%)").toString();
            Text petRatingLine = TextCodecs.CODEC
                    .decode(JsonOps.INSTANCE, gson.fromJson(newJson, JsonElement.class))
                    .getOrThrow()
                    .getFirst();

            textList.set(16, petRatingLine);
        }
        return textList;
    }

    public static Text appendTooltipRating(Text textLine) {
        String[] lines = textLine.getString().split("\\r?\\n");
        if(lines.length >= 3 && lines[0].contains(" Pet") && lines[2].contains(" ᴘᴇᴛ")) {
            String petClimateLuck = getMaxFromString(lines[8]);
            String petClimateScale = getMaxFromString(lines[9]);
            String petLocationLuck = getMaxFromString(lines[12]);
            String petLocationScale = getMaxFromString(lines[13]);

            float multiplier = getRarityMultiplier(lines[1]);
            float total = Stream.of(petClimateLuck, petClimateScale, petLocationLuck, petLocationScale).mapToInt(Integer::parseInt).sum();

            String json = textToJson(textLine);
            StringBuilder builder = new StringBuilder(json);
            float rating = clampRarity(total / multiplier, json);
            String newJson = builder.insert(ordinalIndexOf(json, "\\n", 16), " (" + String.format("%.0f", rating) + "%)").toString();
            return TextCodecs.CODEC
                    .decode(JsonOps.INSTANCE, gson.fromJson(newJson, JsonElement.class))
                    .getOrThrow()
                    .getFirst();
        }
        return textLine;
    }

    private static int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(substr, pos + 1);
        return pos;
    }

    private static String getMaxFromString(String str) {
        return str.substring(str.indexOf("/") + 1, str.indexOf(")"));
    }

    private static String textToJson(Text text) {
        return gson.toJson(TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow());
    }
}
