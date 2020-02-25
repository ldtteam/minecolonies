package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingDeliveryman;
import com.minecolonies.api.colony.buildings.workerbuildings.IWareHouse;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.colony.jobs.JobFarmer;
import com.minecolonies.coremod.colony.jobs.JobFisherman;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;

import static com.minecolonies.api.util.ItemStackUtils.ISCOOKABLE;
import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.ItemStackUtils.IS_COMPOST;
import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;
import static com.minecolonies.api.util.constant.HappinessConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_RESTAURANT;
import static com.minecolonies.coremod.entity.ai.citizen.smelter.EntityAIWorkSmelter.ORE_LIST;
import static com.minecolonies.coremod.util.WorkerUtil.getLastLadder;
import static com.minecolonies.coremod.util.WorkerUtil.isThereCompostedLand;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Class containing initializer for all the validator predicates.
 */
public class InteractionValidatorInitializer
{
    /**
     * Init method called on startup.
     */
    public static void init()
    {
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(FURNACE_USER_NO_FUEL),
          citizen -> citizen.getWorkBuilding() instanceof AbstractBuildingSmelterCrafter && ((AbstractBuildingSmelterCrafter) citizen.getWorkBuilding()).getCopyOfAllowedItems().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE),
          citizen -> citizen.getWorkBuilding() instanceof AbstractBuildingSmelterCrafter && ((AbstractBuildingSmelterCrafter) citizen.getWorkBuilding()).getFurnaces().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(RAW_FOOD),
          citizen -> InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISCOOKABLE) > 0 && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISFOOD) == 0);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_RESTAURANT),
          citizen -> citizen.getColony() != null && citizen.getSaturation() <= LOW_SATURATION && citizen.getCitizenEntity().isPresent() && citizen.getColony().getBuildingManager().getBestRestaurant(citizen.getCitizenEntity().get()) == null && InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(citizen.getInventory(), ISFOOD) == 0);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_HOSPITAL),
          citizen -> citizen.getColony() != null && citizen.getCitizenEntity().isPresent() && citizen.getCitizenEntity().get().getCitizenDiseaseHandler().isSick() && citizen.getColony().getBuildingManager().getBestHospital(citizen.getCitizenEntity().get()) == null );

        InteractionValidatorRegistry.registerPosBasedPredicate(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_CHESTFULL),
          (citizen, pos) ->
          {
              if (citizen.getJob() instanceof JobDeliveryman)
              {
                  final IColony colony = citizen.getColony();
                  if (colony != null)
                  {
                      final IBuilding building = colony.getBuildingManager().getBuilding(pos);
                      if (building != null)
                      {
                          final IItemHandler inv = building.getCapability(ITEM_HANDLER_CAPABILITY, null).orElseGet(null);
                          if (inv != null)
                          {
                              return InventoryUtils.openSlotCount(inv) > 0;
                          }
                      }
                  }
              }
              return false;
          });
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_JOB_DELIVERYMAN_NOWAREHOUSE),
          cit -> {
              if (cit.getJob() instanceof JobDeliveryman && cit.getWorkBuilding() != null)
              {
                  for (final IWareHouse wareHouse : cit.getJob().getColony().getBuildingManager().getWareHouses())
                  {
                      if (wareHouse.registerWithWareHouse((IBuildingDeliveryman) cit.getWorkBuilding()))
                      {
                          return false;
                      }
                  }
              }
              return true;
          });

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_FREE_FIELDS),
          citizen -> citizen.getWorkBuilding() instanceof BuildingFarmer && ((BuildingFarmer) citizen.getWorkBuilding()).hasNoFields());
        InteractionValidatorRegistry.registerPosBasedPredicate(new TranslationTextComponent(NO_SEED_SET),
          (citizen, pos) ->
          {
              if (citizen.getJob() instanceof JobFarmer)
              {
                  final IColony colony = citizen.getColony();
                  if (colony != null)
                  {
                      final World world = colony.getWorld();
                      if (world != null)
                      {
                          final TileEntity tileEntity = world.getTileEntity(pos);
                          if (tileEntity instanceof ScarecrowTileEntity)
                          {
                              return ((ScarecrowTileEntity) tileEntity).getSeed() == null;
                          }
                      }
                  }
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(BAKER_HAS_NO_RECIPES),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBaker && ((BuildingBaker) citizen.getWorkBuilding()).getCopyOfAllowedItems().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE),
          citizen -> citizen.getWorkBuilding() instanceof BuildingBaker && ((BuildingBaker) citizen.getWorkBuilding()).getFurnaces().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ENTITY_WORKER_INVENTORYFULLCHEST), citizen -> citizen.getWorkBuilding() != null && InventoryUtils.isProviderFull(citizen.getWorkBuilding()));
        InteractionValidatorRegistry.registerPosBasedPredicate(
          new TranslationTextComponent(BUILDING_LEVEL_TOO_LOW), (citizen, pos) ->
          {
              final IBuildingWorker workBuilding = citizen.getWorkBuilding();
              if (workBuilding != null)
              {
                  final IColony colony = citizen.getColony();
                  if ( colony != null )
                  {
                      final World world = colony.getWorld();
                      if ( world != null )
                      {
                          return workBuilding.getMaxToolLevel() < WorkerUtil.getCorrectHavestLevelForBlock(world.getBlockState(pos).getBlock());
                      }
                  }
              }
              return false;
          });
        InteractionValidatorRegistry.registerTokenBasedPredicate(new TranslationTextComponent(NORMAL_REQUEST),
          (citizen, token) -> {

              final IColony colony = citizen.getColony();
              if (colony != null)
              {
                  final IRequestResolver<?> resolver = citizen.getColony().getRequestManager().getRequestForToken(token) == null ? null : citizen.getColony().getRequestManager().getResolverForRequest(token);
                  return resolver instanceof IPlayerRequestResolver || resolver instanceof IRetryingRequestResolver;
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(FURNACE_USER_NO_ORE),
          citizen -> citizen.getWorkBuilding() instanceof BuildingSmeltery && IColonyManager.getInstance().getCompatibilityManager()
                                                                                .getSmeltableOres()
                                                                                .stream()
                                                                                .anyMatch(storage -> !((BuildingSmeltery) citizen.getWorkBuilding()).getCopyOfAllowedItems().getOrDefault(ORE_LIST, new ArrayList<>()).contains(storage)));

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(WATER_TOO_FAR),
          citizen -> citizen.getJob() instanceof JobFisherman && ((JobFisherman) citizen.getJob()).getPonds().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(FURNACE_USER_NO_FUEL),
          citizen -> citizen.getWorkBuilding() instanceof AbstractBuildingFurnaceUser && ((AbstractBuildingFurnaceUser) citizen.getWorkBuilding()).getAllowedFuel().isEmpty());
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(BAKER_HAS_NO_FURNACES_MESSAGE),
          citizen -> citizen.getWorkBuilding() instanceof AbstractBuildingFurnaceUser && ((AbstractBuildingFurnaceUser) citizen.getWorkBuilding()).getFurnaces().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_WORKERS_TO_DRAIN_SET),
          citizen -> citizen.getWorkBuilding() instanceof BuildingEnchanter && ((BuildingEnchanter) citizen.getWorkBuilding()).getBuildingsToGatherFrom().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_PLANT_GROUND_FLORIST),
          citizen -> citizen.getWorkBuilding() instanceof BuildingFlorist && ((BuildingFlorist) citizen.getWorkBuilding()).getPlantGround().isEmpty());

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_FLOWERS_IN_CONFIG),
          citizen -> citizen.getWorkBuilding() instanceof BuildingFlorist && ItemStackUtils.isEmpty(((BuildingFlorist) citizen.getWorkBuilding()).getFlowerToGrow()));

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_COMPOST),
          citizen ->
          {
              final IBuildingWorker buildingFlorist = citizen.getWorkBuilding();
              if (buildingFlorist instanceof BuildingFlorist && buildingFlorist.getColony().getWorld() != null)
              {
                  return InventoryUtils.getItemCountInItemHandler(citizen.getInventory(), IS_COMPOST) == 0 && !isThereCompostedLand((BuildingFlorist) buildingFlorist, buildingFlorist.getColony().getWorld());
              }
              return false;
          });


        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NEEDS_BETTER_HUT),
          citizen -> {

              final AbstractBuilding buildingMiner = (AbstractBuilding) citizen.getWorkBuilding();
              if (buildingMiner instanceof BuildingMiner && citizen.getColony() != null && citizen.getColony().getWorld() != null)
              {
                  return getLastLadder(((BuildingMiner) buildingMiner).getLadderLocation(), citizen.getColony().getWorld()) < ((BuildingMiner) buildingMiner).getDepthLimit() && ((BuildingMiner) buildingMiner).getNumberOfLevels() == 0;
              }
              return false;
          });

        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(DEMANDS_HOUSE), citizen -> ( citizen.getCitizenHappinessHandler()).getNumberOfDaysWithoutHouse() > DEMANDS_DAYS_WITHOUT_HOUSE);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_HOUSE),
          citizen -> ( citizen.getCitizenHappinessHandler()).getNumberOfDaysWithoutHouse() > COMPLAIN_DAYS_WITHOUT_HOUSE && ( citizen.getCitizenHappinessHandler()).getNumberOfDaysWithoutHouse() <= DEMANDS_DAYS_WITHOUT_HOUSE );
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(DEMANDS_JOB), citizen -> ( citizen.getCitizenHappinessHandler()).getNumberOfDaysWithoutJob() > DEMANDS_DAYS_WITHOUT_JOB);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_JOB),
          citizen -> ( citizen.getCitizenHappinessHandler()).getNumberOfDaysWithoutJob() > COMPLAIN_DAYS_WITHOUT_JOB && ( citizen.getCitizenHappinessHandler()).getNumberOfDaysWithoutJob() <= DEMANDS_DAYS_WITHOUT_JOB);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(DEMANDS_TOOL), citizen -> ( citizen.getCitizenHappinessHandler()).getMaxOpenToolDays() > NO_TOOLS_DEMANDS_DAYS);
        InteractionValidatorRegistry.registerStandardPredicate(new TranslationTextComponent(NO_TOOL), citizen -> ( citizen.getCitizenHappinessHandler()).getMaxOpenToolDays() > NO_TOOLS_COMPLAINS_DAYS);
    }
}
