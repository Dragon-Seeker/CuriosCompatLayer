package top.theillusivec4.curios.mixin.core.accessories.client;

import com.google.common.collect.BiMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.capability.ItemizedCurioCapability;
import top.theillusivec4.curios.compat.AccessoryFromCurio;

@Mixin(value = AccessoriesRendererRegistry.class, remap = false)
public abstract class AccessoriesRendererRegistryMixin {
    @WrapOperation(method = "getRenderer(Lnet/minecraft/world/item/Item;)Lio/wispforest/accessories/api/client/AccessoryRenderer;", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/BiMap;containsKey(Ljava/lang/Object;)Z"), remap = false)
    private static boolean alterDefaultBehavior(BiMap map, Object object, Operation<Boolean> operation, @Local(argsOnly = true) Item item) {
        var stack = item.getDefaultInstance();

        var iCurioItem = CuriosApi.getCurio(stack).orElse(null);

        if(!(iCurioItem instanceof ItemizedCurioCapability capability && capability.curioItem instanceof AccessoryFromCurio)) {
            return true;
        }

        return operation.call(map, object);
    }
}