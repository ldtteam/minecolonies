package com.minecolonies.structures.helpers;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.structures.fake.FakeEntity;
import com.minecolonies.structures.fake.FakeWorld;
import com.minecolonies.structures.lib.ModelHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

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
    private static final double ONE_HUNDED_EIGHTY_DEGREES = 270D;

    /**
     * Used for scale.
     */
    private static final double SCALE = 1.001;

    /**
     * Size of the buffer.
     */
    private static final int BUFFER_SIZE = 1024;

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
                Log.getLogger().warn(String.format("Failed to load template %s", correctStructureName));
                return;
            }

            try
            {
                this.md5 = Structure.calculateMD5(Structure.getStream(correctStructureName));
                this.template = readTemplateFromStream(inputStream);
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
     * get a InputStream for a give structureName.
     * <p>
     * Look into the following director (in order):
     * - scan
     * - cache
     * - schematics folder
     * - jar
     * It should be the exact opposite that the way used to build the list.
     *
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
        final Structures.StructureName sn = new Structures.StructureName(structureName);
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

        if (inputstream == null)
        {
            Log.getLogger().warn("Structure: Couldn't find any structure with this name " + structureName);
        }

        return inputstream;
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
     * get the Template from the structure.
     *
     * @return The templae for the structure
     */
    public Template getTemplate()
    {
        return this.template;
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
     * Reads a template from an inputstream.
     */
    private static Template readTemplateFromStream(final InputStream stream) throws IOException
    {
        final NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(stream);
        final Template template = new Template();
        template.read(nbttagcompound);
        return template;
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
     * Renders the structure.
     *
     * @param startingPos  the start pos to render.
     * @param clientWorld  the world of the client.
     * @param player       the player object.
     * @param partialTicks the partial ticks.
     */
    public void renderStructure(@NotNull final BlockPos startingPos, @NotNull final World clientWorld, @NotNull final EntityPlayer player, final float partialTicks)
    {
        final Template.BlockInfo[] blockList = this.getBlockInfoWithSettings(this.settings);
        final Entity[] entityList = this.getEntityInfoWithSettings(clientWorld, startingPos, this.settings);

        for (final Template.BlockInfo aBlockList : blockList)
        {
            Block block = aBlockList.blockState.getBlock();
            IBlockState iblockstate = aBlockList.blockState;

            if (block == ModBlocks.blockSubstitution)
            {
                continue;
            }

            if (block == ModBlocks.blockSolidSubstitution)
            {
                iblockstate = BlockUtils.getSubstitutionBlockAtWorld(clientWorld, startingPos);
                block = iblockstate.getBlock();
            }

            final BlockPos blockpos = aBlockList.pos.add(startingPos);
            final IBlockState iBlockExtendedState = block.getExtendedState(iblockstate, clientWorld, blockpos);
            final IBakedModel ibakedmodel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(iblockstate);
            TileEntity tileentity = null;
            if (block.hasTileEntity(iblockstate) && aBlockList.tileentityData != null)
            {
                tileentity = block.createTileEntity(clientWorld, iblockstate);
                tileentity.readFromNBT(aBlockList.tileentityData);
            }
            final ModelHolder models = new ModelHolder(blockpos, iblockstate, iBlockExtendedState, tileentity, ibakedmodel);
            getQuads(models, models.quads);
            this.renderGhost(clientWorld, models, player, partialTicks);
        }

        for (final Entity anEntityList : entityList)
        {
            if (anEntityList != null)
            {
                Minecraft.getMinecraft().getRenderManager().renderEntityStatic(anEntityList, 0.0F, true);
            }
        }
    }

    /**
     * Get blockInfo of structure with a specific setting.
     *
     * @param settings the setting.
     * @return the block info array.
     */
    public Template.BlockInfo[] getBlockInfoWithSettings(final PlacementSettings settings)
    {
        Template.BlockInfo[] blockList = new Template.BlockInfo[template.blocks.size()];
        blockList = template.blocks.toArray(blockList);

        for (int i = 0; i < blockList.length; i++)
        {
            final IBlockState finalState = blockList[i].blockState.withMirror(settings.getMirror()).withRotation(settings.getRotation());
            final BlockPos finalPos = Template.transformedBlockPos(settings, blockList[i].pos);
            final Template.BlockInfo finalInfo = new Template.BlockInfo(finalPos, finalState, blockList[i].tileentityData);
            blockList[i] = finalInfo;
        }
        return blockList;
    }

    /**
     * Get entity info with specific setting.
     *
     * @param world    world the entity is in.
     * @param pos      the position it is at.
     * @param settings the settings.
     * @return the entity info aray.
     */
    public Entity[] getEntityInfoWithSettings(final World world, final BlockPos pos, final PlacementSettings settings)
    {
        Template.EntityInfo[] entityInfoList = new Template.EntityInfo[template.entities.size()];
        entityInfoList = template.entities.toArray(entityInfoList);

        final Entity[] entityList = new Entity[entityInfoList.length];

        for (int i = 0; i < entityInfoList.length; i++)
        {
            final Entity finalEntity = EntityList.createEntityFromNBT(entityInfoList[i].entityData, world);
            final Vec3d entityVec = Structure.transformedVec3d(settings, entityInfoList[i].pos).add(new Vec3d(pos));

            if (finalEntity != null)
            {
                finalEntity.prevRotationYaw = (float) (finalEntity.getMirroredYaw(settings.getMirror()) - NINETY_DEGREES);
                final double rotation =
                  (double) finalEntity.getMirroredYaw(settings.getMirror()) + ((double) finalEntity.rotationYaw - finalEntity.getRotatedYaw(settings.getRotation()));
                finalEntity.setLocationAndAngles(entityVec.x, entityVec.y, entityVec.z, (float) rotation, finalEntity.rotationPitch);
            }
            entityList[i] = finalEntity;
        }

        return entityList;
    }

    public static void getQuads(final ModelHolder holder, final List<BakedQuad> quads)
    {
        if (holder.actualState.getRenderType() == EnumBlockRenderType.MODEL)
        {
            final BlockRenderLayer originalLayer = MinecraftForgeClient.getRenderLayer();

            for (final BlockRenderLayer layer : BlockRenderLayer.values())
            {
                if (holder.actualState.getBlock().canRenderInLayer(holder.actualState, layer))
                {
                    ForgeHooksClient.setRenderLayer(layer);

                    for (final EnumFacing facing : EnumFacing.values())
                    {
                        quads.addAll(holder.model.getQuads(holder.extendedState, facing, 0));
                    }

                    quads.addAll(holder.model.getQuads(holder.extendedState, null, 0));
                }
            }

            ForgeHooksClient.setRenderLayer(originalLayer);
        }
    }

    public void renderGhost(final World world, final ModelHolder holder, final EntityPlayer player, final float partialTicks)
    {
        final boolean existingModel = !this.mc.world.isAirBlock(holder.pos);

        final IBlockState actualState = holder.actualState;
        final Block block = actualState.getBlock();

        if (actualState.getRenderType() == EnumBlockRenderType.MODEL)
        {
            final BlockRenderLayer originalLayer = MinecraftForgeClient.getRenderLayer();

            for (final BlockRenderLayer layer : BlockRenderLayer.values())
            {
                if (block.canRenderInLayer(actualState, layer))
                {
                    this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    ForgeHooksClient.setRenderLayer(layer);
                    this.renderGhostBlock(world, holder, player, layer, existingModel, partialTicks);
                    holder.setRendered(true);
                }
            }

            ForgeHooksClient.setRenderLayer(originalLayer);
        }

        if (holder.te != null && !holder.isRendered())
        {
            final TileEntity te = holder.te;
            te.setPos(holder.pos);
            final FakeWorld fakeWorld = new FakeWorld(holder.actualState, world.getSaveHandler(), world.getWorldInfo(), world.provider, world.profiler, true);
            te.setWorld(fakeWorld);
            final int pass = 0;

            if (te.shouldRenderInPass(pass))
            {
                final TileEntityRendererDispatcher terd = TileEntityRendererDispatcher.instance;
                terd.prepare(fakeWorld,
                  Minecraft.getMinecraft().renderEngine,
                  Minecraft.getMinecraft().fontRenderer,
                  new FakeEntity(fakeWorld),
                  null,
                  0.0F);
                GL11.glPushMatrix();
                terd.renderEngine = Minecraft.getMinecraft().renderEngine;
                terd.preDrawBatch();
                GL11.glColor4f(1F, 1F, 1F, 1F);
                terd.render(te, partialTicks, -1);
                terd.drawBatch(pass);
                GL11.glPopMatrix();
            }
        }
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

    private void renderGhostBlock(
                                   final World world,
                                   final ModelHolder holder,
                                   final EntityPlayer player,
                                   final BlockRenderLayer layer,
                                   final boolean existingModel,
                                   final float partialTicks)
    {
        final double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        final double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        final double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        final BlockPos pos = holder.pos;

        GlStateManager.pushMatrix();
        GlStateManager.translate(pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz);

        if (existingModel)
        {
            GlStateManager.scale(SCALE, SCALE, SCALE);
        }

        RenderHelper.disableStandardItemLighting();

        if (layer == BlockRenderLayer.CUTOUT)
        {
            this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        }

        GlStateManager.color(1F, 1F, 1F, 1F);

        final int alpha = ((int) (1.0D * 0xFF)) << 24;

        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.colorMask(false, false, false, false);
        this.renderModel(world, holder, pos, alpha);

        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        this.renderModel(world, holder, pos, alpha);

        GlStateManager.disableBlend();

        if (layer == BlockRenderLayer.CUTOUT)
        {
            this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        }

        GlStateManager.popMatrix();
    }

    private void renderModel(final World world, final ModelHolder holder, final BlockPos pos, final int alpha)
    {
        for (final EnumFacing facing : EnumFacing.values())
        {
            this.renderQuads(world, holder.actualState, pos, holder.model.getQuads(holder.extendedState, facing, 0), alpha);
        }

        this.renderQuads(world, holder.actualState, pos, holder.model.getQuads(holder.extendedState, null, 0), alpha);
    }

    private void renderQuads(final World world, final IBlockState actualState, final BlockPos pos, final List<BakedQuad> quads, final int alpha)
    {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        for (final BakedQuad quad : quads)
        {
            buffer.begin(GL11.GL_QUADS, quad.getFormat());

            final int color = quad.hasTintIndex() ? this.getTint(world, actualState, pos, alpha, quad.getTintIndex()) : (alpha | 0xffffff);

            LightUtil.renderQuadColor(buffer, quad, color);

            tessellator.draw();
        }
    }

    private int getTint(final World world, final IBlockState actualState, final BlockPos pos, final int alpha, final int tintIndex)
    {
        return alpha | this.mc.getBlockColors().colorMultiplier(actualState, world, pos, tintIndex);
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
     * Transforms the entity's current yaw with the given Rotation and returns it. This does not have a side-effect.
     *
     * @param transformRotation the incoming rotation.
     * @param previousYaw       the previous rotation yaw.
     * @return the new rotation yaw.
     */
    public double getRotatedYaw(final Rotation transformRotation, final double previousYaw)
    {
        switch (transformRotation)
        {
            case CLOCKWISE_180:
                return previousYaw + NINETY_DEGREES;
            case COUNTERCLOCKWISE_90:
                return previousYaw + TWO_HUNDRED_SEVENTY_DEGREES;
            case CLOCKWISE_90:
                return previousYaw + ONE_HUNDED_EIGHTY_DEGREES;
            default:
                return previousYaw;
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
