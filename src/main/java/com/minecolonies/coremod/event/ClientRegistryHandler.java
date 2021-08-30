package com.minecolonies.coremod.event;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.model.ModelScarecrowBoth;
import com.minecolonies.coremod.client.model.raiders.*;
import com.minecolonies.coremod.client.render.*;
import com.minecolonies.coremod.client.render.mobs.RenderMercenary;
import com.minecolonies.coremod.client.render.mobs.amazon.RendererAmazon;
import com.minecolonies.coremod.client.render.mobs.amazon.RendererChiefAmazon;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererBarbarian;
import com.minecolonies.coremod.client.render.mobs.barbarians.RendererChiefBarbarian;
import com.minecolonies.coremod.client.render.mobs.egyptians.RendererArcherMummy;
import com.minecolonies.coremod.client.render.mobs.egyptians.RendererMummy;
import com.minecolonies.coremod.client.render.mobs.egyptians.RendererPharao;
import com.minecolonies.coremod.client.render.mobs.norsemen.RendererArcherNorsemen;
import com.minecolonies.coremod.client.render.mobs.norsemen.RendererChiefNorsemen;
import com.minecolonies.coremod.client.render.mobs.norsemen.RendererShieldmaidenNorsemen;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererArcherPirate;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererChiefPirate;
import com.minecolonies.coremod.client.render.mobs.pirates.RendererPirate;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;

public class ClientRegistryHandler
{
    public static final ModelLayerLocation CITIZEN       = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "citizen"), "citizen");

    public static final ModelLayerLocation MUMMY       = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "mummy"), "mummy");
    public static final ModelLayerLocation ARCHER_MUMMY       = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "archer_mummy"), "archer_mummy");
    public static final ModelLayerLocation PHARAO       = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "pharao"), "pharao");

    public static final ModelLayerLocation SHIELD_MAIDEN       = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "shield_maiden"), "shield_maiden");
    public static final ModelLayerLocation NORSEMEN_ARCHER       = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "norsemen_archer"), "norsemen_archer");
    public static final ModelLayerLocation NORSEMEN_CHIEF       = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "norsemen_chief"), "norsemen_chief");

    public static final ModelLayerLocation AMAZON       = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "amazon"), "amazon");
    public static final ModelLayerLocation AMAZON_CHIEF = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "amazon_chief"), "amazon_chief");

    public static final ModelLayerLocation SCARECROW       = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "scarecrow"), "scarecrow");


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(AMAZON, ModelAmazon::createMesh);
        event.registerLayerDefinition(AMAZON_CHIEF, ModelAmazonChief::createMesh);

        event.registerLayerDefinition(ARCHER_MUMMY, ModelArcherMummy::createMesh);
        event.registerLayerDefinition(MUMMY, ModelMummy::createMesh);
        event.registerLayerDefinition(PHARAO, ModelPharaoh::createMesh);

        event.registerLayerDefinition(SHIELD_MAIDEN, ModelShieldmaiden::createMesh);
        event.registerLayerDefinition(NORSEMEN_ARCHER, ModelArcherNorsemen::createMesh);
        event.registerLayerDefinition(NORSEMEN_CHIEF, ModelChiefNorsemen::createMesh);

        event.registerLayerDefinition(SCARECROW, ModelScarecrowBoth::createMesh);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void doClientStuff(final EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.CITIZEN, RenderBipedCitizen::new);
        event.registerEntityRenderer(ModEntities.VISITOR, RenderBipedCitizen::new);
        event.registerEntityRenderer(ModEntities.FISHHOOK, RenderFishHook::new);
        event.registerEntityRenderer(ModEntities.FIREARROW, FireArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.MC_NORMAL_ARROW, TippableArrowRenderer::new);

        event.registerEntityRenderer(ModEntities.BARBARIAN, RendererBarbarian::new);
        event.registerEntityRenderer(ModEntities.ARCHERBARBARIAN, RendererBarbarian::new);
        event.registerEntityRenderer(ModEntities.CHIEFBARBARIAN, RendererChiefBarbarian::new);
        event.registerEntityRenderer(ModEntities.PIRATE, RendererPirate::new);
        event.registerEntityRenderer(ModEntities.ARCHERPIRATE, RendererArcherPirate::new);
        event.registerEntityRenderer(ModEntities.CHIEFPIRATE, RendererChiefPirate::new);

        event.registerEntityRenderer(ModEntities.MUMMY, RendererMummy::new);
        event.registerEntityRenderer(ModEntities.ARCHERMUMMY, RendererArcherMummy::new);
        event.registerEntityRenderer(ModEntities.PHARAO, RendererPharao::new);

        event.registerEntityRenderer(ModEntities.SHIELDMAIDEN, RendererShieldmaidenNorsemen::new);
        event.registerEntityRenderer(ModEntities.NORSEMEN_ARCHER, RendererArcherNorsemen::new);
        event.registerEntityRenderer(ModEntities.NORSEMEN_CHIEF, RendererChiefNorsemen::new);

        event.registerEntityRenderer(ModEntities.AMAZON, RendererAmazon::new);
        event.registerEntityRenderer(ModEntities.AMAZONCHIEF, RendererChiefAmazon::new);

        event.registerEntityRenderer(ModEntities.MERCENARY, RenderMercenary::new);
        event.registerEntityRenderer(ModEntities.SITTINGENTITY, RenderSitting::new);
        event.registerEntityRenderer(ModEntities.MINECART, (context) -> new MinecartRenderer<>(context, ModelLayers.MINECART));

        event.registerBlockEntityRenderer(MinecoloniesTileEntities.BUILDING, EmptyTileEntitySpecialRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.SCARECROW, TileEntityScarecrowRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.ENCHANTER, TileEntityEnchanterRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.COLONY_FLAG, TileEntityColonyFlagRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.NAMED_GRAVE, TileEntityNamedGraveRenderer::new);

        Arrays.stream(ModBlocks.getHuts())
          .forEach(hut -> ItemBlockRenderTypes.setRenderLayer(hut, renderType -> renderType.equals(RenderType.cutout()) || renderType.equals(RenderType.solid())));
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockScarecrow, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockRack, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockDecorationPlaceholder, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockCompostedDirt, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockBarrel, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockWayPoint, RenderType.cutout());
    }
}
