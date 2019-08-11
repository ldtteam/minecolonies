package com.minecolonies.api.inventory;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.minecolonies.api.inventory.container.*;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class ModContainers
{
    @ObjectHolder("crafting_furnace")
    public static ContainerType<ContainerCraftingFurnace> craftingFurnace;

    @ObjectHolder("bulding_inv")
    public static ContainerType<ContainerBuildingInventory> buildingInv;

    @ObjectHolder("citizen_inv")
    public static ContainerType<ContainerCitizenInventory> citizenInv;

    @ObjectHolder("rack_inv")
    public static ContainerType<ContainerRack> rackInv;

    @ObjectHolder("crafting_building")
    public static ContainerType<ContainerCrafting> craftingGrid;

    @ObjectHolder("field")
    public static ContainerType<ContainerField> field;
}
