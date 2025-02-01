package top.theillusivec4.curios.mixin.core.accessories;

import io.wispforest.accessories.neoforge.AccessoriesForge;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.common.data.CuriosSlotManager;

import java.util.function.Consumer;

@Mixin(AccessoriesForge.class)
public abstract class AccessoriesForgeMixin {
    @Inject(method = "intermediateRegisterListeners", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 0))
    private void cclayer$injectCuriosSlotLoaderFirst(Consumer<PreparableReloadListener> registrationMethod, CallbackInfo ci) {
        registrationMethod.accept(CuriosSlotManager.SERVER);
    }

    @Inject(method = "intermediateRegisterListeners", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1))
    private void cclayer$injectCuriosEntityLoaderFirst(Consumer<PreparableReloadListener> registrationMethod, CallbackInfo ci) {
        registrationMethod.accept(CuriosEntityManager.SERVER);
    }
}
