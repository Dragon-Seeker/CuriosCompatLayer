package top.theillusivec4.curios.compat;

import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.slot.SlotType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.common.capability.ItemizedCurioCapability;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NeoConversionUtils {
    public static Accessory convertToA(ICurioItem curioItem) {
        return new AccessoryFromCurio(stack -> new ItemizedCurioCapability(curioItem, stack));
    }

    public static Accessory convertToA(ICapabilityProvider<ItemStack, Void, ICurio> icurioProvider) {
        return new AccessoryFromCurio(stack -> icurioProvider.getCapability(stack, null));
    }

    @Nullable
    public static ICurio convertToC(Accessory accessory, ItemStack stack) {
        if (accessory instanceof AccessoryFromCurio accessoryFromCurio) {
            return accessoryFromCurio.iCurio(stack).orElse(null);
        }

        return new ItemizedCurioCapability(new CurioFromAccessory(accessory), stack);
    }

    // TODO: GET MORE PERFORMANCE BY WRAPPING MAP WHEN ENTRIES ARE ITERATED!
    //--
    public static Map<String, ISlotType> convertToC(@Nullable Collection<SlotType> slots) {
        if (slots == null) return Map.of();

        return slots
                .stream()
                .collect(Collectors.toMap(slotType -> ConversionUtils.convertSlotToC(slotType.name()), AccessoriesBasedCurioSlot::new));
    }

    public static Map<String, ISlotType> convertToC(@Nullable Map<String, SlotType> slots) {
        if (slots == null) return Map.of();

        var map = slots.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> ConversionUtils.convertSlotToC(entry.getKey()), entry -> new AccessoriesBasedCurioSlot(entry.getValue())));

        return Collections.unmodifiableMap(map);
    }

    public static <T> Map<String, T> convertToC(@Nullable Map<String, SlotType> slots, Function<SlotType, T> conversionFunc) {
        if (slots == null) return Map.of();

        var map = slots.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> ConversionUtils.convertSlotToC(entry.getKey()), entry -> conversionFunc.apply(entry.getValue())));

        return Collections.unmodifiableMap(map);
    }

    //--

    public static net.fabricmc.fabric.api.util.TriState convertToFabric(@NotNull TriState triState) {
        return switch (triState) {
            case FALSE -> net.fabricmc.fabric.api.util.TriState.FALSE;
            case TRUE -> net.fabricmc.fabric.api.util.TriState.TRUE;
            case DEFAULT -> net.fabricmc.fabric.api.util.TriState.DEFAULT;
        };
    }

    public static TriState convertToNeo(@NotNull net.fabricmc.fabric.api.util.TriState triState) {
        return switch (triState) {
            case FALSE -> TriState.FALSE;
            case TRUE -> TriState.TRUE;
            case DEFAULT -> TriState.DEFAULT;
        };
    }
}
