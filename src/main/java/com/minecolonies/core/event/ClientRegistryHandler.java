package com.minecolonies.core.event;

import com.ldtteam.blockui.mod.BlockUI;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.client.ModKeyMappings;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.model.*;
import com.minecolonies.core.client.model.raiders.*;
import com.minecolonies.core.client.render.*;
import com.minecolonies.core.client.render.mobs.RenderMercenary;
import com.minecolonies.core.client.render.mobs.amazon.RendererAmazon;
import com.minecolonies.core.client.render.mobs.amazon.RendererAmazonSpearman;
import com.minecolonies.core.client.render.mobs.amazon.RendererChiefAmazon;
import com.minecolonies.core.client.render.mobs.barbarians.RendererBarbarian;
import com.minecolonies.core.client.render.mobs.barbarians.RendererChiefBarbarian;
import com.minecolonies.core.client.render.mobs.drownedpirates.RendererDrownedArcherPirate;
import com.minecolonies.core.client.render.mobs.drownedpirates.RendererDrownedChiefPirate;
import com.minecolonies.core.client.render.mobs.drownedpirates.RendererDrownedPirate;
import com.minecolonies.core.client.render.mobs.egyptians.RendererArcherMummy;
import com.minecolonies.core.client.render.mobs.egyptians.RendererMummy;
import com.minecolonies.core.client.render.mobs.egyptians.RendererPharao;
import com.minecolonies.core.client.render.mobs.norsemen.RendererArcherNorsemen;
import com.minecolonies.core.client.render.mobs.norsemen.RendererChiefNorsemen;
import com.minecolonies.core.client.render.mobs.norsemen.RendererShieldmaidenNorsemen;
import com.minecolonies.core.client.render.mobs.pirates.RendererArcherPirate;
import com.minecolonies.core.client.render.mobs.pirates.RendererChiefPirate;
import com.minecolonies.core.client.render.mobs.pirates.RendererPirate;
import com.minecolonies.core.client.render.projectile.FireArrowRenderer;
import com.minecolonies.core.client.render.projectile.RendererSpear;
import com.minecolonies.core.client.render.SpearSpecialRenderer;
import com.minecolonies.core.network.messages.client.ClientOnlyNetworkHooks;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.model.sprite.AtlasManager.AtlasConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public class ClientRegistryHandler
{
    static
    {
        ClientOnlyNetworkHooks.register();
    }

    public static final ModelLayerLocation FEMALE_FARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_farmer"), "female_farmer");
    public static final ModelLayerLocation MALE_COURIER  = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_deliveryman"), "male_deliveryman");
    public static final ModelLayerLocation FEMALE_CHILD  = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_child"), "female_child");
    public static final ModelLayerLocation FEMALE_SHEEPFARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_sheepfarmer"), "female_sheepfarmer");
    public static final ModelLayerLocation MALE_CHILD = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_child"), "male_child");
    public static final ModelLayerLocation FEMALE_CONCRETEMIXER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_concretemixer"), "female_concretemixer");
    public static final ModelLayerLocation MALE_COOK = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_cook"), "male_cook");
    public static final ModelLayerLocation MALE_SHEEPFARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_sheepfarmer"), "male_sheepfarmer");
    public static final ModelLayerLocation MALE_SMELTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_smelter"), "male_smelter");
    public static final ModelLayerLocation MALE_UNDERTAKER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_undertaker"), "male_undertaker");
    public static final ModelLayerLocation FEMALE_BUILDER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_builder"), "female_builder");
    public static final ModelLayerLocation MALE_BUILDER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_builder"), "male_builder");
    public static final ModelLayerLocation FEMALE_BAKER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_baker"), "female_baker");
    public static final ModelLayerLocation MALE_MECHANIST = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_mechanist"), "male_mechanist");
    public static final ModelLayerLocation FEMALE_TEACHER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_teacher"), "female_teacher");
    public static final ModelLayerLocation FEMALE_COMPOSTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_composter"), "female_composter");
    public static final ModelLayerLocation FEMALE_RABBITHERDER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_rabbitherder"), "female_rabbitherder");
    public static final ModelLayerLocation FEMALE_DYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_dyer"), "female_dyer");
    public static final ModelLayerLocation FEMALE_UNDERTAKER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_undertaker"), "female_undertaker");
    public static final ModelLayerLocation MALE_COMPOSTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_composter"), "male_composter");
    public static final ModelLayerLocation MALE_FLETCHER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_fletcher"), "male_fletcher");
    public static final ModelLayerLocation MALE_CITIZEN    = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_citizen"), "male_citizen");
    public static final ModelLayerLocation FEMALE_CITIZEN    = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_citizen"), "female_citizen");
    public static final ModelLayerLocation FEMALE_SETTLER    = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_settler"), "female_settler");
    public static final ModelLayerLocation MALE_SETTLER    = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_settler"), "male_settler");
    public static final ModelLayerLocation FEMALE_FISHER     = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_fisherman"), "female_fisherman");
    public static final ModelLayerLocation MALE_RABBITHERDER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_rabbitherder"), "male_rabbitherder");
    public static final ModelLayerLocation FEMALE_FLETCHER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_fletcher"), "female_fletcher");
    public static final ModelLayerLocation FEMALE_CRAFTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_crafter"), "female_crafter");
    public static final ModelLayerLocation MALE_DYER          = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_dyer"), "male_dyer");
    public static final ModelLayerLocation MALE_FORESTER      = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_lumberjack"), "male_lumberjack");
    public static final ModelLayerLocation MALE_CONCRETEMIXER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_concretemixer"), "male_concretemixer");
    public static final ModelLayerLocation FEMALE_CHICKENFARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_chickenfarmer"), "female_chickenfarmer");
    public static final ModelLayerLocation MALE_MINER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_miner"), "male_miner");
    public static final ModelLayerLocation FEMALE_MINER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_miner"), "female_miner");
    public static final ModelLayerLocation MALE_CHICKENFARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_chickenfarmer"), "male_chickenfarmer");
    public static final ModelLayerLocation MALE_GLASSBLOWER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_glassblower"), "male_glassblower");
    public static final ModelLayerLocation FEMALE_PIGFARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_pigfarmer"), "female_pigfarmer");
    public static final ModelLayerLocation FEMALE_CITIZENNOBLE = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_citizennoble"), "female_citizennoble");
    public static final ModelLayerLocation MALE_CITIZENNOBLE = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_citizennoble"), "male_citizennoble");
    public static final ModelLayerLocation MALE_BLACKSMITH = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_blacksmith"), "male_blacksmith");
    public static final ModelLayerLocation FEMALE_GLASSBLOWER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_glassblower"), "female_glassblower");
    public static final ModelLayerLocation FEMALE_BLACKSMITH = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_blacksmith"), "female_blacksmith");
    public static final ModelLayerLocation MALE_FARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_farmer"), "male_farmer");
    public static final ModelLayerLocation MALE_PIGFARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_pigfarmer"), "male_pigfarmer");
    public static final ModelLayerLocation FEMALE_ARISTOCRAT = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_aristocrat"), "female_aristocrat");
    public static final ModelLayerLocation MALE_ARISTOCRAT = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_aristocrat"), "male_aristocrat");
    public static final ModelLayerLocation MALE_COWFARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_cowfarmer"), "male_cowfarmer");
    public static final ModelLayerLocation FEMALE_SMELTER  = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_smelter"), "female_smelter");
    public static final ModelLayerLocation FEMALE_FORESTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_lumberjack"), "female_lumberjack");
    public static final ModelLayerLocation FEMALE_COURIER  = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_deliveryman"), "female_deliveryman");
    public static final ModelLayerLocation FEMALE_HEALER     = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_healer"), "female_healer");
    public static final ModelLayerLocation FEMALE_PLANTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_planter"), "female_planter");
    public static final ModelLayerLocation FEMALE_STUDENT = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_student"), "female_student");
    public static final ModelLayerLocation FEMALE_COWFARMER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_cowfarmer"), "female_cowfarmer");
    public static final ModelLayerLocation FEMALE_MECHANIST = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_mechanist"), "female_mechanist");
    public static final ModelLayerLocation FEMALE_COOK  = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_cook"), "female_cook");
    public static final ModelLayerLocation MALE_FISHER  = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_fisherman"), "male_fisherman");
    public static final ModelLayerLocation MALE_PLANTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_planter"), "male_planter");
    public static final ModelLayerLocation MALE_BAKER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_baker"), "male_baker");
    public static final ModelLayerLocation FEMALE_BEEKEEPER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_beekeeper"), "female_beekeeper");
    public static final ModelLayerLocation MALE_BEEKEEPER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_beekeeper"), "male_beekeeper");
    public static final ModelLayerLocation MALE_TEACHER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_teacher"), "male_teacher");
    public static final ModelLayerLocation MALE_STUDENT = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_student"), "male_student");
    public static final ModelLayerLocation MALE_HEALER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_healer"), "male_healer");
    public static final ModelLayerLocation MALE_CRAFTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_crafter"), "male_crafter");
    public static final ModelLayerLocation MALE_DRUID = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_druid"), "male_druid");
    public static final ModelLayerLocation FEMALE_DRUID = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_druid"), "female_druid");
    public static final ModelLayerLocation MALE_NETHERWORKER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_netherworker"), "male_netherworker");
    public static final ModelLayerLocation FEMALE_NETHERWORKER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_netherworker"), "female_netherworker");
    public static final ModelLayerLocation MALE_ENCHANTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_enchanter"), "male_enchanter");
    public static final ModelLayerLocation FEMALE_ENCHANTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_enchanter"), "female_enchanter");
    public static final ModelLayerLocation MALE_FLORIST = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_florist"), "male_florist");
    public static final ModelLayerLocation FEMALE_FLORIST = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_florist"), "female_florist");
    public static final ModelLayerLocation MALE_KNIGHT = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_knight"), "male_knight");
    public static final ModelLayerLocation FEMALE_KNIGHT = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_knight"), "female_knight");
    public static final ModelLayerLocation MALE_ARCHER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_archer"), "male_archer");
    public static final ModelLayerLocation FEMALE_ARCHER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_archer"), "female_archer");
    public static final ModelLayerLocation FEMALE_CARPENTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_carpenter"), "female_carpenter");
    public static final ModelLayerLocation MALE_CARPENTER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_carpenter"), "male_carpenter");
    public static final ModelLayerLocation MALE_ALCHEMIST = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "male_alchemist"), "male_alchemist");
    public static final ModelLayerLocation FEMALE_ALCHEMIST = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "female_alchemist"), "female_alchemist");

    public static final ModelLayerLocation MERCENARY    = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "mercenary"), "mercenary");

    public static final ModelLayerLocation MUMMY        = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "mummy"), "mummy");
    public static final ModelLayerLocation ARCHER_MUMMY = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "archer_mummy"), "archer_mummy");
    public static final ModelLayerLocation PHARAO       = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pharao"), "pharao");

    public static final ModelLayerLocation SHIELD_MAIDEN   = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "shield_maiden"), "shield_maiden");
    public static final ModelLayerLocation NORSEMEN_ARCHER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "norsemen_archer"), "norsemen_archer");
    public static final ModelLayerLocation NORSEMEN_CHIEF  = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "norsemen_chief"), "norsemen_chief");

    public static final ModelLayerLocation AMAZON       = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "amazon"), "amazon");
    public static final ModelLayerLocation AMAZON_CHIEF = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "amazon_chief"), "amazon_chief");
    public static final ModelLayerLocation AMAZON_SPEARMAN = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "amazon_spearman"), "amazon_spearman");

    public static final ModelLayerLocation SCARECROW = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "scarecrow"), "scarecrow");

    public static final ModelLayerLocation CITIZEN = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "citizen"), "citizen");

    /**
     * Registers the MineColonies GUI atlas used by the citizen health and happiness sprites.
     *
     * <p>The 26.2 client no longer discovers a mod's atlas just because an
     * {@code atlases/<name>.json} file is present. Registering the atlas here keeps the
     * unstitch sprites (for example {@code minecolonies:citizen/smiley/full}) available to
     * BlockUI's image resolver.</p>
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerTextureAtlases(final RegisterTextureAtlasesEvent event)
    {
        final Identifier atlasKey = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "minecolonies_gui");
        BlockUI.NAMESPACE_TO_ATLAS_MAP.put(Constants.MOD_ID, atlasKey);
        event.register(new AtlasConfig(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/atlas/minecolonies_gui.png"),
            atlasKey,
            false,
            Set.of(GuiMetadataSection.TYPE)));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(MERCENARY, MercenaryModel::createMesh);

        event.registerLayerDefinition(AMAZON, ModelAmazon::createMesh);
        event.registerLayerDefinition(AMAZON_CHIEF, ModelAmazonChief::createMesh);
        event.registerLayerDefinition(AMAZON_SPEARMAN, ModelAmazonSpearman::createMesh);

        event.registerLayerDefinition(ARCHER_MUMMY, ModelArcherMummy::createMesh);
        event.registerLayerDefinition(MUMMY, ModelMummy::createMesh);
        event.registerLayerDefinition(PHARAO, ModelPharaoh::createMesh);

        event.registerLayerDefinition(SHIELD_MAIDEN, ModelShieldmaiden::createMesh);
        event.registerLayerDefinition(NORSEMEN_ARCHER, ModelArcherNorsemen::createMesh);
        event.registerLayerDefinition(NORSEMEN_CHIEF, ModelChiefNorsemen::createMesh);

        event.registerLayerDefinition(SCARECROW, ScarecrowModel::createMesh);

        event.registerLayerDefinition(FEMALE_FARMER, FemaleFarmerModel::createMesh);
        event.registerLayerDefinition(MALE_COURIER, MaleCourierModel::createMesh);
        event.registerLayerDefinition(FEMALE_CHILD, FemaleChildModel::createMesh);
        event.registerLayerDefinition(FEMALE_SHEEPFARMER, FemaleShepherdModel::createMesh);
        event.registerLayerDefinition(MALE_CHILD, MaleChildModel::createMesh);
        event.registerLayerDefinition(FEMALE_CONCRETEMIXER, FemaleConcreteMixerModel::createMesh);
        event.registerLayerDefinition(MALE_COOK, MaleCookModel::createMesh);
        event.registerLayerDefinition(MALE_SHEEPFARMER, MaleShepherdModel::createMesh);
        event.registerLayerDefinition(MALE_SMELTER, MaleSmelterModel::createMesh);
        event.registerLayerDefinition(MALE_UNDERTAKER, MaleUndertakerModel::createMesh);
        event.registerLayerDefinition(FEMALE_BUILDER, FemaleBuilderModel::createMesh);
        event.registerLayerDefinition(MALE_BUILDER, MaleBuilderModel::createMesh);
        event.registerLayerDefinition(FEMALE_BAKER, FemaleBakerModel::createMesh);
        event.registerLayerDefinition(MALE_MECHANIST, MaleMechanistModel::createMesh);
        event.registerLayerDefinition(FEMALE_TEACHER, FemaleTeacherModel::createMesh);
        event.registerLayerDefinition(FEMALE_COMPOSTER, FemaleComposterModel::createMesh);
        event.registerLayerDefinition(FEMALE_RABBITHERDER, FemaleRabbitHerderModel::createMesh);
        event.registerLayerDefinition(FEMALE_DYER, FemaleDyerModel::createMesh);
        event.registerLayerDefinition(FEMALE_UNDERTAKER, FemaleUndertakerModel::createMesh);
        event.registerLayerDefinition(MALE_COMPOSTER, MaleComposterModel::createMesh);
        event.registerLayerDefinition(MALE_FLETCHER, MaleFletcherModel::createMesh);
        event.registerLayerDefinition(FEMALE_CITIZEN, FemaleCitizenModel::createMesh);
        event.registerLayerDefinition(MALE_CITIZEN, MaleCitizenModel::createMesh);
        event.registerLayerDefinition(FEMALE_SETTLER, FemaleSettlerModel::createMesh);
        event.registerLayerDefinition(MALE_SETTLER, MaleSettlerModel::createMesh);
        event.registerLayerDefinition(FEMALE_FISHER, FemaleFisherModel::createMesh);
        event.registerLayerDefinition(MALE_RABBITHERDER, MaleRabbitHerderModel::createMesh);
        event.registerLayerDefinition(FEMALE_FLETCHER, FemaleFletcherModel::createMesh);
        event.registerLayerDefinition(FEMALE_CRAFTER, FemaleCrafterModel::createMesh);
        event.registerLayerDefinition(MALE_DYER, MaleDyerModel::createMesh);
        event.registerLayerDefinition(MALE_FORESTER, MaleForesterModel::createMesh);
        event.registerLayerDefinition(MALE_CONCRETEMIXER, MaleConcreteMixerModel::createMesh);
        event.registerLayerDefinition(FEMALE_CHICKENFARMER, FemaleChickenHerderModel::createMesh);
        event.registerLayerDefinition(MALE_MINER, MaleMinerModel::createMesh);
        event.registerLayerDefinition(FEMALE_MINER, FemaleMinerModel::createMesh);
        event.registerLayerDefinition(MALE_CHICKENFARMER, MaleChickenHerderModel::createMesh);
        event.registerLayerDefinition(MALE_GLASSBLOWER, MaleGlassblowerModel::createMesh);
        event.registerLayerDefinition(FEMALE_PIGFARMER, FemaleSwineHerderModel::createMesh);
        event.registerLayerDefinition(FEMALE_CITIZENNOBLE, FemaleNobleModle::createMesh);
        event.registerLayerDefinition(MALE_CITIZENNOBLE, MaleNobleModel::createMesh);
        event.registerLayerDefinition(MALE_BLACKSMITH, MaleBlacksmithModel::createMesh);
        event.registerLayerDefinition(FEMALE_GLASSBLOWER, FemaleGlassblowerModel::createMesh);
        event.registerLayerDefinition(FEMALE_BLACKSMITH, FemaleBlacksmithModel::createMesh);
        event.registerLayerDefinition(MALE_FARMER, MaleFarmerModel::createMesh);
        event.registerLayerDefinition(MALE_PIGFARMER, MaleSwineHerderModel::createMesh);
        event.registerLayerDefinition(FEMALE_ARISTOCRAT, FemaleAristocratModel::createMesh);
        event.registerLayerDefinition(MALE_ARISTOCRAT, MaleAristocratModel::createMesh);
        event.registerLayerDefinition(MALE_COWFARMER, MaleCowHerderModel::createMesh);
        event.registerLayerDefinition(FEMALE_SMELTER, FemaleSmelterModel::createMesh);
        event.registerLayerDefinition(FEMALE_FORESTER, FemaleForesterModel::createMesh);
        event.registerLayerDefinition(FEMALE_COURIER, FemaleCourierModel::createMesh);
        event.registerLayerDefinition(FEMALE_HEALER, FemaleHealerModel::createMesh);
        event.registerLayerDefinition(FEMALE_PLANTER, FemalePlanterModel::createMesh);
        event.registerLayerDefinition(FEMALE_STUDENT, FemaleStudentModel::createMesh);
        event.registerLayerDefinition(FEMALE_COWFARMER, FemaleCowHerderModel::createMesh);
        event.registerLayerDefinition(FEMALE_MECHANIST, FemaleMechanistModel::createMesh);
        event.registerLayerDefinition(FEMALE_COOK, FemaleCookModel::createMesh);
        event.registerLayerDefinition(MALE_FISHER, MaleFisherModel::createMesh);
        event.registerLayerDefinition(MALE_PLANTER, MalePlanterModel::createMesh);
        event.registerLayerDefinition(MALE_BAKER, MaleBakerModel::createMesh);
        event.registerLayerDefinition(FEMALE_BEEKEEPER, FemaleApiaryModel::createMesh);
        event.registerLayerDefinition(MALE_BEEKEEPER, MaleApiaryModel::createMesh);
        event.registerLayerDefinition(MALE_TEACHER, MaleTeacherModel::createMesh);
        event.registerLayerDefinition(MALE_STUDENT, MaleStudentModel::createMesh);
        event.registerLayerDefinition(MALE_HEALER, MaleHealerModel::createMesh);
        event.registerLayerDefinition(MALE_CRAFTER, MaleCrafterModel::createMesh);
        event.registerLayerDefinition(MALE_DRUID, MaleDruidModel::createMesh);
        event.registerLayerDefinition(FEMALE_DRUID, FemaleDruidModel::createMesh);
        event.registerLayerDefinition(MALE_NETHERWORKER, MaleNetherWorkerModel::createMesh);
        event.registerLayerDefinition(FEMALE_NETHERWORKER, FemaleNetherWorkerModel::createMesh);
        event.registerLayerDefinition(MALE_FLORIST, MaleFloristModel::createMesh);
        event.registerLayerDefinition(FEMALE_FLORIST, FemaleFloristModel::createMesh);
        event.registerLayerDefinition(MALE_ENCHANTER, MaleEnchanterModel::createMesh);
        event.registerLayerDefinition(FEMALE_ENCHANTER, FemaleEnchanterModel::createMesh);
        event.registerLayerDefinition(MALE_KNIGHT, MaleKnightModel::createMesh);
        event.registerLayerDefinition(FEMALE_KNIGHT, FemaleKnightModel::createMesh);
        event.registerLayerDefinition(MALE_ARCHER, MaleArcherModel::createMesh);
        event.registerLayerDefinition(FEMALE_ARCHER, FemaleArcherModel::createMesh);
        event.registerLayerDefinition(MALE_CARPENTER, MaleCarpenterModel::createMesh);
        event.registerLayerDefinition(FEMALE_CARPENTER, FemaleCarpenterModel::createMesh);
        event.registerLayerDefinition(MALE_ALCHEMIST, MaleAlchemistModel::createMesh);
        event.registerLayerDefinition(FEMALE_ALCHEMIST, FemaleAlchemistModel::createMesh);

        event.registerLayerDefinition(CITIZEN, CitizenModel::createMesh);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRegisterItemDecorations(final RegisterItemDecorationsEvent event)
    {
        event.register(ModItems.colonyMap, new ColonyMapDecorator());
        event.register(ModItems.clipboard, new ClipBoardDecorator());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void doClientStuff(final EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.CITIZEN, RenderBipedCitizen::new);
        event.registerEntityRenderer(ModEntities.VISITOR, RenderBipedCitizen::new);
        event.registerEntityRenderer(ModEntities.FISHHOOK, RenderFishHook::new);
        event.registerEntityRenderer(ModEntities.FIREARROW, FireArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.SPEAR, RendererSpear::new);

        event.registerEntityRenderer(ModEntities.MC_NORMAL_ARROW, TippableArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.DRUID_POTION, m -> new ThrownItemRenderer<>(m, 1.0F, true));

        // Raiders

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
        event.registerEntityRenderer(ModEntities.AMAZONSPEARMAN, RendererAmazonSpearman::new);

        event.registerEntityRenderer(ModEntities.DROWNED_PIRATE, RendererDrownedPirate::new);
        event.registerEntityRenderer(ModEntities.DROWNED_ARCHERPIRATE, RendererDrownedArcherPirate::new);
        event.registerEntityRenderer(ModEntities.DROWNED_CHIEFPIRATE, RendererDrownedChiefPirate::new);

        // Camp Raiders

        event.registerEntityRenderer(ModEntities.CAMP_BARBARIAN, RendererBarbarian::new);
        event.registerEntityRenderer(ModEntities.CAMP_ARCHERBARBARIAN, RendererBarbarian::new);
        event.registerEntityRenderer(ModEntities.CAMP_CHIEFBARBARIAN, RendererChiefBarbarian::new);

        event.registerEntityRenderer(ModEntities.CAMP_PIRATE, RendererPirate::new);
        event.registerEntityRenderer(ModEntities.CAMP_ARCHERPIRATE, RendererArcherPirate::new);
        event.registerEntityRenderer(ModEntities.CAMP_CHIEFPIRATE, RendererChiefPirate::new);

        event.registerEntityRenderer(ModEntities.CAMP_MUMMY, RendererMummy::new);
        event.registerEntityRenderer(ModEntities.CAMP_ARCHERMUMMY, RendererArcherMummy::new);
        event.registerEntityRenderer(ModEntities.CAMP_PHARAO, RendererPharao::new);

        event.registerEntityRenderer(ModEntities.CAMP_SHIELDMAIDEN, RendererShieldmaidenNorsemen::new);
        event.registerEntityRenderer(ModEntities.CAMP_NORSEMEN_ARCHER, RendererArcherNorsemen::new);
        event.registerEntityRenderer(ModEntities.CAMP_NORSEMEN_CHIEF, RendererChiefNorsemen::new);

        event.registerEntityRenderer(ModEntities.CAMP_AMAZON, RendererAmazon::new);
        event.registerEntityRenderer(ModEntities.CAMP_AMAZONCHIEF, RendererChiefAmazon::new);
        event.registerEntityRenderer(ModEntities.CAMP_AMAZONSPEARMAN, RendererAmazonSpearman::new);

        event.registerEntityRenderer(ModEntities.CAMP_DROWNED_PIRATE, RendererDrownedPirate::new);
        event.registerEntityRenderer(ModEntities.CAMP_DROWNED_ARCHERPIRATE, RendererDrownedArcherPirate::new);
        event.registerEntityRenderer(ModEntities.CAMP_DROWNED_CHIEFPIRATE, RendererDrownedChiefPirate::new);

        // Misc

        event.registerEntityRenderer(ModEntities.MERCENARY, RenderMercenary::new);
        event.registerEntityRenderer(ModEntities.SITTINGENTITY, RenderSitting::new);
        event.registerEntityRenderer(ModEntities.MINECART, (context) -> new MinecartRenderer(context, ModelLayers.MINECART));
        event.registerEntityRenderer(ModEntities.CAVALRY_HORSE, ctx -> 
        {
            HorseRenderer renderer = new HorseRenderer(ctx); 
            renderer.addLayer(new CavalryOverlayLayer(renderer));
            return renderer;
        });

        event.registerBlockEntityRenderer(MinecoloniesTileEntities.BUILDING.get(), EmptyTileEntitySpecialRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.SCARECROW.get(), TileEntityScarecrowRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.ENCHANTER.get(), TileEntityEnchanterRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.COLONY_FLAG.get(), TileEntityColonyFlagRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.NAMED_GRAVE.get(), TileEntityNamedGraveRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.DECO_CONTROLLER.get(), TileEntityDecoControllerRenderer::new);
        event.registerBlockEntityRenderer(MinecoloniesTileEntities.COLONY_SIGN.get(), TileEntityColonySignRenderer::new);

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerKeys(final RegisterKeyMappingsEvent event)
    {
        ModKeyMappings.register(event);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerSpecialModels(final RegisterSpecialModelRendererEvent event)
    {
        event.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "spear"), SpearSpecialRenderer.MAP_CODEC);
    }
}
