package top.theillusivec4.curios.mixin.core.accessories;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.accessories.data.SlotTypeLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.CuriosConstants;
import top.theillusivec4.curios.common.data.CuriosSlotManager;
import top.theillusivec4.curios.compat.ConversionUtils;

import java.util.HashMap;
import java.util.Map;

@Mixin(SlotTypeLoader.class)
public abstract class SlotTypeLoaderMixin {

    @Unique
    private final ResourceLocation EMPTY_TEXTURE = ResourceLocation.fromNamespaceAndPath(CuriosConstants.MOD_ID, "slot/empty_curio_slot");

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Ljava/util/HashMap;<init>()V", shift = At.Shift.AFTER, ordinal = 2), remap = false)
    private void injectCuriosSpecificSlots(
            Map<ResourceLocation, JsonObject> data,
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfo ci,
            @Local(name = "builders") HashMap<String, SlotTypeLoader.SlotBuilder> builders){

        CuriosSlotManager.SERVER.getCuriosBuilders().forEach((curiosId, curiosBuilder) -> {
            var accessoriesId = ConversionUtils.convertSlotToA(curiosId);

            SlotTypeLoader.SlotBuilder builder;
            Integer slotsCurrentSize = null;

            if (builders.containsKey(accessoriesId)) {
                builder = builders.get(accessoriesId);

                slotsCurrentSize = builder.baseAmount;
            } else {
                builder = builders.computeIfAbsent(accessoriesId, SlotTypeLoader.SlotBuilder::new);

                var icon = curiosBuilder.icon;

                if (icon != null && !icon.equals(EMPTY_TEXTURE)) builder.icon(icon);

                if (curiosBuilder.order != null) builder.order(curiosBuilder.order);

                if (curiosBuilder.dropRule != null) builder.dropRule(ConversionUtils.convertToA(curiosBuilder.dropRule));

                builder.alternativeTranslation("curios.identifier." + curiosId);
            }

            if (curiosBuilder.size != null && slotsCurrentSize != null && curiosBuilder.size > slotsCurrentSize) {
                builder.amount(curiosBuilder.size);
            }

            if (curiosBuilder.sizeMod != 0) {
                builder.addAmount(curiosBuilder.sizeMod);
            }

            if(curiosBuilder.validators != null) {
                curiosBuilder.validators.forEach(validatorPredicate -> builder.validator(ConversionUtils.convertToA(validatorPredicate)));
            }
        });
    }

}
