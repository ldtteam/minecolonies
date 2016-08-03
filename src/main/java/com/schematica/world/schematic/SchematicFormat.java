package com.schematica.world.schematic;

import com.schematica.reference.Names;
import com.schematica.reference.Reference;
import com.schematica.world.storage.Schematic;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public abstract class SchematicFormat
{
    private static final Map<String, SchematicFormat> FORMATS = new HashMap<>();
    private static final String FORMAT_DEFAULT;

    protected abstract Schematic readFromNBT(NBTTagCompound tagCompound);

    protected abstract void writeToNBT(NBTTagCompound tagCompound, Schematic schematic);

    public static Schematic readFromStream(InputStream stream)
    {
        try
        {
            NBTTagCompound tagCompound = CompressedStreamTools.readCompressed(stream);
            String format = tagCompound.getString(Names.NBT.MATERIALS);
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

    public static boolean writeToFile(final File file, final Schematic schematic) {
        try
        {
            final NBTTagCompound tagCompound = new NBTTagCompound();

            FORMATS.get(FORMAT_DEFAULT).writeToNBT(tagCompound, schematic);

            try (final DataOutputStream dataOutputStream = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
                NBTTagCompound.writeEntry(Names.NBT.ROOT, tagCompound, dataOutputStream);
            }

            return true;
        } catch (final Exception ex) {
            Reference.logger.error("Failed to write schematic!", ex);
        }

        return false;
    }

    static {
        FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());

        FORMAT_DEFAULT = Names.NBT.FORMAT_ALPHA;
    }
}
