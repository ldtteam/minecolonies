package com.minecolonies.api.inventory;

import com.minecolonies.api.inventory.container.*;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModContainers
{
    public static DeferredHolder<MenuType<?>, MenuType<ContainerCraftingFurnace>> craftingFurnace;

    public static DeferredHolder<MenuType<?>, MenuType<ContainerBuildingInventory>> buildingInv;

    public static DeferredHolder<MenuType<?>, MenuType<ContainerCitizenInventory>> citizenInv;

    public static DeferredHolder<MenuType<?>, MenuType<ContainerRack>> rackInv;

    public static DeferredHolder<MenuType<?>, MenuType<ContainerGrave>> graveInv;

    public static DeferredHolder<MenuType<?>, MenuType<ContainerCrafting>> craftingGrid;

    public static DeferredHolder<MenuType<?>, MenuType<ContainerCraftingBrewingstand>> craftingBrewingstand;
}
