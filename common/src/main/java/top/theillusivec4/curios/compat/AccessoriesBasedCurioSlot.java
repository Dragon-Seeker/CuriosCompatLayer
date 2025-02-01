package top.theillusivec4.curios.compat;

import io.wispforest.accessories.api.slot.SlotType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.type.ISlotType;
import top.theillusivec4.curios.api.type.capability.ICurio;

public record AccessoriesBasedCurioSlot(SlotType slotType) implements ISlotType {

    @Override public boolean useNativeGui() { return true; }
    @Override public boolean hasCosmetic() { return true; }
    @Override public boolean canToggleRendering() { return true; }

    @Override
    public String getIdentifier() {
        return ConversionUtils.convertSlotToC(this.slotType.name());
    }

    @Override
    public ICurio.DropRule getDropRule() {
        return ConversionUtils.convertToC(this.slotType.dropRule());
    }

    @Override
    public ResourceLocation getIcon() {
        return this.slotType.icon();
    }

    @Override
    public int getOrder() {
        return this.slotType.order();
    }

    @Override
    public int getSize() {
        return this.slotType.amount();
    }

    @Override
    public int compareTo(@NotNull ISlotType otherType) {
        if (this.getOrder() == otherType.getOrder()) {
            return this.getIdentifier().compareTo(otherType.getIdentifier());
        } else if (this.getOrder() > otherType.getOrder()) {
            return 1;
        }

        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        return ISlotType.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return ISlotType.hashCode(this);
    }
}
