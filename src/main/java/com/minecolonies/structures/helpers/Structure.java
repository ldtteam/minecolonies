package com.minecolonies.structures.helpers;

import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.util.Log;
import com.minecolonies.structures.fake.FakeEntity;
import com.minecolonies.structures.fake.FakeWorld;
import com.minecolonies.structures.lib.ModelHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
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
import java.io.*;
import java.util.List;

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
    private static final double SCALE                    = 1.001;

    /**
     * Template of the structure.
     */
    private Template          template;
    private Minecraft         mc;
    private PlacementSettings settings;

    /**
     * Constuctor of Structure, tries to create a new structure.
     *
     * @param world         with world.
     * @param structureName name of the structure (at stored location).
     * @param settings      it's settings.
     */
    public Structure(@Nullable final World world, final String structureName, final PlacementSettings settings)
    {
        InputStream inputstream = MinecraftServer.class.getResourceAsStream("/assets/" + Constants.MOD_ID + "/schematics/" + structureName + ".nbt");

        if (world == null || world.isRemote)
        {
            this.settings = settings;
            this.mc = Minecraft.getMinecraft();
        }

        //Might be at a different location!
        if (inputstream == null)
        {
            try
            {
                final File decorationFolder;

                if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
                {
                    decorationFolder = new File(Minecraft.getMinecraft().mcDataDir, "minecolonies/");
                }
                else
                {
                    decorationFolder = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory(), "minecolonies/");
                }
                inputstream = new FileInputStream(decorationFolder.getPath() + "/" + structureName + ".nbt");
            }
            catch (final FileNotFoundException e)
            {
                Log.getLogger().warn("Couldn't find any structure with this name anywhere", e);
            }
        }

        if (inputstream == null)
        {
            return;
        }

        try
        {
            this.template = readTemplateFromStream(inputstream);
        }
        catch (final IOException e)
        {
            Log.getLogger().warn(String.format("Failed to load template %s", structureName), e);
        }
        finally
        {
            IOUtils.closeQuietly(inputstream);
        }
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

    public boolean isTemplateNull()
    {
        return template == null;
    }

    /**
     * Checks if the template is null.
     *
     * @return true if it's not.
     */
    public boolean doesExist()
    {
        return template == null ? false : true;
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
            finalEntity.setPosition(entityVec.xCoord, entityVec.yCoord, entityVec.zCoord);
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
            final Block block = aBlockList.blockState.getBlock();
            final IBlockState iblockstate = aBlockList.blockState;
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
            if(anEntityList != null)
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
                    = (double)finalEntity.getMirroredYaw(settings.getMirror()) + ((double)finalEntity.rotationYaw - (double)finalEntity.getRotatedYaw(settings.getRotation()));

            finalEntity.setLocationAndAngles(entityVec.xCoord, entityVec.yCoord, entityVec.zCoord,
                    (float) rotationYaw, finalEntity.rotationPitch);

            final NBTTagCompound nbttagcompound = new NBTTagCompound();
            finalEntity.writeToNBTOptional(nbttagcompound);
            return new Template.EntityInfo(entityInfo.pos, entityInfo.blockPos, nbttagcompound);
        }

        return null;
    }


    /**
     * Transforms the entity's current yaw with the given Rotation and returns it. This does not have a side-effect.
     * @param transformRotation the incoming rotation.
     * @param previousYaw the previous rotation yaw.
     * @return the new rotation yaw.
     */
    public double getRotatedYaw(Rotation transformRotation, double previousYaw)
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
                finalEntity.setLocationAndAngles(entityVec.xCoord, entityVec.yCoord, entityVec.zCoord, (float) rotation, finalEntity.rotationPitch);
            }
            entityList[i] = finalEntity;
        }

        return entityList;
    }

    private static void getQuads(final ModelHolder holder, final List<BakedQuad> quads)
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

    private void renderGhost(final World world, final ModelHolder holder, final EntityPlayer player, final float partialTicks)
    {
        final boolean existingModel = !this.mc.theWorld.isAirBlock(holder.pos);

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
            final FakeWorld fakeWorld = new FakeWorld(holder.actualState, world.getSaveHandler(), world.getWorldInfo(), world.provider, world.theProfiler, true);
            te.setWorldObj(fakeWorld);
            final int pass = 0;

            if (te.shouldRenderInPass(pass))
            {
                final TileEntityRendererDispatcher terd = TileEntityRendererDispatcher.instance;
                terd.func_190056_a(fakeWorld,
                        Minecraft.getMinecraft().renderEngine,
                        Minecraft.getMinecraft().fontRendererObj,
                        new FakeEntity(fakeWorld),
                        null,
                        0.0F);
                GL11.glPushMatrix();
                terd.renderEngine = Minecraft.getMinecraft().renderEngine;
                terd.preDrawBatch();
                GL11.glColor4f(1F, 1F, 1F, 1F);
                terd.renderTileEntity(te, partialTicks, -1);
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
        double xCoord = vec.xCoord;
        final double yCoord = vec.yCoord;
        double zCoord = vec.zCoord;
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
        final VertexBuffer buffer = tessellator.getBuffer();

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
