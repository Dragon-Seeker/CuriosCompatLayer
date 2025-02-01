/*
 * Copyright (c) 2018-2024 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package top.theillusivec4.curios.api;

import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.api.slot.SlotType;
import io.wispforest.accessories.data.SlotTypeLoader;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.compat.ConversionUtils;

import java.util.Objects;

/**
 * A record representing the accessible slot information related to its context
 */
public final class SlotContext {

    private final SlotReference slotReference;

    private final String identifier;
    private final LivingEntity entity;
    private final int index;
    private final boolean cosmetic;
    private final boolean visible;

    private boolean isClient = false;

    /**
     * @param identifier The identifier of the slot type
     * @param entity     The wearer or intended wearer of the slot type
     * @param index      The index of the slot
     * @param cosmetic   True if the slot is cosmetic, false if the slot is functional
     * @param visible    True if the slot can render its item on the wearer, false if not
     */
    public SlotContext(String identifier, LivingEntity entity, int index, boolean cosmetic, boolean visible) {
        this.identifier = identifier;
        this.entity = entity;
        this.index = index;
        this.cosmetic = cosmetic;
        this.visible = visible;
        this.slotReference = null;
    }

    public SlotContext(SlotReference slotReference) {
        this.identifier = ConversionUtils.convertSlotToC(slotReference.slotName());
        this.entity = slotReference.entity();
        this.index = slotReference.slot();
        this.cosmetic = false;
        this.visible = true;
        this.slotReference = slotReference;

        if (entity != null) {
            this.isClient = entity.level().isClientSide();
        }
    }

    @Nullable
    public SlotReference slotReference() {
        return this.slotReference;
    }

    public SlotContext isClient(boolean value) {
        this.isClient = value;

        return this;
    }

    public SlotType slotType() {
        if(slotReference() != null) return slotReference.type();

        return SlotTypeLoader.INSTANCE.getSlotTypes(this.isClient)
                .get(ConversionUtils.convertSlotToA(this.identifier));
    }

    public String identifier() {
        return identifier;
    }

    public LivingEntity entity() {
        return entity;
    }

    public int index() {
        return index;
    }

    public boolean cosmetic() {
        return cosmetic;
    }

    public boolean visible() {
        return visible;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SlotContext) obj;
        return Objects.equals(this.identifier, that.identifier) &&
                Objects.equals(this.entity, that.entity) &&
                this.index == that.index &&
                this.cosmetic == that.cosmetic &&
                this.visible == that.visible;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, entity, index, cosmetic, visible);
    }

    @Override
    public String toString() {
        return "SlotContext[" +
                "identifier=" + identifier + ", " +
                "entity=" + entity + ", " +
                "index=" + index + ", " +
                "cosmetic=" + cosmetic + ", " +
                "visible=" + visible + ']';
    }
}
