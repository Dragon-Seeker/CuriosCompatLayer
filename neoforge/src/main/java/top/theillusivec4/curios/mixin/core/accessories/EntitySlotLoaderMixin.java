package top.theillusivec4.curios.mixin.core.accessories;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import io.wispforest.accessories.api.slot.SlotType;
import io.wispforest.accessories.data.EntitySlotLoader;
import io.wispforest.accessories.data.SlotTypeLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.compat.ConversionUtils;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntitySlotLoader.class)
public abstract class EntitySlotLoaderMixin {

    @Unique
    private static final Logger CURIOS_LOGGER = LogUtils.getLogger();

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;"), remap = false)
    private void injectCuriosSpecificSlots(Map<ResourceLocation, JsonObject> data,
                                           ResourceManager resourceManager,
                                           ProfilerFiller profiler,
                                           CallbackInfo ci,
                                           @Local(name = "tempMap") HashMap<EntityType<?>, Map<String, SlotType>> tempMap){
        for (var entry : CuriosEntityManager.SERVER.entityToSlotId().entrySet()) {
            var slotTypes = tempMap.computeIfAbsent(entry.getKey(), entityType -> new HashMap<>());

            for (String curiosId : entry.getValue().build()) {
                var accessoriesId = ConversionUtils.convertSlotToA(curiosId);

                if (slotTypes.containsKey(accessoriesId)) continue;

                var type = SlotTypeLoader.INSTANCE.getSlotTypes(false).get(accessoriesId);

                if (type == null)  {
                    CURIOS_LOGGER.warn("Unable to locate the given slot for a given entity binding, it will be skipped: [Name: {}]", accessoriesId);

                    continue;
                }

                slotTypes.put(accessoriesId, type);
            }
        }
    }
}
