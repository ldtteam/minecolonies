package com.schematica.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.schematica.Settings;
import com.schematica.client.renderer.chunk.OverlayRenderDispatcher;
import com.schematica.client.renderer.chunk.container.SchematicChunkRenderContainer;
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
import com.schematica.core.util.vector.Vector3d;
import com.schematica.handler.ConfigurationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
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
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;

@SideOnly(Side.CLIENT)
public class RenderSchematic extends RenderGlobal {
    public static final RenderSchematic INSTANCE = new RenderSchematic(Minecraft.getMinecraft());

    private static final int RENDER_DISTANCE = 32;
    private static final int CHUNKS_XZ = (RENDER_DISTANCE + 1) * 2;
    private static final int CHUNKS_Y = 16;
    private static final int CHUNKS = CHUNKS_XZ * CHUNKS_XZ * CHUNKS_Y;
    private static final int PASS = 2;

    private static final ShaderProgram SHADER_ALPHA = new ShaderProgram("minecolonies", null, "shaders/alpha.frag");
    private static final Vector3d PLAYER_POSITION_OFFSET = new Vector3d();
    private final Minecraft mc;
    private final Profiler profiler;
    private final RenderManager renderManager;
    private SchematicWorld world;
    private Set<RenderChunk> chunksToUpdate = Sets.newLinkedHashSet();
    private Set<RenderOverlay> overlaysToUpdate = Sets.newLinkedHashSet();
    private List<ContainerLocalRenderInformation> renderInfos = Lists.newArrayListWithCapacity(CHUNKS);
    private ViewFrustumOverlay viewFrustum;
    private double frustumUpdatePosX = Double.MIN_VALUE;
    private double frustumUpdatePosY = Double.MIN_VALUE;
    private double frustumUpdatePosZ = Double.MIN_VALUE;
    private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
    private double lastViewEntityX = Double.MIN_VALUE;
    private double lastViewEntityY = Double.MIN_VALUE;
    private double lastViewEntityZ = Double.MIN_VALUE;
    private double lastViewEntityPitch = Double.MIN_VALUE;
    private double lastViewEntityYaw = Double.MIN_VALUE;
    private final ChunkRenderDispatcher renderDispatcher = new ChunkRenderDispatcher();
    private final OverlayRenderDispatcher renderDispatcherOverlay = new OverlayRenderDispatcher();
    private SchematicChunkRenderContainer renderContainer;
    private int renderDistanceChunks = -1;
    private int countEntitiesTotal;
    private int countEntitiesRendered;
    private boolean vboEnabled = false;
    private ISchematicRenderChunkFactory renderChunkFactory;
    private double prevRenderSortX;
    private double prevRenderSortY;
    private double prevRenderSortZ;
    private boolean displayListEntitiesDirty = true;
    private int frameCount = 0;
    private BlockPos.MutableBlockPos tmp = new BlockPos.MutableBlockPos();

    private RenderSchematic(final Minecraft minecraft) {
        super(minecraft);
        this.mc = minecraft;
        this.profiler = minecraft.mcProfiler;
        this.renderManager = minecraft.getRenderManager();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.bindTexture(0);
        this.vboEnabled = OpenGlHelper.useVbo();

        if (this.vboEnabled) {
            initVbo();
        } else {
            initList();
        }
    }

    private void initVbo() {
        this.renderContainer = new SchematicChunkRenderContainerVbo();
        this.renderChunkFactory = new ISchematicRenderChunkFactory() {
            @Override
            public RenderChunk makeRenderChunk(final World world, final RenderGlobal renderGlobal, final BlockPos pos, final int index) {
                return new SchematicRenderChunkVbo(world, renderGlobal, pos, index);
            }

            @Override
            public RenderOverlay makeRenderOverlay(final World world, final RenderGlobal renderGlobal, final BlockPos pos, final int index) {
                return new RenderOverlay(world, renderGlobal, pos, index);
            }
        };
    }

    private void initList() {
        this.renderContainer = new SchematicChunkRenderContainerList();
        this.renderChunkFactory = new ISchematicRenderChunkFactory() {
            @Override
            public RenderChunk makeRenderChunk(final World world, final RenderGlobal renderGlobal, final BlockPos pos, final int index) {
                return new SchematicRenderChunkList(world, renderGlobal, pos, index);
            }

            @Override
            public RenderOverlay makeRenderOverlay(final World world, final RenderGlobal renderGlobal, final BlockPos pos, final int index) {
                return new RenderOverlayList(world, renderGlobal, pos, index);
            }
        };
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {}

    @Override
    public void makeEntityOutlineShader() {}

    @Override
    public void renderEntityOutlineFramebuffer() {}

    @Override
    protected boolean isRenderEntityOutlines() {
        return false;
    }

    @Override
    public void setWorldAndLoadRenderers(final WorldClient worldClient) {
        if (worldClient instanceof SchematicWorld) {
            setWorldAndLoadRenderers((SchematicWorld) worldClient);
        } else {
            setWorldAndLoadRenderers(null);
        }
    }
    
    public void setWorldAndLoadRenderers(final SchematicWorld world) {
        if (this.world != null) {
            this.world.removeWorldAccess(this);
        }

        this.frustumUpdatePosX = Double.MIN_VALUE;
        this.frustumUpdatePosY = Double.MIN_VALUE;
        this.frustumUpdatePosZ = Double.MIN_VALUE;
        this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        this.renderManager.set(world);
        this.world = world;

        if (world != null) {
            world.addWorldAccess(this);
            loadRenderers();
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(final RenderWorldLastEvent event) {
        final EntityPlayerSP player = this.mc.thePlayer;
        if (player != null) {
            this.profiler.startSection("schematica");
            final boolean isRenderingSchematic = Settings.instance.schematic != null && Settings.instance.schematic.isRendering;

            this.profiler.startSection("schematic");
            if (isRenderingSchematic) {
                GlStateManager.pushMatrix();
                renderSchematic(Settings.instance.schematic, event.partialTicks);
                GlStateManager.popMatrix();
            }

            this.profiler.endStartSection("guide");
            if (Settings.instance.isRenderingGuide || isRenderingSchematic) {
                GlStateManager.pushMatrix();
                renderOverlay(Settings.instance.schematic, isRenderingSchematic);
                GlStateManager.popMatrix();
            }

            this.profiler.endSection();
            this.profiler.endSection();
        }
    }

    private void renderSchematic(final SchematicWorld schematic, final float partialTicks) {
        if (this.world != schematic) {
            this.world = schematic;

            loadRenderers();
        }

        PLAYER_POSITION_OFFSET.set(this.mc.thePlayer.posX, this.mc.thePlayer.posY, this.mc.thePlayer.posZ)
                              .sub(this.world.position.getX(), this.world.position.getY(), this.world.position.getZ());

        if (OpenGlHelper.shadersSupported && ConfigurationHandler.enableAlpha) {
            GL20.glUseProgram(SHADER_ALPHA.getProgram());
            GL20.glUniform1f(GL20.glGetUniformLocation(SHADER_ALPHA.getProgram(), "alpha_multiplier"), ConfigurationHandler.alpha);
        }

        final int fps = Math.max(Minecraft.getDebugFPS(), 30);
        renderWorld(partialTicks, System.nanoTime() + 1000000000 / fps);

        if (OpenGlHelper.shadersSupported && ConfigurationHandler.enableAlpha) {
            GL20.glUseProgram(0);
        }
    }

    private void renderOverlay(final SchematicWorld schematic, final boolean isRenderingSchematic) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        final GeometryTessellator tessellator = GeometryTessellator.getInstance();
        tessellator.setTranslation(-this.mc.thePlayer.posX, -this.mc.thePlayer.posY, -this.mc.thePlayer.posZ);
        tessellator.setDelta(ConfigurationHandler.blockDelta);

        if (Settings.instance.isRenderingGuide) {
            tessellator.beginQuads();
            tessellator.drawCuboid(Settings.instance.pointA, GeometryMasks.Quad.ALL, 0x3FBF0000);
            tessellator.drawCuboid(Settings.instance.pointB, GeometryMasks.Quad.ALL, 0x3F0000BF);
            tessellator.draw();
        }

        tessellator.beginLines();
        if (Settings.instance.isRenderingGuide) {
            tessellator.drawCuboid(Settings.instance.pointA, GeometryMasks.Line.ALL, 0x3FBF0000);
            tessellator.drawCuboid(Settings.instance.pointB, GeometryMasks.Line.ALL, 0x3F0000BF);
            tessellator.drawCuboid(Settings.instance.pointMin, Settings.instance.pointMax, GeometryMasks.Line.ALL, 0x7F00BF00);
        }
        if (isRenderingSchematic) {
            this.tmp.set(schematic.position.getX() + schematic.getWidth() - 1, schematic.position.getY() + schematic.getHeight() - 1, schematic.position.getZ() + schematic.getLength() - 1);
            tessellator.drawCuboid(schematic.position, this.tmp, GeometryMasks.Line.ALL, 0x7FBF00BF);
        }
        tessellator.draw();

        GlStateManager.depthMask(false);
        this.renderContainer.renderOverlay();
        GlStateManager.depthMask(true);

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private void renderWorld(final float partialTicks, final long finishTimeNano) {
        GlStateManager.enableCull();
        this.profiler.endStartSection("culling");
        final Frustum frustum = new Frustum();
        final Entity entity = this.mc.getRenderViewEntity();

        final double x = PLAYER_POSITION_OFFSET.x;
        final double y = PLAYER_POSITION_OFFSET.y;
        final double z = PLAYER_POSITION_OFFSET.z;
        frustum.setPosition(x, y, z);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        this.profiler.endStartSection("prepareterrain");
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        RenderHelper.disableStandardItemLighting();

        this.profiler.endStartSection("terrain_setup");
        setupTerrain(entity, partialTicks, frustum, this.frameCount++, isInsideWorld(x, y, z));

        this.profiler.endStartSection("updatechunks");
        updateChunks(finishTimeNano / 2);

        this.profiler.endStartSection("terrain");
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        renderBlockLayer(EnumWorldBlockLayer.SOLID, partialTicks, PASS, entity);
        renderBlockLayer(EnumWorldBlockLayer.CUTOUT_MIPPED, partialTicks, PASS, entity);
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        renderBlockLayer(EnumWorldBlockLayer.CUTOUT, partialTicks, PASS, entity);
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
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
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();
        this.profiler.endStartSection("translucent");
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        renderBlockLayer(EnumWorldBlockLayer.TRANSLUCENT, partialTicks, PASS, entity);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();
    }

    private boolean isInsideWorld(final double x, final double y, final double z) {
        return x >= -1 && y >= -1 && z >= -1 && x <= this.world.getWidth() && y <= this.world.getHeight() && z <= this.world.getLength();
    }

    private void disableLightmap() {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public void refresh() {
        loadRenderers();
    }

    @Override
    public void loadRenderers() {
        if (this.world != null) {
            this.displayListEntitiesDirty = true;
            this.renderDistanceChunks = ConfigurationHandler.renderDistance;
            final boolean vbo = this.vboEnabled;
            this.vboEnabled = OpenGlHelper.useVbo();

            if (vbo && !this.vboEnabled) {
                initList();
            } else if (!vbo && this.vboEnabled) {
                initVbo();
            }

            if (this.viewFrustum != null) {
                this.viewFrustum.deleteGlResources();
            }

            stopChunkUpdates();
            this.viewFrustum = new ViewFrustumOverlay(this.world, this.renderDistanceChunks, this, this.renderChunkFactory);

            final double posX = PLAYER_POSITION_OFFSET.x;
            final double posZ = PLAYER_POSITION_OFFSET.z;
            this.viewFrustum.updateChunkPositions(posX, posZ);
        }
    }

    @Override
    protected void stopChunkUpdates() {
        this.chunksToUpdate.clear();
        this.overlaysToUpdate.clear();
        this.renderDispatcher.stopChunkUpdates();
        this.renderDispatcherOverlay.stopChunkUpdates();
    }

    @Override
    public void createBindEntityOutlineFbs(final int p_72720_1_, final int p_72720_2_) {}

    @Override
    public void renderEntities(final Entity renderViewEntity, final ICamera camera, final float partialTicks) {
        final int entityPass = 0;

        this.profiler.startSection("prepare");
        TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(this.world, this.mc.getTextureManager(), this.mc.fontRendererObj, renderViewEntity, partialTicks);
        this.renderManager.cacheActiveRenderInfo(this.world, this.mc.fontRendererObj, renderViewEntity, this.mc.pointedEntity, this.mc.gameSettings, partialTicks);

        this.countEntitiesTotal = 0;
        this.countEntitiesRendered = 0;

        final double x = PLAYER_POSITION_OFFSET.x;
        final double y = PLAYER_POSITION_OFFSET.y;
        final double z = PLAYER_POSITION_OFFSET.z;

        TileEntityRendererDispatcher.staticPlayerX = x;
        TileEntityRendererDispatcher.staticPlayerY = y;
        TileEntityRendererDispatcher.staticPlayerZ = z;

        TileEntityRendererDispatcher.instance.entityX = x;
        TileEntityRendererDispatcher.instance.entityY = y;
        TileEntityRendererDispatcher.instance.entityZ = z;

        this.renderManager.setRenderPosition(x, y, z);
        this.mc.entityRenderer.enableLightmap();

        this.profiler.endStartSection("blockentities");
        RenderHelper.enableStandardItemLighting();

        for (final ContainerLocalRenderInformation renderInfo : this.renderInfos) {
            for (final TileEntity tileEntity : renderInfo.renderChunk.getCompiledChunk().getTileEntities()) {
                final AxisAlignedBB renderBB = tileEntity.getRenderBoundingBox();

                if (!tileEntity.shouldRenderInPass(entityPass) || !camera.isBoundingBoxInFrustum(renderBB)) {
                    continue;
                }

                if (!this.mc.theWorld.isAirBlock(tileEntity.getPos().add(this.world.position))) {
                    continue;
                }

                TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, partialTicks, -1);
            }
        }

        this.mc.entityRenderer.disableLightmap();
        this.profiler.endSection();
    }

    @Override
    public String getDebugInfoRenders() {
        final int total = this.viewFrustum.renderChunks.length;
        int rendered = 0;

        for (final ContainerLocalRenderInformation renderInfo : this.renderInfos) {
            final CompiledChunk compiledChunk = renderInfo.renderChunk.compiledChunk;

            if (compiledChunk != CompiledChunk.DUMMY && !compiledChunk.isEmpty()) {
                rendered++;
            }
        }

        return String.format("C: %d/%d %sD: %d, %s", rendered, total, this.mc.renderChunksMany ? "(s) " : "", this.renderDistanceChunks, this.renderDispatcher.getDebugInfo());
    }

    @Override
    public String getDebugInfoEntities() {
        return String.format("E: %d/%d", this.countEntitiesRendered, this.countEntitiesTotal);
    }

    @Override
    public void setupTerrain(final Entity viewEntity, final double partialTicks, final ICamera camera, final int frameCount, final boolean playerSpectator) {
        if (ConfigurationHandler.renderDistance != this.renderDistanceChunks || this.vboEnabled != OpenGlHelper.useVbo()) {
            loadRenderers();
        }

        this.profiler.startSection("camera");
        final double posX = PLAYER_POSITION_OFFSET.x;
        final double posY = PLAYER_POSITION_OFFSET.y;
        final double posZ = PLAYER_POSITION_OFFSET.z;

        final double deltaX = posX - this.frustumUpdatePosX;
        final double deltaY = posY - this.frustumUpdatePosY;
        final double deltaZ = posZ - this.frustumUpdatePosZ;

        final int chunkCoordX = MathHelper.floor_double(posX) >> 4;
        final int chunkCoordY = MathHelper.floor_double(posY) >> 4;
        final int chunkCoordZ = MathHelper.floor_double(posZ) >> 4;

        if (this.frustumUpdatePosChunkX != chunkCoordX || this.frustumUpdatePosChunkY != chunkCoordY || this.frustumUpdatePosChunkZ != chunkCoordZ || deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 16.0) {
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
        final BlockPos posEye = new BlockPos(posX, posY + viewEntity.getEyeHeight(), posZ);
        final RenderChunk renderchunk = this.viewFrustum.getRenderChunk(posEye);
        final RenderOverlay renderoverlay = this.viewFrustum.getRenderOverlay(posEye);

        this.displayListEntitiesDirty = this.displayListEntitiesDirty || !this.chunksToUpdate.isEmpty() || posX != this.lastViewEntityX || posY != this.lastViewEntityY || posZ != this.lastViewEntityZ || viewEntity.rotationPitch != this.lastViewEntityPitch || viewEntity.rotationYaw != this.lastViewEntityYaw;
        this.lastViewEntityX = posX;
        this.lastViewEntityY = posY;
        this.lastViewEntityZ = posZ;
        this.lastViewEntityPitch = viewEntity.rotationPitch;
        this.lastViewEntityYaw = viewEntity.rotationYaw;

        if (this.displayListEntitiesDirty) {
            this.displayListEntitiesDirty = false;
            this.renderInfos = Lists.newArrayListWithCapacity(CHUNKS);

            final LinkedList<ContainerLocalRenderInformation> renderInfoList = Lists.newLinkedList();
            boolean renderChunksMany = this.mc.renderChunksMany;

            if (renderchunk == null) {
                final int chunkY = posEye.getY() > 0 ? 248 : 8;

                for (int chunkX = -this.renderDistanceChunks; chunkX <= this.renderDistanceChunks; chunkX++) {
                    for (int chunkZ = -this.renderDistanceChunks; chunkZ <= this.renderDistanceChunks; chunkZ++) {
                        final BlockPos pos = new BlockPos((chunkX << 4) + 8, chunkY, (chunkZ << 4) + 8);
                        final RenderChunk renderChunk = this.viewFrustum.getRenderChunk(pos);
                        final RenderOverlay renderOverlay = this.viewFrustum.getRenderOverlay(pos);

                        if (renderChunk != null && camera.isBoundingBoxInFrustum(renderChunk.boundingBox)) {
                            renderChunk.setFrameIndex(frameCount);
                            renderOverlay.setFrameIndex(frameCount);
                            renderInfoList.add(new ContainerLocalRenderInformation(renderChunk, renderOverlay, null, 0));
                        }
                    }
                }
            } else {
                boolean add = false;
                final ContainerLocalRenderInformation renderInfo = new ContainerLocalRenderInformation(renderchunk, renderoverlay, null, 0);
                final Set<EnumFacing> visibleSides = getVisibleSides(posEye);

                if (!visibleSides.isEmpty() && visibleSides.size() == 1) {
                    final Vector3f viewVector = getViewVector(viewEntity, partialTicks);
                    final EnumFacing facing = EnumFacing.getFacingFromVector(viewVector.x, viewVector.y, viewVector.z).getOpposite();
                    visibleSides.remove(facing);
                }

                if (visibleSides.isEmpty()) {
                    add = true;
                }

                if (add && !playerSpectator) {
                    this.renderInfos.add(renderInfo);
                } else {
                    if (playerSpectator && this.world.getBlockState(posEye).getBlock().isOpaqueCube()) {
                        renderChunksMany = false;
                    }

                    renderchunk.setFrameIndex(frameCount);
                    renderoverlay.setFrameIndex(frameCount);
                    renderInfoList.add(renderInfo);
                }
            }

            while (!renderInfoList.isEmpty()) {
                final ContainerLocalRenderInformation renderInfo = renderInfoList.poll();
                final RenderChunk renderChunk = renderInfo.renderChunk;
                final EnumFacing facing = renderInfo.facing;
                final BlockPos posChunk = renderChunk.getPosition();
                this.renderInfos.add(renderInfo);

                for (final EnumFacing side : EnumFacing.VALUES) {
                    final RenderChunk neighborRenderChunk = getNeighborRenderChunk(posEye, posChunk, side);
                    final RenderOverlay neighborRenderOverlay = getNeighborRenderOverlay(posEye, posChunk, side);

                    if ((!renderChunksMany || !renderInfo.setFacing.contains(side.getOpposite())) && (!renderChunksMany || facing == null || renderChunk.getCompiledChunk().isVisible(facing.getOpposite(), side)) && neighborRenderChunk != null && neighborRenderChunk.setFrameIndex(frameCount) && camera.isBoundingBoxInFrustum(neighborRenderChunk.boundingBox)) {
                        final ContainerLocalRenderInformation renderInfoNext = new ContainerLocalRenderInformation(neighborRenderChunk, neighborRenderOverlay, side, renderInfo.counter + 1);
                        renderInfoNext.setFacing.addAll(renderInfo.setFacing);
                        renderInfoNext.setFacing.add(side);
                        renderInfoList.add(renderInfoNext);
                    }
                }
            }
        }

        this.renderDispatcher.clearChunkUpdates();
        this.renderDispatcherOverlay.clearChunkUpdates();
        final Set<RenderChunk> set = this.chunksToUpdate;
        final Set<RenderOverlay> set1 = this.overlaysToUpdate;
        this.chunksToUpdate = Sets.newLinkedHashSet();

        for (final ContainerLocalRenderInformation renderInfo : this.renderInfos) {
            final RenderChunk renderChunk = renderInfo.renderChunk;
            final RenderOverlay renderOverlay = renderInfo.renderOverlay;

            if (renderChunk.isNeedsUpdate() || set.contains(renderChunk)) {
                this.displayListEntitiesDirty = true;

                this.chunksToUpdate.add(renderChunk);
            }

            if (renderOverlay.isNeedsUpdate() || set1.contains(renderOverlay)) {
                this.displayListEntitiesDirty = true;

                this.overlaysToUpdate.add(renderOverlay);
            }
        }

        this.chunksToUpdate.addAll(set);
        this.overlaysToUpdate.addAll(set1);
        this.profiler.endSection();
    }

    private Set<EnumFacing> getVisibleSides(final BlockPos pos) {
        final VisGraph visgraph = new VisGraph();
        final BlockPos posChunk = new BlockPos(pos.getX() & ~0xF, pos.getY() & ~0xF, pos.getZ() & ~0xF);

        for (final BlockPos.MutableBlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(posChunk, posChunk.add(15, 15, 15))) {
            if (this.world.getBlockState(mutableBlockPos).getBlock().isOpaqueCube()) {
                visgraph.func_178606_a(mutableBlockPos);
            }
        }

        return visgraph.func_178609_b(pos);
    }

    private RenderChunk getNeighborRenderChunk(final BlockPos posEye, final BlockPos posChunk, final EnumFacing side) {
        final BlockPos offset = posChunk.offset(side, 16);
        if (MathHelper.abs_int(posEye.getX() - offset.getX()) > this.renderDistanceChunks * 16) {
            return null;
        }

        if (offset.getY() < 0 || offset.getY() >= 256) {
            return null;
        }

        if (MathHelper.abs_int(posEye.getZ() - offset.getZ()) > this.renderDistanceChunks * 16) {
            return null;
        }

        return this.viewFrustum.getRenderChunk(offset);
    }

    private RenderOverlay getNeighborRenderOverlay(final BlockPos posEye, final BlockPos posChunk, final EnumFacing side) {
        final BlockPos offset = posChunk.offset(side, 16);
        if (MathHelper.abs_int(posEye.getX() - offset.getX()) > this.renderDistanceChunks * 16) {
            return null;
        }

        if (offset.getY() < 0 || offset.getY() >= 256) {
            return null;
        }

        if (MathHelper.abs_int(posEye.getZ() - offset.getZ()) > this.renderDistanceChunks * 16) {
            return null;
        }

        return this.viewFrustum.getRenderOverlay(offset);
    }

    @Override
    public int renderBlockLayer(final EnumWorldBlockLayer layer, final double partialTicks, final int pass, final Entity entity) {
        RenderHelper.disableStandardItemLighting();

        if (layer == EnumWorldBlockLayer.TRANSLUCENT) {
            this.profiler.startSection("translucent_sort");
            final double posX = PLAYER_POSITION_OFFSET.x;
            final double posY = PLAYER_POSITION_OFFSET.y;
            final double posZ = PLAYER_POSITION_OFFSET.z;

            final double deltaX = posX - this.prevRenderSortX;
            final double deltaY = posY - this.prevRenderSortY;
            final double deltaZ = posZ - this.prevRenderSortZ;

            if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > 1.0) {
                this.prevRenderSortX = posX;
                this.prevRenderSortY = posY;
                this.prevRenderSortZ = posZ;
                int count = 0;

                for (final ContainerLocalRenderInformation renderInfo : this.renderInfos) {
                    if (renderInfo.renderChunk.compiledChunk.isLayerStarted(layer) && count++ < 15) {
                        this.renderDispatcher.updateTransparencyLater(renderInfo.renderChunk);
                        this.renderDispatcherOverlay.updateTransparencyLater(renderInfo.renderOverlay);
                    }
                }
            }

            this.profiler.endSection();
        }

        this.profiler.startSection("filterempty");
        int count = 0;
        final boolean isTranslucent = layer == EnumWorldBlockLayer.TRANSLUCENT;
        final int start = isTranslucent ? this.renderInfos.size() - 1 : 0;
        final int end = isTranslucent ? -1 : this.renderInfos.size();
        final int step = isTranslucent ? -1 : 1;

        for (int index = start; index != end; index += step) {
            final ContainerLocalRenderInformation renderInfo = this.renderInfos.get(index);
            final RenderChunk renderChunk = renderInfo.renderChunk;
            final RenderOverlay renderOverlay = renderInfo.renderOverlay;

            if (!renderChunk.getCompiledChunk().isLayerEmpty(layer)) {
                count++;
                this.renderContainer.addRenderChunk(renderChunk, layer);
            }

            if (isTranslucent && renderOverlay != null && !renderOverlay.getCompiledChunk().isLayerEmpty(layer)) {
                count++;
                this.renderContainer.addRenderOverlay(renderOverlay);
            }
        }

        this.profiler.endStartSection("render_" + layer);
        renderBlockLayer(layer);
        this.profiler.endSection();

        return count;
    }

    private void renderBlockLayer(final EnumWorldBlockLayer layer) {
        this.mc.entityRenderer.enableLightmap();

        this.renderContainer.renderChunkLayer(layer);

        this.mc.entityRenderer.disableLightmap();
    }

    @Override
    public void updateClouds() {
    }

    @Override
    public void renderSky(final float partialTicks, final int pass) {
    }

    @Override
    public void renderClouds(final float partialTicks, final int pass) {
    }

    @Override
    public boolean hasCloudFog(final double x, final double y, final double z, final float partialTicks) {
        return false;
    }

    @Override
    public void updateChunks(final long finishTimeNano) {
        this.displayListEntitiesDirty |= this.renderDispatcher.runChunkUploads(finishTimeNano);

        final Iterator<RenderChunk> chunkIterator = this.chunksToUpdate.iterator();
        while (chunkIterator.hasNext()) {
            final RenderChunk renderChunk = chunkIterator.next();
            if (!this.renderDispatcher.updateChunkLater(renderChunk)) {
                break;
            }

            renderChunk.setNeedsUpdate(false);
            chunkIterator.remove();
        }

        this.displayListEntitiesDirty |= this.renderDispatcherOverlay.runChunkUploads(finishTimeNano);

        final Iterator<RenderOverlay> overlayIterator = this.overlaysToUpdate.iterator();
        while (overlayIterator.hasNext()) {
            final RenderOverlay renderOverlay = overlayIterator.next();
            if (!this.renderDispatcherOverlay.updateChunkLater(renderOverlay)) {
                break;
            }

            renderOverlay.setNeedsUpdate(false);
            overlayIterator.remove();
        }
    }

    @Override
    public void renderWorldBorder(final Entity entity, final float partialTicks) {}

    @Override
    public void drawBlockDamageTexture(final Tessellator tessellator, final WorldRenderer worldRenderer, final Entity entity, final float partialTicks) {}

    @Override
    public void drawSelectionBox(final EntityPlayer player, final MovingObjectPosition movingObjectPosition, final int p_72731_3_, final float partialTicks) {}

    @Override
    public void markBlockForUpdate(final BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void notifyLightSet(final BlockPos pos) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
    }

    private void markBlocksForUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        if (this.world == null) {
            return;
        }

        final BlockPos.MutableBlockPos position = this.world.position;
        this.viewFrustum.markBlocksForUpdate(x1 - position.getX(), y1 - position.getY(), z1 - position.getZ(), x2 - position.getX(), y2 - position.getY(), z2 - position.getZ());
    }

    @Override
    public void playRecord(final String name, final BlockPos pos) {}

    @Override
    public void playSound(final String name, final double x, final double y, final double z, final float volume, final float pitch) {}

    @Override
    public void playSoundToNearExcept(final EntityPlayer player, final String name, final double x, final double y, final double z, final float volume, final float pitch) {}

    @Override
    public void spawnParticle(final int p_180442_1_, final boolean p_180442_2_, final double p_180442_3_, final double p_180442_5_, final double p_180442_7_, final double p_180442_9_, final double p_180442_11_, final double p_180442_13_, final int... p_180442_15_) {}

    @Override
    public void onEntityAdded(final Entity entityIn) {}

    @Override
    public void onEntityRemoved(final Entity entityIn) {}

    @Override
    public void deleteAllDisplayLists() {}

    @Override
    public void broadcastSound(final int p_180440_1_, final BlockPos pos, final int p_180440_3_) {}

    @Override
    public void playAuxSFX(final EntityPlayer player, final int sfxType, final BlockPos blockPosIn, final int p_180439_4_) {}

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress) {}

    @Override
    public void setDisplayListEntitiesDirty() {
        this.displayListEntitiesDirty = true;
    }

    @SideOnly(Side.CLIENT)
    private class ContainerLocalRenderInformation {
        final RenderChunk renderChunk;
        final RenderOverlay renderOverlay;
        final EnumFacing facing;
        final Set<EnumFacing> setFacing;
        final int counter;

        ContainerLocalRenderInformation(final RenderChunk renderChunk, final RenderOverlay renderOverlay, final EnumFacing facing, final int counter) {
            this.setFacing = EnumSet.noneOf(EnumFacing.class);
            this.renderChunk = renderChunk;
            this.renderOverlay = renderOverlay;
            this.facing = facing;
            this.counter = counter;
        }
    }
}
