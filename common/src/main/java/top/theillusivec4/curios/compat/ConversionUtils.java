package top.theillusivec4.curios.compat;

import io.wispforest.accessories.Accessories;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.DropRule;
import io.wispforest.accessories.api.data.AccessoriesBaseData;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class ConversionUtils {

    public static SlotContext convertToC(SlotReference slotReference) {
        return new SlotContext(slotReference);
    }

    public static SlotReference convertToA(SlotContext slotContext) {
        var ref = slotContext.slotReference();

        // Either use unpacked reference that was used to create slot context from within the cclayer or
        // make one from info on the context
        if (ref == null) {
            ref = SlotReference.of(slotContext.entity(), convertSlotToA(slotContext.identifier()), slotContext.index());
        }

        return ref;
    }

    public static ICurio.DropRule convertToC(DropRule dropRule) {
        return switch (dropRule) {
            case KEEP -> ICurio.DropRule.ALWAYS_KEEP;
            case DROP -> ICurio.DropRule.ALWAYS_DROP;
            case DESTROY -> ICurio.DropRule.DESTROY;
            case DEFAULT -> ICurio.DropRule.DEFAULT;
        };
    }

    public static DropRule convertToA(ICurio.DropRule dropRule) {
        return switch (dropRule) {
            case ALWAYS_KEEP -> DropRule.KEEP;
            case ALWAYS_DROP -> DropRule.DROP;
            case DESTROY -> DropRule.DESTROY;
            case DEFAULT -> DropRule.DEFAULT;
        };
    }

    public static String convertSlotToA(String curiosType) {
        return switch (curiosType) {
            case "curio" -> "any";
            case "head" -> "hat";
            case "body" -> "cape";
            case "bracelet" -> "wrist";
            case "hands" -> "hand";
            case "feet" -> "shoes"; // Special Case for artifacts
            default -> curiosType;
        };
    }

    public static String convertSlotToC(String accessoriesType) {
        return switch (accessoriesType) {
            case "any" -> "curio";
            case "hat" -> "head";
            case "cape" -> "body";
            case "wrist" -> "bracelet";
            case "hand" -> "hands";
            case "shoes" -> "feet"; // Special Case for artifacts
            default -> accessoriesType;
        };
    }

    public static ResourceLocation convertToC(ResourceLocation accessoryPredicateId) {
        if (accessoryPredicateId.equals(AccessoriesBaseData.ALL_PREDICATE_ID)) {
            return ResourceLocation.parse("curios:all");
        } else if (accessoryPredicateId.equals(AccessoriesBaseData.NONE_PREDICATE_ID)) {
            return ResourceLocation.parse("curios:none");
        } else if (accessoryPredicateId.equals(AccessoriesBaseData.TAG_PREDICATE_ID)) {
            return ResourceLocation.parse("curios:tag");
        }

        return accessoryPredicateId;
    }

    public static ResourceLocation convertToA(ResourceLocation curiosPredicateId) {
        return switch (curiosPredicateId.toString()) {
            case "curios:all" -> AccessoriesBaseData.ALL_PREDICATE_ID;
            case "curios:none" -> AccessoriesBaseData.NONE_PREDICATE_ID;
            case "curios:tag" -> AccessoriesBaseData.TAG_PREDICATE_ID;
            default -> curiosPredicateId;
        };
    }
}
