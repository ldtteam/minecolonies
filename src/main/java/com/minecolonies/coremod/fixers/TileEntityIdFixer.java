package com.minecolonies.coremod.fixers;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Map;

public class TileEntityIdFixer implements IFixableData
{

    public static final int VERSION = 1;
    private final Map<String, String> idsToFix;

    public TileEntityIdFixer()
    {
        idsToFix = ImmutableMap.<String, String>builder()
                     .put("minecraft:" + Constants.MOD_ID + ".colonybuilding", Constants.MOD_ID + ":colonybuilding")
                     .put("minecraft:" + Constants.MOD_ID + ".scarecrow", Constants.MOD_ID + ":scarecrow")
                     .put("minecraft:" + Constants.MOD_ID + ".wareHouse", Constants.MOD_ID + ":warehouse")
                     .put("minecraft:" + Constants.MOD_ID + ".rack", Constants.MOD_ID + ":rack")
                     .put("minecraft:" + Constants.MOD_ID + ".infoposter", Constants.MOD_ID + ":infoposter")
                     .put("minecraft:" + Constants.MOD_ID + ".multiblock", Constants.MOD_ID + ":multiblock")
                     .put("minecraft:" + Constants.MOD_ID + ".barrel", Constants.MOD_ID + ":barrel")
                     .build();
    }

    @Override
    public int getFixVersion()
    {
        return VERSION;
    }

    @Override
    public NBTTagCompound fixTagCompound(final NBTTagCompound compound)
    {
        String teID = compound.getString("id");

        compound.setString("id", idsToFix.getOrDefault(teID, teID)); //only change value if teID is in the map
        return compound;
    }
}
