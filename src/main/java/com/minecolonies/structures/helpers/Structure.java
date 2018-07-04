package com.minecolonies.structures.helpers;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.StructureName;
import com.minecolonies.coremod.colony.Structures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.minecolonies.api.util.constant.Suppression.RESOURCES_SHOULD_BE_CLOSED;

/**
 * Structure class, used to store, create, get structures.
 */
public class Structure
{
    /**
     * Max amount of entities to render.
     */
    private static final int MAX_ENTITIES_TO_RENDER = 10;

    /**
     * Rotation by 90°.
     */
    private static final double NINETY_DEGREES = 90D;

    /**
     * Rotation by 270°.
     */
    private static final double TWO_HUNDRED_SEVENTY_DEGREES = 270D;

    /**
     * Rotation by 180°.
     */
    private static final double ONE_HUNDED_EIGHTY_DEGREES = 180D;

    /**
     * Used for scale.
     */
    private static final double SCALE = 1.001;

    /**
     * Size of the buffer.
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * Required Datafixer
     */
    private final DataFixer fixer;

    /**
     * The last starting position.
     */
    private BlockPos lastStartingPos = BlockPos.ORIGIN;

    /**
     * Template of the structure.
     */
    private Template          template;
    private Minecraft         mc;
    private PlacementSettings settings;
    private String            md5;

    /**
     * Constuctor of Structure, tries to create a new structure.
     *
     * @param world         with world.
     * @param structureName name of the structure (at stored location).
     * @param settings      it's settings.
     */
    public Structure(@Nullable final World world, final String structureName, final PlacementSettings settings)
    {
        String correctStructureName = structureName;
        if (world == null || world.isRemote)
        {
            this.settings = settings;
            this.mc = Minecraft.getMinecraft();
        }
        this.fixer = DataFixesManager.createFixer();

        InputStream inputStream = null;
        try
        {

            //Try the cache first
            if (Structures.hasMD5(correctStructureName))
            {
                inputStream = Structure.getStream(Structures.SCHEMATICS_CACHE + '/' + Structures.getMD5(correctStructureName));
                if (inputStream != null)
                {
                    correctStructureName = Structures.SCHEMATICS_CACHE + '/' + Structures.getMD5(correctStructureName);
                }
            }

            if (inputStream == null)
            {
                inputStream = Structure.getStream(correctStructureName);
            }

            if (inputStream == null)
            {
                return;
            }

            try
            {
                this.md5 = Structure.calculateMD5(Structure.getStream(correctStructureName));
                this.template = readTemplateFromStream(inputStream, fixer);
            }
            catch (final IOException e)
            {
                Log.getLogger().warn(String.format("Failed to load template %s", correctStructureName), e);
            }
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * get a InputStream for a give structureName.
     * <p>
     * Look into the following director (in order):
     * - scan
     * - cache
     * - schematics folder
     * - jar
     * It should be the exact opposite that the way used to build the list.
     * <p>
     * Suppressing Sonar Rule squid:S2095
     * This rule enforces "Close this InputStream"
     * But in this case the rule does not apply because
     * We are returning the stream and that is reasonable
     *
     * @param structureName name of the structure to load
     * @return the input stream or null
     */
    @SuppressWarnings(RESOURCES_SHOULD_BE_CLOSED)
    @Nullable
    public static InputStream getStream(final String structureName)
    {
        final StructureName sn = new StructureName(structureName);
        InputStream inputstream = null;
        if (Structures.SCHEMATICS_CACHE.equals(sn.getPrefix()))
        {
            return Structure.getStreamFromFolder(Structure.getCachedSchematicsFolder(), structureName);
        }
        else if (Structures.SCHEMATICS_SCAN.equals(sn.getPrefix()))
        {
            return Structure.getStreamFromFolder(Structure.getClientSchematicsFolder(), structureName);
        }
        else if (!Structures.SCHEMATICS_PREFIX.equals(sn.getPrefix()))
        {
            return null;
        }
        else
        {
            //Look in the folder first
            inputstream = Structure.getStreamFromFolder(MineColonies.proxy.getSchematicsFolder(), structureName);
            if (inputstream == null && !Configurations.gameplay.ignoreSchematicsFromJar)
            {
                inputstream = Structure.getStreamFromJar(structureName);
            }
        }

        return inputstream;
    }

    /**
     * Calculate the MD5 hash for a template from an inputstream.
     *
     * @param stream to which we want the MD5 hash
     * @return the MD5 hash string or null
     */
    public static String calculateMD5(final InputStream stream)
    {
        if (stream == null)
        {
            Log.getLogger().error("Structure.calculateMD5: stream is null, this should not happen");
            return null;
        }
        return calculateMD5(getStreamAsByteArray(stream));
    }

    /**
     * Reads a template from an inputstream.
     */
    private static Template readTemplateFromStream(final InputStream stream, final DataFixer fixer) throws IOException
    {
        final NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(stream);

        if (!nbttagcompound.hasKey("DataVersion", 99))
        {
            nbttagcompound.setInteger("DataVersion", 500);
        }

        final Template template = new Template();
        template.read(fixer.process(FixTypes.STRUCTURE, nbttagcompound));
        return template;
    }

    /**
     * get a input stream for a schematic within a specif folder.
     *
     * @param folder        where to load it from.
     * @param structureName name of the structure to load.
     * @return the input stream or null
     */
    @Nullable
    private static InputStream getStreamFromFolder(@Nullable final File folder, final String structureName)
    {
        if (folder == null)
        {
            return null;
        }
        final File nbtFile = new File(folder.getPath() + "/" + structureName + ".nbt");
        try
        {
            if (folder.exists())
            {
                //We need to check that we stay within the correct folder
                if (!nbtFile.toURI().normalize().getPath().startsWith(folder.toURI().normalize().getPath()))
                {
                    Log.getLogger().error("Structure: Illegal structure name \"" + structureName + "\"");
                    return null;
                }
                if (nbtFile.exists())
                {
                    return new FileInputStream(nbtFile);
                }
            }
        }
        catch (final FileNotFoundException e)
        {
            //we should will never go here
            Log.getLogger().error("Structure.getStreamFromFolder", e);
        }
        return null;
    }

    /**
     * Get the file representation of the cached schematics' folder.
     *
     * @return the folder for the cached schematics
     */
    @Nullable
    public static File getCachedSchematicsFolder()
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            if (ColonyManager.getServerUUID() != null)
            {
                return new File(Minecraft.getMinecraft().mcDataDir, Constants.MOD_ID + "/" + ColonyManager.getServerUUID());
            }
            else
            {
                Log.getLogger().error("ColonyManager.getServerUUID() => null this should not happen");
                return null;
            }
        }
        return new File(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getSaveHandler().getWorldDirectory()
                          + "/" + Constants.MOD_ID);
    }

    /**
     * get the schematic folder for the client.
     *
     * @return the client folder.
     */
    public static File getClientSchematicsFolder()
    {
        return new File(Minecraft.getMinecraft().mcDataDir, Constants.MOD_ID);
    }

    /**
     * get a input stream for a schematic from jar.
     *
     * @param structureName name of the structure to load from the jar.
     * @return the input stream or null
     */
    @Nullable
    private static InputStream getStreamFromJar(final String structureName)
    {
        return MinecraftServer.class.getResourceAsStream("/assets/" + Constants.MOD_ID + '/' + structureName + ".nbt");
    }

    /**
     * Calculate the MD5 hash of a byte array
     *
     * @param bytes array
     * @return the MD5 hash string or null
     */
    public static String calculateMD5(final byte[] bytes)
    {
        try
        {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            return DatatypeConverter.printHexBinary(md.digest(bytes));
        }
        catch (@NotNull final NoSuchAlgorithmException e)
        {
            Log.getLogger().trace(e);
        }

        return null;
    }

    /**
     * Convert an InputStream into and array of bytes.
     *
     * @param stream to be converted to bytes array
     * @return the array of bytes, array is size 0 when the stream is null
     */
    public static byte[] getStreamAsByteArray(final InputStream stream)
    {
        if (stream == null)
        {
            Log.getLogger().info("Structure.getStreamAsByteArray: stream is null this should not happen");
            return new byte[0];
        }
        try
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            final byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = stream.read(data, 0, data.length)) != -1)
            {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
        catch (@NotNull final IOException e)
        {
            Log.getLogger().trace(e);
        }
        return new byte[0];
    }

    /**
     * Constuctor of Structure, tries to create a new structure.
     * creates a plain Structure to append rendering later.
     *
     * @param world with world.
     */
    public Structure(@Nullable final World world)
    {
        if (world == null || world.isRemote)
        {
            this.settings = settings;
            this.mc = Minecraft.getMinecraft();
        }
        this.fixer = DataFixesManager.createFixer();
    }

    public static byte[] compress(final byte[] data)
    {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
        try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream))
        {
            zipStream.write(data);
        }
        catch (@NotNull final IOException e)
        {
            Log.getLogger().error("Could not compress the data", e);
        }
        return byteStream.toByteArray();
    }

    public static byte[] uncompress(final byte[] data)
    {
        final byte[] buffer = new byte[BUFFER_SIZE];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             GZIPInputStream zipStream = new GZIPInputStream(byteStream))
        {
            int len;
            while ((len = zipStream.read(buffer)) > 0)
            {
                out.write(buffer, 0, len);
            }
        }
        catch (@NotNull final IOException e)
        {
            Log.getLogger().warn("Could not uncompress data", e);
        }

        return out.toByteArray();
    }

    /**
     * get the Template from the structure.
     *
     * @return The templae for the structure
     */
    public Template getTemplate()
    {
        return this.template;
    }

    /**
     * Compare the md5 from the structure with an other md5 hash.
     *
     * @param otherMD5 to compare with
     * @return whether the otherMD5 match, return false if md5 is null
     */
    public boolean isCorrectMD5(final String otherMD5)
    {
        Log.getLogger().info("isCorrectMD5: md5:" + md5 + " other:" + otherMD5);
        if (md5 == null || otherMD5 == null)
        {
            return false;
        }
        return md5.compareTo(otherMD5) == 0;
    }

    /**
     * Checks if the template is null.
     *
     * @return true if the template is null.
     */
    public boolean isTemplateMissing()
    {
        return template == null;
    }

    public Template.BlockInfo[] getBlockInfo()
    {
        Template.BlockInfo[] blockList = new Template.BlockInfo[template.blocks.size()];
        blockList = template.blocks.toArray(blockList);
        return blockList;
    }

    /**
     * Get entity array at position in world.
     *
     * @param world the world.
     * @param pos   the position.
     * @return the entity array.
     */
    public Entity[] getEntityInfo(final World world, final BlockPos pos)
    {
        Template.EntityInfo[] entityInfoList = new Template.EntityInfo[template.entities.size()];
        entityInfoList = template.blocks.toArray(entityInfoList);

        final Entity[] entityList = null;

        for (int i = 0; i < entityInfoList.length; i++)
        {
            final Entity finalEntity = EntityList.createEntityFromNBT(entityInfoList[i].entityData, world);
            final Vec3d entityVec = entityInfoList[i].pos.add(new Vec3d(pos));
            finalEntity.setPosition(entityVec.x, entityVec.y, entityVec.z);
        }

        return entityList;
    }

    /**
     * Get size of structure.
     *
     * @param rotation with rotation.
     * @return size as blockPos (x = length, z = width, y = height).
     */
    public BlockPos getSize(final Rotation rotation)
    {
        return this.template.transformedSize(rotation);
    }

    public void setPlacementSettings(final PlacementSettings settings)
    {
        this.settings = settings;
    }

    /**
     * Get blockInfo of structure with a specific setting.
     *
     * @param settings the setting.
     * @return the block info array.
     */
    public ImmutableList<Template.BlockInfo> getBlockInfoWithSettings(final PlacementSettings settings)
    {
        final ImmutableList.Builder<Template.BlockInfo> builder = ImmutableList.builder();

        template.blocks.forEach(blockInfo -> {
            final IBlockState finalState = blockInfo.blockState.withMirror(settings.getMirror()).withRotation(settings.getRotation());
            final BlockPos finalPos = Template.transformedBlockPos(settings, blockInfo.pos);
            final Template.BlockInfo finalInfo = new Template.BlockInfo(finalPos, finalState, blockInfo.tileentityData);
            builder.add(finalInfo);
        });

        return builder.build();
    }

    /**
     * Get entity info with specific setting.
     *
     * @param entityInfo the entity to transform.
     * @param world      world the entity is in.
     * @param pos        the position it is at.
     * @param settings   the settings.
     * @return the entity info aray.
     */
    public Template.EntityInfo transformEntityInfoWithSettings(final Template.EntityInfo entityInfo, final World world, final BlockPos pos, final PlacementSettings settings)
    {
        final Entity finalEntity = EntityList.createEntityFromNBT(entityInfo.entityData, world);

        //err might be here? only use pos? or don't add?
        final Vec3d entityVec = Structure.transformedVec3d(settings, entityInfo.pos).add(new Vec3d(pos));

        if (finalEntity != null)
        {
            finalEntity.prevRotationYaw = (float) (finalEntity.getMirroredYaw(settings.getMirror()) - NINETY_DEGREES);
            final double rotationYaw
              = (double) finalEntity.getMirroredYaw(settings.getMirror()) + ((double) finalEntity.rotationYaw - (double) finalEntity.getRotatedYaw(settings.getRotation()));

            finalEntity.setLocationAndAngles(entityVec.x, entityVec.y, entityVec.z,
              (float) rotationYaw, finalEntity.rotationPitch);

            final NBTTagCompound nbttagcompound = new NBTTagCompound();
            finalEntity.writeToNBTOptional(nbttagcompound);
            return new Template.EntityInfo(entityInfo.pos, entityInfo.blockPos, nbttagcompound);
        }

        return null;
    }

    /**
     * Transform a Vec3d with placement settings.
     *
     * @param settings the settings.
     * @param vec      the vector.
     * @return the new vector.
     */
    public static Vec3d transformedVec3d(final PlacementSettings settings, final Vec3d vec)
    {
        final Mirror mirrorIn = settings.getMirror();
        final Rotation rotationIn = settings.getRotation();
        double xCoord = vec.x;
        final double yCoord = vec.y;
        double zCoord = vec.z;
        boolean flag = true;

        switch (mirrorIn)
        {
            case LEFT_RIGHT:
                zCoord = 1.0D - zCoord;
                break;
            case FRONT_BACK:
                xCoord = 1.0D - xCoord;
                break;
            default:
                flag = false;
        }

        switch (rotationIn)
        {
            case COUNTERCLOCKWISE_90:
                return new Vec3d(zCoord, yCoord, 1.0D - xCoord);
            case CLOCKWISE_90:
                return new Vec3d(1.0D - zCoord, yCoord, xCoord);
            case CLOCKWISE_180:
                return new Vec3d(1.0D - xCoord, yCoord, 1.0D - zCoord);
            default:
                return flag ? new Vec3d(xCoord, yCoord, zCoord) : vec;
        }
    }

    /**
     * Get all additional entities.
     *
     * @return list of entities.
     */
    public List<Template.EntityInfo> getTileEntities()
    {
        return template.entities;
    }

    /**
     * Get the Placement settings of the structure.
     *
     * @return the settings.
     */
    public PlacementSettings getSettings()
    {
        return settings;
    }
}
