package com.schematica.world.schematic;

import com.schematica.reference.Names;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

final class SchematicUtil
{
    private static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.grass);

    @NotNull
    public static ItemStack getIconFromNBT(final NBTTagCompound tagCompound)
    {
        ItemStack icon = DEFAULT_ICON.copy();

        if (tagCompound != null && tagCompound.hasKey(Names.NBT.ICON))
        {
            icon.readFromNBT(tagCompound.getCompoundTag(Names.NBT.ICON));

            if (icon.getItem() == null)
            {
                icon = DEFAULT_ICON.copy();
            }
        }

        return icon;
    }
}
