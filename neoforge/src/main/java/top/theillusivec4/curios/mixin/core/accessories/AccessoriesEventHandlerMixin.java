package top.theillusivec4.curios.mixin.core.accessories;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.impl.AccessoriesEventHandler;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.theillusivec4.curios.compat.AccessoryFromCurio;
import top.theillusivec4.curios.compat.NeoConversionUtils;

@Mixin(AccessoriesEventHandler.class)
public abstract class AccessoriesEventHandlerMixin {
    @WrapOperation(
            method = "handleInvalidStacks(Lnet/minecraft/world/Container;Lio/wispforest/accessories/api/slot/SlotReference;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(value = "INVOKE", target = "Lio/wispforest/accessories/api/AccessoriesAPI;canInsertIntoSlot(Lnet/minecraft/world/item/ItemStack;Lio/wispforest/accessories/api/slot/SlotReference;)Z"), remap = false)
    private static boolean cclayer$adjustCheckBehavior(ItemStack stack, SlotReference reference, Operation<Boolean> operation){
        var accessory = AccessoriesAPI.getAccessory(stack);

        // This exists due to how curios only checks a given curio equability on first equip only and not on revalidating
        if (!(accessory instanceof AccessoryFromCurio)) return operation.call(stack, reference);

        var slotType = reference.type();

        return AccessoriesAPI.getPredicateResults(slotType.validators(), reference.entity().level(), reference.entity(), slotType, 0, stack);
    }

    @WrapOperation(method = {"attemptEquipFromUse", "attemptEquipOnEntity"}, at = @At(value = "INVOKE", target = "Lio/wispforest/accessories/api/Accessory;canEquipFromUse(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean cclayer$preventEquippingForCuriosItems(Accessory instance, ItemStack stack, Operation<Boolean> original) {
        if (AccessoriesAPI.isDefaultAccessory(instance) && stack.is(NeoConversionUtils.ALL_CURIOS_ITEMS)) {
            return false;
        }

        return original.call(instance, stack);
    }
}
