package com.minecolonies.api.inventory;

import com.minecolonies.api.inventory.container.*;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.ObjectHolder;

public class ModContainers
{
    public static ContainerType<ContainerCraftingFurnace> craftingFurnace;

    public static ContainerType<ContainerBuildingInventory> buildingInv;

    public static ContainerType<ContainerCitizenInventory> citizenInv;

    public static ContainerType<ContainerRack> rackInv;

    public static ContainerType<ContainerGrave> graveInv;

    public static ContainerType<ContainerCrafting> craftingGrid;

    public static ContainerType<ContainerField> field;

    public static ContainerType<ContainerCraftingBrewingstand> craftingBrewingstand;
}
