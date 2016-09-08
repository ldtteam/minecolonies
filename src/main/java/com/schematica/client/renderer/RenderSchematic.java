package com.schematica.client.renderer;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.schematica.Settings;
import com.schematica.client.renderer.chunk.OverlayRenderDispatcher;
import com.schematica.client.renderer.chunk.container.AbstractSchematicChunkRenderContainer;
import com.schematica.client.renderer.chunk.container.SchematicChunkRenderContainerList;
import com.schematica.client.renderer.chunk.container.SchematicChunkRenderContainerVbo;
import com.schematica.client.renderer.chunk.overlay.ISchematicRenderChunkFactory;
import com.schematica.client.renderer.chunk.overlay.RenderOverlay;
import com.schematica.client.renderer.chunk.overlay.RenderOverlayList;
import com.schematica.client.renderer.chunk.proxy.SchematicRenderChunkList;
import com.schematica.client.renderer.chunk.proxy.SchematicRenderChunkVbo;
import com.schematica.client.renderer.shader.ShaderProgram;
import com.schematica.client.world.SchematicWorld;
import com.schematica.core.client.renderer.GeometryMasks;
import com.schematica.core.client.renderer.GeometryTessellator;
import com.schematica.handler.ConfigurationHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderSchematic extends RenderGlobal
{
    public static final RenderSchematic INSTANCE = new RenderSchematic(Minecraft.getMinecraft());

    private static final int RENDER_DISTANCE = 32;
    private static final int CHUNKS_XZ       = (RENDER_DISTANCE + 1) * 2;
    private static final int CHUNKS_Y        = 16;
    private static final int CHUNKS          = CHUNKS_XZ * CHUNKS_XZ * CHUNKS_Y;
    private static final int PASS            = 2;

    private static final ShaderProgram SHADER_ALPHA         = new ShaderProgram("minecolonies", null, "shaders/alpha.frag");
    private static final double        DOUBLE_EPSILON       = 0.00000001D;
    private static       Vec3d          playerPositionOffset = new Vec3d(0, 0, 0);
    @NotNull
    private final Minecraft     mc;
    private final Profiler      profiler;
    private final RenderManager renderManager;
    private final Set<RenderOverlay>                    overlaysToUpdate = Sets.newLinkedHashSet();
    private final ChunkRenderDispatcher   renderDispatcher        = new ChunkRenderDispatcher();
    private final OverlayRenderDispatcher renderDispatcherOverlay = new OverlayRenderDispatcher();
    private final BlockPos.MutableBlockPos tmp                      = new BlockPos.MutableBlockPos();
    @Nullable
    private SchematicWorld world;
    @NotNull
    private Set<RenderChunk>                      chunksToUpdate = Sets.newLinkedHashSet();
    @NotNull
    private List<ContainerLocalRenderInformation> renderInfos    = Lists.newArrayListWithCapacity(CHUNKS);
    @Nullable
    private ViewFrustumOverlay viewFrustum;
    private       double                  frustumUpdatePosX       = Double.MIN_VALUE;
    private       double                  frustumUpdatePosY       = Double.MIN_VALUE;
    private       double                  frustumUpdatePosZ       = Double.MIN_VALUE;
    private       int                     frustumUpdatePosChunkX  = Integer.MIN_VALUE;
    private       int                     frustumUpdatePosChunkY  = Integer.MIN_VALUE;
    private       int                     frustumUpdatePosChunkZ  = Integer.MIN_VALUE;
    private       double                  lastViewEntityX         = Double.MIN_VALUE;
    private       double                  lastViewEntityY         = Double.MIN_VALUE;
    private       double                  lastViewEntityZ         = Double.MIN_VALUE;
    private       double                  lastViewEntityPitch     = Double.MIN_VALUE;
    private       double                  lastViewEntityYaw       = Double.MIN_VALUE;
    private AbstractSchematicChunkRenderContainer renderContainer;
    private int renderDistanceChunks = -1;
    private int countEntitiesTotal;
    private int countEntitiesRendered;
    private boolean vboEnabled = false;
    private ISchematicRenderChunkFactory renderChunkFactory;
    private double                       prevRenderSortX;
    private double                       prevRenderSortY;
    private double                       prevRenderSortZ;
    private       boolean                  displayListEntitiesDirty = true;
    private       int                      frameCount               = 0;

    private RenderSchematic(@NotNull final Minecraft minecraft)
    {
        super(minecraft);
        this.mc = minecraft;
        this.profiler = minecraft.mcProfiler;
        this.renderManager = minecraft.getRenderManager();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.bindTexture(0);
        this.vboEnabled = OpenGlHelper.useVbo();

        if (this.vboEnabled)
        {
            initVbo();
        }
        else
        {
            initList();
        }
    }

    private void initVbo()
    {
        this.renderContainer = new SchematicChunkRenderContainerVbo();
        this.renderChunkFactory = new ISchematicRenderChunkFactory()
        {
            @NotNull
            @Override
            public RenderChunk create(final World world, final RenderGlobal renderGlobal, final int index)
            {
                return new SchematicRenderChunkVbo(world, renderGlobal, index);
            }

            @NotNull
            @Override
            public RenderOverlay makeRenderOverlay(final World world, final RenderGlobal renderGlobal, @NotNull final BlockPos pos, final int index)
            {
                return new RenderOverlay(world, renderGlobal, index);
            }
        };
    }

    private void initList()
    {
        this.renderContainer = new SchematicChunkRenderContainerList();
        this.renderChunkFactory = new ISchematicRenderChunkFactory()
        {
            @NotNull
            @Override
            public RenderChunk create(final World world, final RenderGlobal renderGlobal, final int index)
            {
                return new SchematicRenderChunkList(world, renderGlobal, index);
            }

            @NotNull
            @Override
            public RenderOverlay makeRenderOverlay(final World world, final RenderGlobal renderGlobal, @NotNull final BlockPos pos, final int index)
            {
                return new RenderOverlayList(world, renderGlobal, index);
            }
        };
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager)
    {
        //Not Needed
    }

    @Override
    public void makeEntityOutlineShader()
    {
        //Not Needed
    }

    @Override
    public void renderEntityOutlineFramebuffer()
    {
        //Not Needed
    }

    @Override
    protected boolean isRenderEntityOutlines()
    {
        return false;
    }

    @Override
    public void setWorldAndLoadRenderers(final WorldClient worldClient)
    {
        if (worldClient instanceof SchematicWorld)
        {
            setWorldAndLoadRenderers((SchematicWorld) worldClient);
        }
        else
        {
            setWorldAndLoadRenderers(null);
        }
    }

    public void setWorldAndLoadRenderers(@Nullable final SchematicWorld world)
    {
        if (this.world != null)
        {
            this.world.removeEventListener(this);
        }

        this.frustumUpdatePosX = Double.MIN_VALUE;
        this.frustumUpdatePosY = Double.MIN_VALUE;
        this.frustumUpdatePosZ = Double.MIN_VALUE;
        this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        this.renderManager.set(world);
        this.world = world;

        if (world != null)
        {
            world.addEventListener(this);
            loadRenderers();
        }
    }

    @Override
    public void loadRenderers()
    {
        if (this.world != null)
        {
            this.displayListEntitiesDirty = true;
            this.renderDistanceChunks = ConfigurationHandler.renderDistance;
            final boolean vbo = this.vboEnabled;
            this.vboEnabled = OpenGlHelper.useVbo();

            if (vbo && !this.vboEnabled)
            {
                initList();
            }
            else if (!vbo && this.vboEnabled)
            {
                initVbo();
            }

            if (this.viewFrustum != null)
            {
                this.viewFrustum.deleteGlResources();
            }

            stopChunkUpdates();
            this.viewFrustum = new ViewFrustumOverlay(this.world, this.renderDistanceChunks, this, this.renderChunkFactory);

            final double posX = playerPositionOffset.xCoord;
            final double posZ = playerPositionOffset.zCoord;
            this.viewFrustum.updateChunkPositions(posX, posZ);
        }
    }

    @Override
    protected void stopChunkUpdates()
    {
        this.chunksToUpdate.clear();
        this.overlaysToUpdate.clear();
        this.renderDispatcher.stopChunkUpdates();
        this.renderDispatcherOverlay.stopChunkUpdates();
    }

    @Override
    public void createBindEntityOutlineFbs(final int p_72720_1_, final int p_72720_2_)
    {
        //Not needed
    }

    @Override
    public void renderEntities(@NotNull final Entity renderViewEntity, @NotNull final ICamera camera, final float partialTicks)
    {
        final int entityPass = 0;

        this.profiler.startSection("prepare");
        TileEntityRendererDispatcher.instance.func_190056_a(this.world, this.mc.getTextureManager(), this.mc.fontRendererObj, renderViewEntity, this.mc.objectMouseOver, partialTicks);
        this.renderManager.cacheActiveRenderInfo(this.world, this.mc.fontRendererObj, renderViewEntity, this.mc.pointedEntity, this.mc.gameSettings, partialTicks);

        this.countEntitiesTotal = 0;
        this.countEntitiesRendered = 0;

        final double x = playerPositionOffset.xCoord;
        final double y = playerPositionOffset.yCoord;
        final double z = playerPositionOffset.zCoord;

        setStaticPlayerPos(x, y, z);

        TileEntityRendererDispatcher.instance.entityX = x;
        TileEntityRendererDispatcher.instance.entityY = y;
        TileEntityRendererDispatcher.instance.entityZ = z;

        this.renderManager.setRenderPosition(x, y, z);
        this.mc.entityRenderer.enableLightmap();

        this.profiler.endStartSection("blockentities");
        RenderHelper.enableStandardItemLighting();

        for (@NotNull final ContainerLocalRenderInformation renderInfo : this.renderInfos)
        {
            for (@NotNull final TileEntity tileEntity : renderInfo.renderChunk.getCompiledChunk().getTileEntities())
            {
                final AxisAlignedBB renderBB = tileEntity.getRenderBoundingBox();

                if (!tileEntity.shouldRenderInPass(entityPass) || !camera.isBoundingBoxInFrustum(renderBB))
                {
                    continue;
                }

                if (!this.mc.theWorld.isAirBlock(tileEntity.getPos().add(this.world.position)))
                {
                    continue;
                }

                TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, partialTicks, -1);
            }
        }

        this.mc.entityRenderer.disableLightmap();
        this.profiler.endSection();
    }

    @Override
    public String getDebugInfoRenders()
    {
        final int total = this.viewFrustum.renderChunks.length;
        int rendered = 0;

        for (@NotNull final ContainerLocalRenderInformation renderInfo : this.renderInfos)
        {
            final CompiledChunk compiledChunk = renderInfo.renderChunk.compiledChunk;

            if (compiledChunk != CompiledChunk.DUMMY && !compiledChunk.isEmpty())
            {
                rendered++;
            }
        }

        return String.format("C: %d/%d %sD: %d, %s", rendered, total, this.mc.renderChunksMany ? "(s) " : "", this.renderDistanceChunks, this.renderDispatcher.getDebugInfo());
    }

    @Override
    public String getDebugInfoEntities()
    {
        return String.format("E: %d/%d", this.countEntitiesRendered, this.countEntitiesTotal);
    }

    @Override
    public void setupTerrain(final Entity viewEntity, final double partialTicks, @NotNull final ICamera camera, final int frameCount, final boolean playerSpectator)
    {
        if (ConfigurationHandler.renderDistance != this.renderDistanceChunks || this.vboEnabled != OpenGlHelper.useVbo())
        {
            loadRenderers();
        }

        this.profiler.startSection("camera");
        final double posX = playerPositionOffset.xCoord;
        final double posY = playerPositionOffset.yCoord;
        final double posZ = playerPositionOffset.zCoord;

        final double deltaX = posX - this.frustumUpdatePosX;
        final double deltaY = posY - this.frustumUpdatePosY;
        final double deltaZ = posZ - this.frustumUpdatePosZ;

        final int chunkCoordX = MathHelper.floor_double(posX) >> 4;
        final int chunkCoordY = MathHelper.floor_double(posY) >> 4;
        final int chunkCoordZ = MathHelper.floor_double(posZ) >> 4;

        if (this.frustumUpdatePosChunkX != chunkCoordX || this.frustumUpdatePosChunkY != chunkCoordY ||
              this.frustumUpdatePosChunkZ != chunkCoordZ || deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 16.0)
        {
            this.frustumUpdatePosX = posX;
            this.frustumUpdatePosY = posY;
            this.frustumUpdatePosZ = posZ;
            this.frustumUpdatePosChunkX = chunkCoordX;
            this.frustumUpdatePosChunkY = chunkCoordY;
            this.frustumUpdatePosChunkZ = chunkCoordZ;
            this.viewFrustum.updateChunkPositions(posX, posZ);
        }

        this.profiler.endStartSection("renderlistcamera");
        this.renderContainer.initialize(posX, posY, posZ);

        this.profiler.endStartSection("culling");
        @NotNull final BlockPos posEye = new BlockPos(posX, posY + viewEntity.getEyeHeight(), posZ);
        final RenderChunk renderchunk = this.viewFrustum.getRenderChunk(posEye);
        final RenderOverlay renderoverlay = this.viewFrustum.getRenderOverlay(posEye);

        this.displayListEntitiesDirty = isDisplayListDirty(viewEntity, posX, posY, posZ);
        this.lastViewEntityX = posX;
        this.lastViewEntityY = posY;
        this.lastViewEntityZ = posZ;
        this.lastViewEntityPitch = viewEntity.rotationPitch;
        this.lastViewEntityYaw = viewEntity.rotationYaw;

        if (this.displayListEntitiesDirty)
        {
            this.displayListEntitiesDirty = false;
            this.renderInfos = Lists.newArrayListWithCapacity(CHUNKS);

            @NotNull final LinkedList<ContainerLocalRenderInformation> renderInfoList = Lists.newLinkedList();
            boolean renderChunksMany = this.mc.renderChunksMany;

            if (renderchunk == null)
            {
                final int chunkY = posEye.getY() > 0 ? 248 : 8;

                for (int chunkX = -this.renderDistanceChunks; chunkX <= this.renderDistanceChunks; chunkX++)
                {
                    for (int chunkZ = -this.renderDistanceChunks; chunkZ <= this.renderDistanceChunks; chunkZ++)
                    {
                        @NotNull final BlockPos pos = new BlockPos((chunkX << 4) + 8, chunkY, (chunkZ << 4) + 8);
                        final RenderChunk renderChunk = this.viewFrustum.getRenderChunk(pos);
                        final RenderOverlay renderOverlay = this.viewFrustum.getRenderOverlay(pos);

                        if (renderChunk != null && camera.isBoundingBoxInFrustum(renderChunk.boundingBox))
                        {
                            renderChunk.setFrameIndex(frameCount);
                            renderOverlay.setFrameIndex(frameCount);
                            renderInfoList.add(new ContainerLocalRenderInformation(renderChunk, renderOverlay, null, 0));
                        }
                    }
                }
            }
            else
            {
                boolean add = false;
                @NotNull final ContainerLocalRenderInformation renderInfo = new ContainerLocalRenderInformation(renderchunk, renderoverlay, null, 0);
                final Set<EnumFacing> visibleSides = getVisibleSides(posEye);

                if (!visibleSides.isEmpty() && visibleSides.size() == 1)
                {
                    final Vector3f viewVector = getViewVector(viewEntity, partialTicks);
                    final EnumFacing facing = EnumFacing.getFacingFromVector(viewVector.x, viewVector.y, viewVector.z).getOpposite();
                    visibleSides.remove(facing);
                }

                if (visibleSides.isEmpty())
                {
                    add = true;
                }

                if (add && !playerSpectator)
                {
                    this.renderInfos.add(renderInfo);
                }
                else
                {
                    if (playerSpectator && this.world.getBlockState(posEye).isOpaqueCube())
                    {
                        renderChunksMany = false;
                    }

                    renderchunk.setFrameIndex(frameCount);
                    renderoverlay.setFrameIndex(frameCount);
                    renderInfoList.add(renderInfo);
                }
            }

            while (!renderInfoList.isEmpty())
            {
                final ContainerLocalRenderInformation renderInfo = renderInfoList.poll();
                final RenderChunk renderChunk = renderInfo.renderChunk;
                final EnumFacing facing = renderInfo.facing;
                final BlockPos posChunk = renderChunk.getPosition();
                this.renderInfos.add(renderInfo);

                for (@NotNull final EnumFacing side : EnumFacing.VALUES)
                {
                    @Nullable final RenderChunk neighborRenderChunk = getNeighborRenderChunk(posEye, posChunk, side);
                    @Nullable final RenderOverlay neighborRenderOverlay = getNeighborRenderOverlay(posEye, posChunk, side);

                    if ((!renderChunksMany || !renderInfo.setFacing.contains(side.getOpposite())) && (!renderChunksMany || facing == null ||
                                                                                                        renderChunk.getCompiledChunk().isVisible(facing.getOpposite(), side))
                          && neighborRenderChunk != null &&
                          neighborRenderChunk.setFrameIndex(frameCount) && camera.isBoundingBoxInFrustum(neighborRenderChunk.boundingBox))
                    {
                        @Nullable final ContainerLocalRenderInformation renderInfoNext =
                          new ContainerLocalRenderInformation(neighborRenderChunk, neighborRenderOverlay, side, renderInfo.counter + 1);

                        renderInfoNext.setFacing.addAll(renderInfo.setFacing);
                        renderInfoNext.setFacing.add(side);
                        renderInfoList.add(renderInfoNext);
                    }
                }
            }
        }

        this.renderDispatcher.clearChunkUpdates();
        this.renderDispatcherOverlay.clearChunkUpdates();
        @NotNull final Set<RenderChunk> set = this.chunksToUpdate;
        @NotNull final Set<RenderOverlay> set1 = this.overlaysToUpdate;
        this.chunksToUpdate = Sets.newLinkedHashSet();

        for (@NotNull final ContainerLocalRenderInformation renderInfo : this.renderInfos)
        {
            final RenderChunk renderChunk = renderInfo.renderChunk;
            final RenderOverlay renderOverlay = renderInfo.renderOverlay;

            if (renderChunk.isNeedsUpdate() || set.contains(renderChunk))
            {
                this.displayListEntitiesDirty = true;

                this.chunksToUpdate.add(renderChunk);
            }

            if (renderOverlay.isNeedsUpdate() || set1.contains(renderOverlay))
            {
                this.displayListEntitiesDirty = true;

                this.overlaysToUpdate.add(renderOverlay);
            }
        }

        this.chunksToUpdate.addAll(set);
        this.overlaysToUpdate.addAll(set1);
        this.profiler.endSection();
    }

    @Override
    public int renderBlockLayer(@NotNull final BlockRenderLayer layer, final double partialTicks, final int pass, final Entity entity)
    {
        RenderHelper.disableStandardItemLighting();

        if (layer == BlockRenderLayer.TRANSLUCENT)
        {
            this.profiler.startSection("translucent_sort");
            final double posX = playerPositionOffset.xCoord;
            final double posY = playerPositionOffset.yCoord;
            final double posZ = playerPositionOffset.zCoord;

            final double deltaX = posX - this.prevRenderSortX;
            final double deltaY = posY - this.prevRenderSortY;
            final double deltaZ = posZ - this.prevRenderSortZ;

            if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 1.0)
            {
                this.prevRenderSortX = posX;
                this.prevRenderSortY = posY;
                this.prevRenderSortZ = posZ;
                int count = 0;

                for (@NotNull final ContainerLocalRenderInformation renderInfo : this.renderInfos)
                {
                    if (renderInfo.renderChunk.compiledChunk.isLayerStarted(layer) && count < 15)
                    {
                        this.renderDispatcher.updateTransparencyLater(renderInfo.renderChunk);
                        this.renderDispatcherOverlay.updateTransparencyLater(renderInfo.renderOverlay);
                    }
                    count++;
                }
            }

            this.profiler.endSection();
        }

        this.profiler.startSection("filterempty");
        int count = 0;
        final boolean isTranslucent = layer == BlockRenderLayer.TRANSLUCENT;
        final int start = isTranslucent ? (this.renderInfos.size() - 1) : 0;
        final int end = isTranslucent ? -1 : this.renderInfos.size();
        final int step = isTranslucent ? -1 : 1;

        for (int index = start; index != end; index += step)
        {
            final ContainerLocalRenderInformation renderInfo = this.renderInfos.get(index);
            final RenderChunk renderChunk = renderInfo.renderChunk;
            final RenderOverlay renderOverlay = renderInfo.renderOverlay;

            if (!renderChunk.getCompiledChunk().isLayerEmpty(layer))
            {
                count++;
                this.renderContainer.addRenderChunk(renderChunk, layer);
            }

            if (isTranslucent && renderOverlay != null && !renderOverlay.getCompiledChunk().isLayerEmpty(layer))
            {
                count++;
                this.renderContainer.addRenderOverlay(renderOverlay);
            }
        }

        this.profiler.endStartSection("render_" + layer);
        renderBlockLayer(layer);
        this.profiler.endSection();

        return count;
    }

    @Override
    public void updateClouds()
    {
        //Not Needed
    }

    @Override
    public void renderSky(final float partialTicks, final int pass)
    {
        //Not Needed
    }

    @Override
    public void renderClouds(final float partialTicks, final int pass)
    {
        //Not Needed
    }

    @Override
    public boolean hasCloudFog(final double x, final double y, final double z, final float partialTicks)
    {
        return false;
    }

    @Override
    public void updateChunks(final long finishTimeNano)
    {
        this.displayListEntitiesDirty |= this.renderDispatcher.runChunkUploads(finishTimeNano);

        @NotNull final Iterator<RenderChunk> chunkIterator = this.chunksToUpdate.iterator();
        while (chunkIterator.hasNext())
        {
            final RenderChunk renderChunk = chunkIterator.next();
            if (!this.renderDispatcher.updateChunkLater(renderChunk))
            {
                break;
            }

            renderChunk.setNeedsUpdate(false);
            chunkIterator.remove();
        }

        this.displayListEntitiesDirty |= this.renderDispatcherOverlay.runChunkUploads(finishTimeNano);

        @NotNull final Iterator<RenderOverlay> overlayIterator = this.overlaysToUpdate.iterator();
        while (overlayIterator.hasNext())
        {
            final RenderOverlay renderOverlay = overlayIterator.next();
            if (!this.renderDispatcherOverlay.updateChunkLater(renderOverlay))
            {
                break;
            }

            renderOverlay.setNeedsUpdate(false);
            overlayIterator.remove();
        }
    }

    @Override
    public void renderWorldBorder(final Entity entity, final float partialTicks)
    {
        // Not needed
    }

    @Override
    public void drawBlockDamageTexture(final Tessellator tessellator, final VertexBuffer VertexBuffer, final Entity entity, final float partialTicks)
    {
        // Not needed
    }

    @Override
    public void drawSelectionBox(final EntityPlayer player, final RayTraceResult movingObjectPosition, final int p_72731_3_, final float partialTicks)
    {
        // Not needed
    }

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags)
    {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1, (flags & 8) != 0);
    }

    @Override
    public void notifyLightSet(final BlockPos pos)
    {
    	//TODO CHECK
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1, true);
    }

    @Override
    public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2)
    {
        markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1, false);
    }

    @Override
    public void playRecord(@Nullable SoundEvent soundIn, BlockPos pos)
    {
        // Not needed
    }

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch)
    {
        // Not needed
    }

    @Override
    public void spawnParticle(
                               final int p_180442_1_, final boolean p_180442_2_, final double p_180442_3_, final double p_180442_5_, final double p_180442_7_,
                               final double p_180442_9_, final double p_180442_11_, final double p_180442_13_, final int... p_180442_15_)
    {
        // Not needed
    }

    @Override
    public void onEntityAdded(final Entity entityIn)
    {
        // Not needed
    }

    @Override
    public void onEntityRemoved(final Entity entityIn)
    {
        // Not needed
    }

    @Override
    public void deleteAllDisplayLists()
    {
        // Not needed
    }

    @Override
    public void broadcastSound(final int p_180440_1_, final BlockPos pos, final int p_180440_3_)
    {
        // Not needed
    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data)
    {
        // Not needed
    }

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress)
    {
        // Not needed
    }

    @Override
    public void setDisplayListEntitiesDirty()
    {
        this.displayListEntitiesDirty = true;
    }

    private void markBlocksForUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2, boolean flag)
    {
        if (this.world == null)
        {
            return;
        }

        @NotNull final BlockPos.MutableBlockPos position = this.world.position;
        this.viewFrustum.markBlocksForUpdate(x1 - position.getX(), y1 - position.getY(), z1 - position.getZ(), x2 - position.getX(), y2 - position.getY(), z2 - position.getZ(), flag);
    }

    /**
     * Render the schematic and colored overlay.
     *
     * @param event Forge event.
     */
    @SubscribeEvent
    public void onRenderWorldLast(@NotNull final RenderWorldLastEvent event)
    {
        final EntityPlayerSP player = this.mc.thePlayer;
        if (player != null)
        {
            this.profiler.startSection("schematica");
            final boolean isRenderingSchematic = Settings.instance.getActiveSchematic() != null && Settings.instance.getSchematicWorld().isRendering;

            this.profiler.startSection("schematic");
            if (isRenderingSchematic)
            {
                GlStateManager.pushMatrix();
                renderSchematic(Settings.instance.getSchematicWorld(), event.getPartialTicks());
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                renderOverlay(Settings.instance.getSchematicWorld());
                GlStateManager.popMatrix();
            }

            this.profiler.endSection();
            this.profiler.endSection();
        }
    }

    private synchronized void renderSchematic(final SchematicWorld schematic, final float partialTicks)
    {
        if (this.world != schematic)
        {
            this.world = schematic;

            loadRenderers();
        }
        playerPositionOffset = this.mc.thePlayer.getPositionVector().subtract(this.world.position.getX(), this.world.position.getY(), this.world.position.getZ());

        if (OpenGlHelper.shadersSupported && ConfigurationHandler.enableAlpha)
        {
            GL20.glUseProgram(SHADER_ALPHA.getProgram());
            GL20.glUniform1f(GL20.glGetUniformLocation(SHADER_ALPHA.getProgram(), "alpha_multiplier"), ConfigurationHandler.alpha);
        }

        final int fps = Math.max(Minecraft.getDebugFPS(), 30);
        renderWorld(partialTicks, System.nanoTime() + 1000000000 / fps);

        if (OpenGlHelper.shadersSupported && ConfigurationHandler.enableAlpha)
        {
            GL20.glUseProgram(0);
        }
    }

    private void renderOverlay(@NotNull final SchematicWorld schematic)
    {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        @Nullable final GeometryTessellator tessellator = GeometryTessellator.getInstance();
        tessellator.setTranslation(-this.mc.thePlayer.posX, -this.mc.thePlayer.posY, -this.mc.thePlayer.posZ);
        tessellator.setDelta(ConfigurationHandler.blockDelta);

        tessellator.beginLines();
        this.tmp.setPos(schematic.position.getX() + schematic.getWidth() - 1,
          schematic.position.getY() + schematic.getHeight() - 1,
          schematic.position.getZ() + schematic.getLength() - 1);
        tessellator.drawCuboid(schematic.position, this.tmp, GeometryMasks.Line.ALL, 0x7FBF00BF);
        tessellator.draw();

        GlStateManager.depthMask(false);
        this.renderContainer.renderOverlay();
        GlStateManager.depthMask(true);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private void renderWorld(final float partialTicks, final long finishTimeNano)
    {
        GlStateManager.enableCull();
        this.profiler.endStartSection("culling");
        @NotNull final Frustum frustum = new Frustum();
        final Entity entity = this.mc.getRenderViewEntity();

        final double x = playerPositionOffset.xCoord;
        final double y = playerPositionOffset.yCoord;
        final double z = playerPositionOffset.zCoord;
        frustum.setPosition(x, y, z);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        this.profiler.endStartSection("prepareterrain");
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();

        this.profiler.endStartSection("terrain_setup");
        setupTerrain(entity, partialTicks, frustum, this.frameCount, isInsideWorld(x, y, z));
        this.frameCount++;

        this.profiler.endStartSection("updatechunks");
        updateChunks(finishTimeNano / 2);

        this.profiler.endStartSection("terrain");
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        renderBlockLayer(BlockRenderLayer.SOLID, partialTicks, PASS, entity);
        renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, partialTicks, PASS, entity);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        renderBlockLayer(BlockRenderLayer.CUTOUT, partialTicks, PASS, entity);
        this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        this.profiler.endStartSection("entities");
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        renderEntities(entity, frustum, partialTicks);
        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();
        disableLightmap();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        GlStateManager.enableCull();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();
        this.profiler.endStartSection("translucent");
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        renderBlockLayer(BlockRenderLayer.TRANSLUCENT, partialTicks, PASS, entity);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();
    }

    private boolean isInsideWorld(final double x, final double y, final double z)
    {
        return x >= -1 && y >= -1 && z >= -1 && x <= this.world.getWidth() && y <= this.world.getHeight() && z <= this.world.getLength();
    }

    private static void disableLightmap()
    {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private boolean isDisplayListDirty(@NotNull Entity viewEntity, double posX, double posY, double posZ)
    {
        return this.displayListEntitiesDirty ||
                 !this.chunksToUpdate.isEmpty() ||
                 didEntityMove(posX, posY, posZ) ||
                 didEntityRotate(viewEntity);
    }

    private Set<EnumFacing> getVisibleSides(@NotNull final BlockPos pos)
    {
        @NotNull final VisGraph visgraph = new VisGraph();
        @NotNull final BlockPos posChunk = new BlockPos(pos.getX() & ~0xF, pos.getY() & ~0xF, pos.getZ() & ~0xF);

        for (@NotNull final BlockPos.MutableBlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(posChunk, posChunk.add(15, 15, 15)))
        {
            if (this.world.getBlockState(mutableBlockPos).isOpaqueCube())
            {
                visgraph.setOpaqueCube(mutableBlockPos);
            }
        }

        return visgraph.getVisibleFacings(pos);
    }

    private RenderChunk getNeighborRenderChunk(@NotNull final BlockPos posEye, @NotNull final BlockPos posChunk, final EnumFacing side)
    {
        final BlockPos offset = posChunk.offset(side, 16);
        if (MathHelper.abs_int(posEye.getX() - offset.getX()) > this.renderDistanceChunks * 16)
        {
            return null;
        }

        if (offset.getY() < 0 || offset.getY() >= 256)
        {
            return null;
        }

        if (MathHelper.abs_int(posEye.getZ() - offset.getZ()) > this.renderDistanceChunks * 16)
        {
            return null;
        }

        return this.viewFrustum.getRenderChunk(offset);
    }

    private RenderOverlay getNeighborRenderOverlay(@NotNull final BlockPos posEye, @NotNull final BlockPos posChunk, final EnumFacing side)
    {
        final BlockPos offset = posChunk.offset(side, 16);
        if (MathHelper.abs_int(posEye.getX() - offset.getX()) > this.renderDistanceChunks * 16)
        {
            return null;
        }

        if (offset.getY() < 0 || offset.getY() >= 256)
        {
            return null;
        }

        if (MathHelper.abs_int(posEye.getZ() - offset.getZ()) > this.renderDistanceChunks * 16)
        {
            return null;
        }

        return this.viewFrustum.getRenderOverlay(offset);
    }

    private void renderBlockLayer(final BlockRenderLayer layer)
    {
        this.mc.entityRenderer.enableLightmap();

        this.renderContainer.renderChunkLayer(layer);

        this.mc.entityRenderer.disableLightmap();
    }

    private static void setStaticPlayerPos(double x, double y, double z)
    {
        TileEntityRendererDispatcher.staticPlayerX = x;
        TileEntityRendererDispatcher.staticPlayerY = y;
        TileEntityRendererDispatcher.staticPlayerZ = z;
    }

    private boolean didEntityMove(double posX, double posY, double posZ)
    {
        return doubleEquals(posX, this.lastViewEntityX) ||
                 doubleEquals(posY, this.lastViewEntityY) ||
                 doubleEquals(posZ, this.lastViewEntityZ);
    }

    private boolean didEntityRotate(@NotNull Entity viewEntity)
    {
        return doubleEquals(viewEntity.rotationPitch, this.lastViewEntityPitch) ||
                 doubleEquals(viewEntity.rotationYaw, this.lastViewEntityYaw);
    }

    private static boolean doubleEquals(double d1, double d2)
    {
        // Calculate the difference.
        double diff = Math.abs(d1 - d2);
        double nd1 = Math.abs(d1);
        double nd2 = Math.abs(d2);
        // Find the largest
        double largest = (nd2 > nd1) ? nd2 : nd1;

        return diff <= largest * DOUBLE_EPSILON;
    }

    public void refresh()
    {
        loadRenderers();
    }

    @SideOnly(Side.CLIENT)
    private class ContainerLocalRenderInformation
    {
        private final RenderChunk     renderChunk;
        private final RenderOverlay   renderOverlay;
        private final EnumFacing      facing;
        @NotNull
        private final Set<EnumFacing> setFacing;
        private final int             counter;

        ContainerLocalRenderInformation(final RenderChunk renderChunk, final RenderOverlay renderOverlay, final EnumFacing facing, final int counter)
        {
            this.setFacing = EnumSet.noneOf(EnumFacing.class);
            this.renderChunk = renderChunk;
            this.renderOverlay = renderOverlay;
            this.facing = facing;
            this.counter = counter;
        }
    }
}
