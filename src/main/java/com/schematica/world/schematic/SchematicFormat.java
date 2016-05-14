package com.schematica.world.schematic;

import com.minecolonies.util.Log;
import com.schematica.world.SchematicWorld;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicFormat
{
    public static final Map<String, SchematicFormat> FORMATS      = new HashMap<>();
    public static final String                       MATERIALS    = "Materials";
    public static final String                       FORMAT_ALPHA = "Alpha";
    public static String FORMAT_DEFAULT;

    public abstract SchematicWorld readFromNBT(NBTTagCompound tagCompound);

    public abstract boolean writeToNBT(NBTTagCompound tagCompound, SchematicWorld world);

    public static SchematicWorld readFromFile(File file)
    {
        try
        {
            InputStream stream = new FileInputStream(file);
            NBTTagCompound tagCompound = CompressedStreamTools.readCompressed(stream);
            String format = tagCompound.getString(MATERIALS);
            SchematicFormat schematicFormat = FORMATS.get(format);

            if(schematicFormat == null)
            {
                throw new UnsupportedFormatException(format);
            }

            return schematicFormat.readFromNBT(tagCompound);
        }
        catch(Exception ex)
        {
            Log.logger.error("Failed to read schematic!", ex);
        }

        return null;
    }

    //Minecolonies start

    public static SchematicWorld readFromStream(InputStream stream)
    {
        try
        {
            NBTTagCompound tagCompound = CompressedStreamTools.readCompressed(stream);
            String format = tagCompound.getString(MATERIALS);
            SchematicFormat schematicFormat = FORMATS.get(format);

            if(schematicFormat == null)
            {
                throw new UnsupportedFormatException(format);
            }

            return schematicFormat.readFromNBT(tagCompound);
        }
        catch(Exception ex)
        {
            throw new IllegalStateException("Failed to read schematic!",ex);
        }
    }

    //Minecolonies end

    public static SchematicWorld readFromFile(String directory, String filename)
    {
        return readFromFile(new File(directory, filename));
    }

    public static boolean writeToFile(File file, SchematicWorld world)
    {
        try
        {
            NBTTagCompound tagCompound = new NBTTagCompound();

            FORMATS.get(FORMAT_DEFAULT).writeToNBT(tagCompound, world);

            try (DataOutputStream dataOutputStream = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file))))
            {
                boolean inDev = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
                Method method = null;
                if(inDev) 
                {
                    method = ReflectionHelper.findMethod(NBTTagCompound.class, null, new String[] {
                            "writeEntry", "a" }, String.class, NBTBase.class, DataOutput.class);
                } 
                else 
                {
                    method = ReflectionHelper.findMethod(NBTTagCompound.class, null, new String[] {
                            "func_150298_a", "a" }, String.class, NBTBase.class, DataOutput.class);
                }
                if(method != null)
                    method.invoke(null, "Schematic", tagCompound, dataOutputStream);
                else
                    throw new NullPointerException("Something went wrong while detecting if we are in development environment or not.");
            }

            return true;
        }
        catch(Exception ex)
        {
            Log.logger.error("Failed to write schematic!", ex);
        }

        return false;
    }

    public static boolean writeToFile(File directory, String filename, SchematicWorld world)
    {
        return writeToFile(new File(directory, filename), world);
    }

    static
    {
        FORMATS.put(FORMAT_ALPHA, new SchematicAlpha());

        FORMAT_DEFAULT = FORMAT_ALPHA;
    }
}
