package top.theillusivec4.curios.mixin.core.accessories;

import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.impl.AccessoriesContainerImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(value = AccessoriesContainerImpl.class, remap = false)
public interface AccessoriesContainerImplAccessor {

    @Accessor(value = "modifiers", remap = false)
    Map<ResourceLocation, AttributeModifier> modifiers();

    @Accessor(value = "persistentModifiers", remap = false)
    Set<AttributeModifier> persistentModifiers();

    @Accessor(value = "cachedModifiers", remap = false)
    Set<AttributeModifier> cachedModifiers();
}
