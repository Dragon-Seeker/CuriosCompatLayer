package top.theillusivec4.curios.mixin.core.accessories.client;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.theillusivec4.curios.compat.lang.TranslationInjectionEvent;

import java.util.Map;

@Mixin(ClientLanguage.class)
public abstract class ClientLanguageMixin {
    @WrapOperation(method = "loadFrom", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;", ordinal = 0))
    private static ImmutableMap<String, String> dynamicLangStuff(Map<String, String> kvMap, Operation<ImmutableMap<String, String>> original) {
        TranslationInjectionEvent.AFTER_LANGUAGE_LOAD.invoker()
                .generateLanguageTranslations(new TranslationInjectionEvent.TranslationMapHelper(kvMap));

        return original.call(kvMap);
    }
}
