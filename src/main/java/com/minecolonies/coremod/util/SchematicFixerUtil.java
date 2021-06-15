package com.minecolonies.coremod.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.math.BlockPos;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider.*;

/**
 * Util for modifying schematics
 */
public class SchematicFixerUtil
{
    public static void fixSchematics()
    {
        String baseFolder = "E:\\Development\\Java_dev\\minecoloniesLATEST1.16_5_4\\src\\main\\resources\\assets\\minecolonies\\schematics\\";
        File baseFolderFile = new File(baseFolder);


        for (File subFolder : baseFolderFile.listFiles())
        {
            for (File blueprintFile : subFolder.listFiles())
            {
                if (!blueprintFile.exists() || !blueprintFile.getName().contains("blueprint"))
                {
                    continue;
                }
                try
                {
                    FileInputStream inputStream = new FileInputStream(blueprintFile);
                    byte[] data = StructureLoadingUtils.getStreamAsByteArray(inputStream);
                    inputStream.close();
                    CompoundNBT compoundNBT = CompressedStreamTools.readCompressed(new ByteArrayInputStream(data));

                    final Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(compoundNBT);
                    fixSchematicNameAndCorners(blueprint);
                    BlueprintUtil.writeToStream(new FileOutputStream(blueprintFile), blueprint);
                }
                catch (Exception e)
                {
                    Log.getLogger().warn("Could not read file:" + subFolder.getName() + ":" + blueprintFile.getName());
                }
            }
        }
    }

    /**
     * Fixes up internal blueprint data for corners and schematic name
     *
     * @param blueprint
     */
    private static void fixSchematicNameAndCorners(final Blueprint blueprint)
    {
        CompoundNBT compoundNBT = blueprint.getBlockInfoAsMap().get(blueprint.getPrimaryBlockOffset()).getTileEntityData();

        if (compoundNBT == null)
        {
            // Fix offset
            final BlockPos original = blueprint.getPrimaryBlockOffset();
            blueprint.setCachePrimaryOffset(null);
            final BlockPos autoPos = blueprint.getPrimaryBlockOffset();
            blueprint.setCachePrimaryOffset(original);
            compoundNBT = blueprint.getBlockInfoAsMap().get(autoPos).getTileEntityData();

            if (compoundNBT != null && compoundNBT.contains(TAG_BLUEPRINTDATA))
            {
                blueprint.setCachePrimaryOffset(autoPos);
                Log.getLogger().warn("Fixing blueprint schematic anchor for:" + blueprint.getName());
            }
        }

        if (compoundNBT != null && compoundNBT.contains(TAG_BLUEPRINTDATA))
        {
            final CompoundNBT schemDataCompound = (CompoundNBT) compoundNBT.get(TAG_BLUEPRINTDATA);

            if (!schemDataCompound.getString(TAG_SCHEMATIC_NAME).equals(blueprint.getName()))
            {
                schemDataCompound.putString(TAG_SCHEMATIC_NAME, blueprint.getName());
                BlockPosUtil.writeToNBT(schemDataCompound, TAG_CORNER_ONE, BlockPos.ZERO.subtract(blueprint.getPrimaryBlockOffset()));
                BlockPosUtil.writeToNBT(schemDataCompound,
                  TAG_CORNER_TWO,
                  new BlockPos(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1).subtract(blueprint.getPrimaryBlockOffset()));
                Log.getLogger().warn("Fixing blueprint schematic name and corners for:" + blueprint.getName());
            }
        }
    }
}
