package com.minecolonies.core.util;

import com.google.common.io.Files;
import com.ldtteam.structurize.api.BlockPosUtil;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.minecolonies.api.util.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.*;

/**
 * Util for modifying schematics
 */
public class SchemFixerUtil
{
    public static void fixSchematics()
    {
        String baseFolder = Paths.get("").toAbsolutePath().getParent().toString() + "/src/main/resources/assets/minecolonies/schematics";
        File baseFolderFile = new File(baseFolder);
        if (!baseFolderFile.exists()) { return; }
        final List<File> files = Arrays.asList(baseFolderFile.listFiles());
        for (File subFolder :files)
        {
            final File[] subFileArray = subFolder.listFiles();
            if (subFileArray == null)
            {
                continue;
            }
            final List<File> subFiles = Arrays.asList(subFileArray);
            for (File blueprintFile : subFiles)
            {
                if (!blueprintFile.exists() || !blueprintFile.getName().contains("blueprint"))
                {
                    continue;
                }
                try
                {
                    if (blueprintFile.getName().startsWith("home"))
                    {
                        Files.move(blueprintFile, new File(blueprintFile.getPath().substring(0, blueprintFile.getPath().lastIndexOf("/") + 1) + blueprintFile.getName().replace("home", "residence")));
                    }
                    else if (blueprintFile.getName().startsWith("citizen"))
                    {
                        Files.move(blueprintFile, new File(blueprintFile.getPath().substring(0, blueprintFile.getPath().lastIndexOf("/") + 1) + blueprintFile.getName().replace("citizen", "residence")));
                    }
                }
                catch (Exception ex)
                {
                    Log.getLogger().warn("Failed rename.", ex);
                }
            }
        }

        final List<File> followFiles = Arrays.asList(baseFolderFile.listFiles());
        for (File subFolder : followFiles)
        {
            final File[] subFileArray = subFolder.listFiles();
            if (subFileArray == null)
            {
                continue;
            }
            final List<File> subFiles = Arrays.asList(subFileArray);
            for (File blueprintFile : subFiles)
            {
                if (!blueprintFile.exists() || !blueprintFile.getName().contains("blueprint"))
                {
                    continue;
                }
                try
                {
                    CompoundTag compoundNBT = NbtIo.readCompressed(new ByteArrayInputStream(java.nio.file.Files.readAllBytes(blueprintFile.toPath())), NbtAccounter.unlimitedHeap());
                    final Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(compoundNBT);
                    if (fixSchematicNameAndCorners(blueprint))
                    {
                        BlueprintUtil.writeToStream(new FileOutputStream(blueprintFile), blueprint);
                    }
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
    private static boolean fixSchematicNameAndCorners(final Blueprint blueprint)
    {
        boolean changed = false;
        CompoundTag compoundNBT = blueprint.getBlockInfoAsMap().get(blueprint.getPrimaryBlockOffset()).getTileEntityData();

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
                changed = true;
            }
        }

        if (compoundNBT != null && compoundNBT.contains(TAG_BLUEPRINTDATA))
        {
            final CompoundTag schemDataCompound = (CompoundTag) compoundNBT.get(TAG_BLUEPRINTDATA);
            final String name = schemDataCompound.getString(TAG_SCHEMATIC_NAME);
            if (name.contains("citizen") || name.contains("home"))
            {
                blueprint.setName(blueprint.getName().replace("home", "residence"));
                blueprint.setName(blueprint.getName().replace("citizen", "residence"));
            }
            if (!name.equals(blueprint.getName()))
            {
                schemDataCompound.putString(TAG_SCHEMATIC_NAME, blueprint.getName());
                BlockPosUtil.writeToNBT(schemDataCompound, TAG_CORNER_ONE, BlockPos.ZERO.subtract(blueprint.getPrimaryBlockOffset()));
                BlockPosUtil.writeToNBT(schemDataCompound,
                  TAG_CORNER_TWO,
                  new BlockPos(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1).subtract(blueprint.getPrimaryBlockOffset()));
                Log.getLogger().warn("Fixing blueprint schematic name and corners for:" + blueprint.getName());
                return true;
            }
        }
        return changed;
    }
}
