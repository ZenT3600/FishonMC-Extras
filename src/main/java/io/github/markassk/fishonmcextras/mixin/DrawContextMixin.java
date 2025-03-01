package io.github.markassk.fishonmcextras.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.markassk.fishonmcextras.common.TooltipPetRating;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    @ModifyVariable(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private List<Text> drawTooltipItem(List<Text> value) {
        return TooltipPetRating.appendTooltipRating(value);
    }

    @ModifyExpressionValue(method = "drawHoverEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/HoverEvent;getValue(Lnet/minecraft/text/HoverEvent$Action;)Ljava/lang/Object;", ordinal = 2))
    private Object drawChatTooltip(Object original) {
        return TooltipPetRating.appendTooltipRating(((Text) original).copy());
    }
}
