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

            Text petClimateLuckLine = appendRating(textList.get(9), Float.parseFloat(petClimateLuck), multiplier, 4, "\",\"italic\"", 3, textToJson(textList.get(16)), false);
            Text petClimateScaleLine = appendRating(textList.get(10), Float.parseFloat(petClimateScale), multiplier, 4, "\",\"italic\"", 3, textToJson(textList.get(16)), false);
            Text petLocationLuckLine = appendRating(textList.get(13), Float.parseFloat(petLocationLuck), multiplier, 4, "\",\"italic\"", 3, textToJson(textList.get(16)), false);
            Text petLocationScaleLine = appendRating(textList.get(14), Float.parseFloat(petLocationScale), multiplier, 4, "\",\"italic\"", 3, textToJson(textList.get(16)), false);
            Text petRatingLine = appendRating(textList.get(16), total, multiplier, 1, "\",\"italic\"", 2, textToJson(textList.get(16)), true);


            textList.set(9, petClimateLuckLine);
            textList.set(10, petClimateScaleLine);
            textList.set(13, petLocationLuckLine);
            textList.set(14, petLocationScaleLine);
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

            Text petRatingLine = textLine.copy();

            petRatingLine = appendRating(petRatingLine, Float.parseFloat(petClimateLuck), multiplier, 4, "\\n", 9, textToJson(textLine), false);
            petRatingLine = appendRating(petRatingLine, Float.parseFloat(petClimateScale), multiplier, 4, "\\n", 10, textToJson(textLine), false);
            petRatingLine = appendRating(petRatingLine, Float.parseFloat(petLocationLuck), multiplier, 4, "\\n", 13, textToJson(textLine), false);
            petRatingLine = appendRating(petRatingLine, Float.parseFloat(petLocationScale), multiplier, 4, "\\n", 14, textToJson(textLine), false);
            petRatingLine = appendRating(petRatingLine, total, multiplier, 1, "\\n", 16, textToJson(textLine), true);

            return petRatingLine;
        }
        return textLine;
    }

    private static Text appendRating(Text line, float rating, float rarityMultiplier, float extraMultiplier, String substr, int occurrence, String rarity, boolean clamp) {
        String json = textToJson(line);
        StringBuilder builder = new StringBuilder(json);
        float ratingPercentage = clamp ? clampRarity(rating * extraMultiplier / rarityMultiplier, rarity) : rating * extraMultiplier / rarityMultiplier;
        String newJson = builder.insert(ordinalIndexOf(json, substr, occurrence), " (" + String.format("%.0f", ratingPercentage) + "%)").toString();

        return TextCodecs.CODEC
                .decode(JsonOps.INSTANCE, gson.fromJson(newJson, JsonElement.class))
                .getOrThrow()
                .getFirst();
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
