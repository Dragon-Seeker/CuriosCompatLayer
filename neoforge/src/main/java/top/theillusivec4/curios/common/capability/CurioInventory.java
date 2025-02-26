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

package top.theillusivec4.curios.common.capability;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.mojang.logging.LogUtils;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.data.SlotTypeLoader;
import io.wispforest.accessories.impl.AccessoriesCapabilityImpl;
import io.wispforest.accessories.impl.AccessoriesContainerImpl;
import io.wispforest.accessories.impl.AccessoriesHolderImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.compat.AccessoriesBasedStackHandler;
import top.theillusivec4.curios.compat.ConversionUtils;

public class CurioInventory implements INBTSerializable<CompoundTag> {
  private static final Logger LOGGER = LogUtils.getLogger();

  @Nullable
  private AccessoriesHolderImpl holder = null;

  final Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
  ICuriosItemHandler curiosItemHandler = null;
  NonNullList<ItemStack> invalidStacks = NonNullList.create();
  Set<ICurioStacksHandler> updates = new HashSet<>();
  CompoundTag deserialized = new CompoundTag();
  boolean markDeserialized = false;

  public List<ItemStack> invalidStacks() {
    return this.holder != null ? this.holder.invalidStacks : List.of();
  }

  public void init(final ICuriosItemHandler curiosItemHandler) {
    if (!(curiosItemHandler instanceof CurioInventoryCapability basedItemHandler)) {
      LOGGER.error("Unable to init a given CurioInventory due to given ICuriosItemHandler not being instance of CurioInventoryCapability!");

      return;
    }

    init(basedItemHandler.capability);
  }

  public void init(AccessoriesCapabilityImpl capability) {
    this.holder = ((AccessoriesHolderImpl) capability.getHolder());

    if (!this.markDeserialized) {
      // TODO: MAY BE A ISSUES ENCOUNTERED WITHIN THE PAST DUE TO ACCESSORIES BEING INIT FIRST LEADING TO DISAPPEARANCE OF STUFF
      //capability.reset(false);
    } else {
      this.markDeserialized = false;

      if (this.deserialized.getBoolean("AccessoriesEncoded") || this.deserialized.isEmpty()) return;

      readData(capability.entity(), capability, this.deserialized.getList("Curios", Tag.TAG_COMPOUND));

      this.deserialized = new CompoundTag();
    }
  }

  public static void readData(LivingEntity livingEntity, AccessoriesCapability capability, ListTag data) {
    for (int i = 0; i < data.size(); i++) {
      var tag = data.getCompound(i);
      var curiosId = tag.getString("Identifier");

      var slotType = SlotTypeLoader.getSlotType(livingEntity.level(), ConversionUtils.convertSlotToA(curiosId));

      var container = (slotType != null) ? capability.getContainer(slotType) : null;

      ((AccessoriesHolderImpl) capability.getHolder()).invalidStacks
              .addAll(deserializeNBT_StackHandler(livingEntity, container, tag.getCompound("StacksHandler")));
    }

    var invalidStacks = ((AccessoriesHolderImpl) capability.getHolder()).invalidStacks;

    for (var entryRef : capability.getAllEquipped()) {
      var reference = entryRef.reference();
      var slotType = reference.type();

      if (AccessoriesAPI.getPredicateResults(slotType.validators(), reference.entity().level(), livingEntity, slotType, 0, entryRef.stack())) continue;

      invalidStacks.add(entryRef.stack().copy());

      entryRef.reference().setStack(ItemStack.EMPTY);
    }
  }

  private static List<ItemStack> deserializeNBT_StackHandler(LivingEntity livingEntity, @Nullable AccessoriesContainer container, CompoundTag nbt){
    var dropped = new ArrayList<ItemStack>();

    if (nbt.contains("Stacks")) {
      dropped.addAll(deserializeNBT_Stacks(livingEntity, container, AccessoriesContainer::getAccessories, nbt.getCompound("Stacks")));
    }

    if (nbt.contains("Cosmetics")) {
      dropped.addAll(deserializeNBT_Stacks(livingEntity, container, AccessoriesContainer::getCosmeticAccessories, nbt.getCompound("Cosmetics")));
    }

    return dropped;
  }

  private static List<ItemStack> deserializeNBT_Stacks(LivingEntity livingEntity, @Nullable AccessoriesContainer container, Function<AccessoriesContainer, Container> containerFunc, CompoundTag nbt){
    var list = nbt.getList("Items", Tag.TAG_COMPOUND)
            .stream()
            .map(tagEntry -> ItemStack.parseOptional(livingEntity.registryAccess(), (tagEntry instanceof CompoundTag compoundTag) ? compoundTag : new CompoundTag()))
            .toList();

    var dropped = new ArrayList<ItemStack>();

    if(container != null) {
      var accessories = containerFunc.apply(container);

      for (var stack : list) {
        boolean consumedStack = false;

        for (int i = 0; i < accessories.getContainerSize() && !consumedStack; i++) {
          var currentStack = accessories.getItem(i);

          if (!currentStack.isEmpty()) continue;

          accessories.setItem(i, stack.copy());

          consumedStack = true;
        }

        if (!consumedStack) dropped.add(stack.copy());
      }
    } else {
      dropped.addAll(list);
    }

    return dropped;
  }

  /*
  public void init(final ICuriosItemHandler curiosItemHandler) {
    this.curiosItemHandler = curiosItemHandler;
    this.curios.clear();
    LivingEntity livingEntity = curiosItemHandler.getWearer();

    if (!this.markDeserialized) {
      SortedSet<ISlotType> sorted =
          new TreeSet<>(CuriosApi.getEntitySlots(livingEntity).values());

      for (ISlotType slotType : sorted) {
        this.curios.put(slotType.getIdentifier(),
            new CurioStacksHandler(curiosItemHandler, slotType.getIdentifier(), slotType.getSize(),
                slotType.useNativeGui(), slotType.hasCosmetic(), slotType.canToggleRendering(),
                slotType.getDropRule()));
      }
    } else {
      this.markDeserialized = false;

      ListTag tagList = this.deserialized.getList("Curios", Tag.TAG_COMPOUND);
      Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
      SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = new TreeMap<>();
      SortedSet<ISlotType> sorted =
          new TreeSet<>(CuriosApi.getEntitySlots(livingEntity).values());

      for (ISlotType slotType : sorted) {
        sortedCurios.put(slotType,
            new CurioStacksHandler(curiosItemHandler, slotType.getIdentifier(),
                slotType.getSize(), slotType.useNativeGui(), slotType.hasCosmetic(),
                slotType.canToggleRendering(), slotType.getDropRule()));
      }

      for (int i = 0; i < tagList.size(); i++) {
        CompoundTag tag = tagList.getCompound(i);
        String identifier = tag.getString("Identifier");
        CurioStacksHandler prevStacksHandler =
            new CurioStacksHandler(curiosItemHandler, identifier);
        prevStacksHandler.deserializeNBT(tag.getCompound("StacksHandler"));

        Optional<ISlotType> optionalType =
            Optional.ofNullable(CuriosApi.getEntitySlots(livingEntity).get(identifier));
        optionalType.ifPresent(slotType -> {
          CurioStacksHandler newStacksHandler =
              new CurioStacksHandler(curiosItemHandler, slotType.getIdentifier(),
                  slotType.getSize(), slotType.useNativeGui(), slotType.hasCosmetic(),
                  slotType.canToggleRendering(), slotType.getDropRule());
          newStacksHandler.copyModifiers(prevStacksHandler);
          int index = 0;

          while (index < newStacksHandler.getSlots() && index < prevStacksHandler
              .getSlots()) {
            ItemStack prevStack = prevStacksHandler.getStacks().getStackInSlot(index);

            if (!prevStack.isEmpty()) {

              if (newStacksHandler.getStacks().isItemValid(index, prevStack)) {
                newStacksHandler.getStacks().setStackInSlot(index, prevStack);
              } else {
                this.curiosItemHandler.loseInvalidStack(prevStack);
              }
            }
            ItemStack prevCosmetic = prevStacksHandler.getCosmeticStacks().getStackInSlot(index);

            if (!prevCosmetic.isEmpty()) {

              if (newStacksHandler.getStacks().isItemValid(index, prevCosmetic)) {
                newStacksHandler.getCosmeticStacks().setStackInSlot(index,
                    prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
              } else {
                this.curiosItemHandler.loseInvalidStack(prevCosmetic);
              }
            }
            index++;
          }

          while (index < prevStacksHandler.getSlots()) {
            this.curiosItemHandler.loseInvalidStack(
                prevStacksHandler.getStacks().getStackInSlot(index));
            this.curiosItemHandler.loseInvalidStack(
                prevStacksHandler.getCosmeticStacks().getStackInSlot(index));
            index++;
          }
          sortedCurios.put(slotType, newStacksHandler);

          for (int j = 0;
               j < newStacksHandler.getRenders().size() &&
                   j < prevStacksHandler.getRenders()
                       .size(); j++) {
            newStacksHandler.getRenders().set(j, prevStacksHandler.getRenders().get(j));
          }
        });

        if (optionalType.isEmpty()) {
          IDynamicStackHandler stackHandler = prevStacksHandler.getStacks();
          IDynamicStackHandler cosmeticStackHandler = prevStacksHandler.getCosmeticStacks();

          for (int j = 0; j < stackHandler.getSlots(); j++) {
            ItemStack stack = stackHandler.getStackInSlot(j);

            if (!stack.isEmpty()) {
              this.curiosItemHandler.loseInvalidStack(stack);
            }

            ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(j);

            if (!cosmeticStack.isEmpty()) {
              this.curiosItemHandler.loseInvalidStack(cosmeticStack);
            }
          }
        }
      }
      sortedCurios.forEach(
          (slotType, stacksHandler) -> curios.put(slotType.getIdentifier(), stacksHandler));
      this.curios.putAll(curios);
      this.deserialized = new CompoundTag();
    }
  }
  */

  public Map<String, ICurioStacksHandler> asMap() {
    if (holder == null) return Map.of();

    return holder.getSlotContainers().entrySet()
            .stream()
            .collect(Collectors.toMap(entry -> ConversionUtils.convertSlotToC(entry.getKey()), entry -> new AccessoriesBasedStackHandler(entry.getValue())));
  }

  public void replace(Map<String, ICurioStacksHandler> curios) {
//    this.curios.clear();
//    this.curios.putAll(curios);
  }

  @Override
  public CompoundTag serializeNBT(@Nonnull HolderLookup.Provider provider) {
    CompoundTag compound = new CompoundTag();

    ListTag taglist = new ListTag();
    this.asMap().forEach((key, stacksHandler) -> {
      CompoundTag tag = new CompoundTag();
      tag.put("StacksHandler", stacksHandler.serializeNBT());
      tag.putString("Identifier", key);
      taglist.add(tag);
    });
    compound.put("Curios", taglist);
    compound.putBoolean("AccessoriesEncoded", true);
    return compound;
  }

  @Override
  public void deserializeNBT(@Nonnull HolderLookup.Provider provider, @Nonnull CompoundTag nbt) {
    this.deserialized = nbt;
    this.markDeserialized = true;
  }
}
