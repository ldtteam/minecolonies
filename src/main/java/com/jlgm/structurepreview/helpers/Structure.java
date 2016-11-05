package com.jlgm.structurepreview.helpers;

import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.jlgm.structurepreview.fake.FakeEntity;
import com.jlgm.structurepreview.fake.FakeWorld;
import com.jlgm.structurepreview.lib.ModelHolder;

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
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class Structure
{

    private       Template          structure;
    private final Minecraft         mc;
    private       PlacementSettings settings;

    private final int   colorA     = 255;
    private final int   colorR     = 255;
    private final int   colorG     = 255;
    private final int   colorB     = 255;
    private       float brightness = 0.9F;

    public Structure(@Nullable World world, String structureName, PlacementSettings settings)
    {
        World worldObj = world;
        if (worldObj == null)
            worldObj = Minecraft.getMinecraft().getIntegratedServer().getServer().worldServerForDimension(Minecraft.getMinecraft().thePlayer.dimension);
        WorldServer worldserver = (WorldServer) worldObj;
        MinecraftServer minecraftserver = worldObj.getMinecraftServer();
        TemplateManager templatemanager = worldserver.getStructureTemplateManager();
        this.structure = templatemanager.func_189942_b(minecraftserver, new ResourceLocation(structureName));
        this.settings = settings;
        this.mc = Minecraft.getMinecraft();
    }

    public boolean doesExist()
    {
        return structure == null ? false : true;
    }

    public Template.BlockInfo[] getBlockInfo()
    {
        Template.BlockInfo[] blockList = new Template.BlockInfo[structure.blocks.size()];
        blockList = structure.blocks.toArray(blockList);
        return blockList;
    }

    public Template.BlockInfo[] getBlockInfoWithSettings(PlacementSettings settings)
    {
        Template.BlockInfo[] blockList = new Template.BlockInfo[structure.blocks.size()];
        blockList = structure.blocks.toArray(blockList);

        for (int i = 0; i < blockList.length; i++)
        {
            IBlockState finalState = blockList[i].blockState.withMirror(settings.getMirror()).withRotation(settings.getRotation());
            BlockPos finalPos = Template.transformedBlockPos(settings, blockList[i].pos);
            Template.BlockInfo finalInfo = new Template.BlockInfo(finalPos, finalState, blockList[i].tileentityData);
            blockList[i] = finalInfo;
        }
        return blockList;
    }

    public Entity[] getEntityInfo(World world, BlockPos pos)
    {
        Template.EntityInfo[] entityInfoList = new Template.EntityInfo[structure.entities.size()];
        entityInfoList = structure.blocks.toArray(entityInfoList);

        Entity[] entityList = null;

        for (int i = 0; i < entityInfoList.length; i++)
        {
            Entity finalEntity = EntityList.createEntityFromNBT(entityInfoList[i].entityData, world);
            Vec3d entityVec = entityInfoList[i].pos.add(new Vec3d(pos));
            finalEntity.setPosition(entityVec.xCoord, entityVec.yCoord, entityVec.zCoord);
        }

        return entityList;
    }

    public Entity[] getEntityInfoWithSettings(World world, BlockPos pos, PlacementSettings settings)
    {
        Template.EntityInfo[] entityInfoList = new Template.EntityInfo[structure.entities.size()];
        entityInfoList = structure.entities.toArray(entityInfoList);

        Entity[] entityList = new Entity[entityInfoList.length];

        for (int i = 0; i < entityInfoList.length; i++)
        {
            Entity finalEntity = EntityList.createEntityFromNBT(entityInfoList[i].entityData, world);
            Vec3d entityVec = this.transformedVec3d(settings, entityInfoList[i].pos).add(new Vec3d(pos));
            finalEntity.prevRotationYaw = finalEntity.getMirroredYaw(settings.getMirror()) - 90;
            float f = finalEntity.getMirroredYaw(settings.getMirror());
            f = f + (finalEntity.rotationYaw - finalEntity.getRotatedYaw(settings.getRotation()));
            finalEntity.setLocationAndAngles(entityVec.xCoord, entityVec.yCoord, entityVec.zCoord, f, finalEntity.rotationPitch);
            entityList[i] = finalEntity;
        }

        return entityList;
    }

    public BlockPos getSize(Rotation rotation)
    {
        return this.structure.transformedSize(rotation);
    }

    public void setPlacementSettings(PlacementSettings settings)
    {
        this.settings = settings;
    }

    private static Vec3d transformedVec3d(PlacementSettings settings, Vec3d vec)
    {
        Mirror mirrorIn = settings.getMirror();
        Rotation rotationIn = settings.getRotation();
        double xCoord = vec.xCoord;
        double yCoord = vec.yCoord;
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

    public void renderStructure(BlockPos startingPos, final World clientWorld, final EntityPlayer player, final float partialTicks)
    {
        Template.BlockInfo[] blockList = this.getBlockInfoWithSettings(this.settings);
        Entity[] entityList = this.getEntityInfoWithSettings(clientWorld, startingPos, this.settings);

        for (int progress = 0; progress < blockList.length; progress++)
        {
            Block block = blockList[progress].blockState.getBlock();
            IBlockState iblockstate = blockList[progress].blockState;
            BlockPos blockpos = blockList[progress].pos.add(startingPos);
            IBlockState iblockextendedstate = block.getExtendedState(iblockstate, clientWorld, blockpos);
            IBakedModel ibakedmodel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(iblockstate);
            TileEntity tileentity = null;
            if (block.hasTileEntity(iblockstate))
            {
                tileentity = block.createTileEntity(clientWorld, iblockstate);
                tileentity.readFromNBT(blockList[progress].tileentityData);
            }
            ModelHolder models = new ModelHolder(blockpos, iblockstate, iblockextendedstate, tileentity, ibakedmodel);
            this.getQuads(models, models.quads);
            this.renderGhost(clientWorld, models, player, partialTicks);
        }

        for (int i = 0; i < entityList.length; i++)
        {
            Minecraft.getMinecraft().getRenderManager().renderEntityStatic(entityList[i], 0.0F, true);
        }
    }

    private void renderGhost(final World world, ModelHolder holder, final EntityPlayer player, final float partialTicks)
    {
        boolean existingModel = !this.mc.theWorld.isAirBlock(holder.pos);

        IBlockState actualState = holder.actualState;
        Block block = actualState.getBlock();

        if (actualState.getRenderType() == EnumBlockRenderType.MODEL)
        {
            BlockRenderLayer originalLayer = MinecraftForgeClient.getRenderLayer();

            for (BlockRenderLayer layer : BlockRenderLayer.values())
            {
                if (block.canRenderInLayer(actualState, layer))
                {
                    this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    ForgeHooksClient.setRenderLayer(layer);
                    this.renderGhostBlock(world, holder, player, layer, existingModel, partialTicks);
                    holder.rendered = true;
                }
            }

            ForgeHooksClient.setRenderLayer(originalLayer);
        }

        if (holder.te != null && !holder.rendered)
        {
            TileEntity te = holder.te;
            te.setPos(holder.pos);
            FakeWorld fakeWorld = new FakeWorld(holder.actualState, world.getSaveHandler(), world.getWorldInfo(), world.provider, world.theProfiler, true);
            te.setWorldObj(fakeWorld);
            int pass = 0;

            if (te.shouldRenderInPass(pass))
            {
                TileEntityRendererDispatcher terd = TileEntityRendererDispatcher.instance;
                terd.func_190056_a(fakeWorld,
                        Minecraft.getMinecraft().renderEngine,
                        Minecraft.getMinecraft().fontRendererObj,
                        new FakeEntity(fakeWorld),
                        (RayTraceResult) null,
                        0.0F);
                GL11.glPushMatrix();
                terd.renderEngine = Minecraft.getMinecraft().renderEngine;
                terd.preDrawBatch();
                GL11.glColor4f((float) (this.colorR / 255), (float) (this.colorG / 255), (float) (this.colorB / 255), (this.colorA / 255));
                terd.renderTileEntity(te, partialTicks, -1);
                terd.drawBatch(pass);
                GL11.glPopMatrix();
            }
        }
    }

    private void renderGhostBlock(final World world, ModelHolder holder, final EntityPlayer player, BlockRenderLayer layer, boolean existingModel, final float partialTicks)
    {
        double dx = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dy = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        float brightness = this.brightness;
        BlockPos pos = holder.pos;

        GlStateManager.pushMatrix();
        GlStateManager.translate(pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz);

        if (existingModel)
        {
            GlStateManager.scale(1.001, 1.001, 1.001);
        }

        RenderHelper.disableStandardItemLighting();

        if (layer == BlockRenderLayer.CUTOUT)
        {
            this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        }

        GlStateManager.color(1f, 1f, 1f, 1f);

        int alpha = ((int) (1.0F * 0xFF)) << 24;

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

    private void getQuads(ModelHolder holder, List<BakedQuad> quads)
    {
        if (holder.actualState.getRenderType() == EnumBlockRenderType.MODEL)
        {
            BlockRenderLayer originalLayer = MinecraftForgeClient.getRenderLayer();

            for (BlockRenderLayer layer : BlockRenderLayer.values())
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

    private void renderQuads(final World world, final IBlockState actualState, final BlockPos pos, final List<BakedQuad> quads, final int alpha)
    {
        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer buffer = tessellator.getBuffer();

        for (final BakedQuad quad : quads)
        {
            buffer.begin(GL11.GL_QUADS, quad.getFormat());

            final int color = quad.hasTintIndex() ? this.getTint(world, actualState, pos, alpha, quad.getTintIndex()) : alpha | 0xffffff;
            int A = (this.colorA << 24) & 0xFF000000;
            int R = (this.colorR << 16) & 0x00FF0000;
            int G = (this.colorG << 8) & 0x0000FF00;
            int B = (this.colorB) & 0x000000FF;
            int finalColor = 0x00000000 | A | R | G | B;
            LightUtil.renderQuadColor(buffer, quad, color);

            tessellator.draw();
        }
    }

    private int getTint(final World world, final IBlockState actualState, final BlockPos pos, final int alpha, final int tintIndex)
    {
        return alpha | this.mc.getBlockColors().colorMultiplier(actualState, world, pos, tintIndex);
    }
}
