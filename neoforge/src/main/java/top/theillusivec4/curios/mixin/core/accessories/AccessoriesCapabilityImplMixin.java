package top.theillusivec4.curios.mixin.core.accessories;

import io.wispforest.accessories.impl.AccessoriesCapabilityImpl;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.common.capability.CurioInventoryCapability;
import top.theillusivec4.curios.common.capability.CurioItemHandler;

@Mixin(AccessoriesCapabilityImpl.class)
public abstract class AccessoriesCapabilityImplMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void attemptCurioConversion(LivingEntity entity, CallbackInfo ci) {
        CurioInventoryCapability.attemptConversion((AccessoriesCapabilityImpl) (Object) this);
    }
}
