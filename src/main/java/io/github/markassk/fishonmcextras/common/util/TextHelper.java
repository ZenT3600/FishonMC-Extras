package io.github.markassk.fishonmcextras.common.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextHelper {
    public static Text concat(Text... texts) {
        MutableText text = Text.empty();
        for (Text t : texts) {
            text.append(t);
        }
        return text;
    }

    public static String fmt(float d)
    {
        return String.format("%.0f", d);
    }
}
