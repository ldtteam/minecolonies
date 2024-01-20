package com.minecolonies.api.inventory.container;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.ai.workers.guards.GuardGear;
import com.minecolonies.api.entity.ai.workers.guards.GuardGearBuilder;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.InventoryConstants.*;
import static com.minecolonies.api.util.constant.ToolLevelConstants.*;

/**
 * Container for Mie
 */
public class ContainerCitizenInventory extends AbstractContainerMenu
{
    /**
     * Player inventory.
     */
    private final Inventory playerInventory;

    /**
     * Amount of rows.
     */
    private final int              inventorySize;

    /**
     * Citizen related data.
     */
    private ICitizen citizenData;

    /**
     * Related entity.
     */
    private Optional<? extends Entity> entity = Optional.empty();
    private       String           displayName;

    /**
     * Deserialize packet buffer to container instance.
     *
     * @param windowId     the id of the window.
     * @param inv          the player inventory.
     * @param packetBuffer network buffer
     * @return new instance
     */
    public static ContainerCitizenInventory fromFriendlyByteBuf(final int windowId, final Inventory inv, final FriendlyByteBuf packetBuffer)
    {
        final int colonyId = packetBuffer.readVarInt();
        final int citizenId = packetBuffer.readVarInt();
        return new ContainerCitizenInventory(windowId, inv, colonyId, citizenId);
    }

    /**
     * Creating the citizen inventory container.
     *
     * @param windowId  the window id.
     * @param inv       the inventory.
     * @param colonyId  colony id
     * @param citizenId citizen id
     */
    public ContainerCitizenInventory(final int windowId, final Inventory inv, final int colonyId, final int citizenId)
    {
        super(ModContainers.citizenInv.get(), windowId);
        this.playerInventory = inv;

        final IColony colony;
        if (inv.player.level().isClientSide)
        {
            colony = IColonyManager.getInstance().getColonyView(colonyId, inv.player.level().dimension());
        }
        else
        {
            colony = IColonyManager.getInstance().getColonyByWorld(colonyId, inv.player.level());
        }

        if (colony == null)
        {
            inventorySize = 0;
            return;
        }

        final InventoryCitizen inventory;
        final BlockPos workBuilding;

        int workBuildingLevel = 0;
        if (inv.player.level().isClientSide)
        {
            final ICitizenDataView data = ((IColonyView) colony).getCitizen(citizenId);
            this.entity = Optional.of(inv.player.level.getEntity(data.getEntityId()));
            this.citizenData = data;
            inventory = data.getInventory();
            this.displayName = data.getName();
            workBuilding = data.getWorkBuilding();
            if (workBuilding != null)
            {
                workBuildingLevel = ((IColonyView) colony).getBuilding(workBuilding).getBuildingLevel();
            }
        }
        else
        {
            final ICitizenData data;
            if (citizenId > 0)
            {
                data = colony.getCitizenManager().getCivilian(citizenId);
            }
            else
            {
                data = colony.getVisitorManager().getCivilian(citizenId);
            }
            this.entity = data.getEntity();
            this.citizenData = data;

            inventory = data.getInventory();
            this.displayName = data.getName();
            workBuilding = data.getWorkBuilding() == null ? null : data.getWorkBuilding().getID();
            if (workBuilding != null)
            {
                workBuildingLevel = data.getWorkBuilding().getBuildingLevel();
            }
        }

        this.inventorySize = inventory.getSlots() / INVENTORY_COLUMNS;
        final int size = inventory.getSlots();

        final int columns = inventorySize <= INVENTORY_BAR_SIZE ? INVENTORY_COLUMNS : ((size / INVENTORY_BAR_SIZE) + 1);
        final int extraOffset = (inventorySize <= INVENTORY_BAR_SIZE ? 0 : 2) + 1;
        final int newOffset = 5;
        int index = 0;


        List<GuardGear> guardGear = switch (workBuildingLevel)
        {
            case 5-> GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_IRON, ARMOR_LEVEL_MAX, LEATHER_BUILDING_LEVEL_RANGE, DIA_BUILDING_LEVEL_RANGE);
            case 4-> GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_CHAIN, ARMOR_LEVEL_DIAMOND, LEATHER_BUILDING_LEVEL_RANGE, DIA_BUILDING_LEVEL_RANGE);
            case 3-> GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_IRON, LEATHER_BUILDING_LEVEL_RANGE, IRON_BUILDING_LEVEL_RANGE);
            case 2-> GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_CHAIN, LEATHER_BUILDING_LEVEL_RANGE, CHAIN_BUILDING_LEVEL_RANGE);
            case 1-> GuardGearBuilder.buildGearForLevel(ARMOR_LEVEL_LEATHER, ARMOR_LEVEL_GOLD, LEATHER_BUILDING_LEVEL_RANGE, GOLD_BUILDING_LEVEL_RANGE);
            default-> Collections.emptyList();
        };

        for (int j = 0; j < Math.min(this.inventorySize, INVENTORY_BAR_SIZE); ++j)
        {
            for (int k = 0; k < columns; ++k)
            {
                if (index < size)
                {
                    this.addSlot(
                      new SlotItemHandler(inventory, index,
                        INVENTORY_BAR_SIZE + k * PLAYER_INVENTORY_OFFSET_EACH,
                        newOffset + PLAYER_INVENTORY_OFFSET_EACH + j * PLAYER_INVENTORY_OFFSET_EACH)
                      {
                          @Override
                          public void set(@NotNull final ItemStack stack)
                          {
                              if (workBuilding != null && !playerInventory.player.level().isClientSide && !ItemStackUtils.isEmpty(stack))
                              {
                                  final IBuilding building = colony.getBuildingManager().getBuilding(workBuilding);
                                  final ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);

                                  building.overruleNextOpenRequestOfCitizenWithStack(citizenData, stack);
                              }
                              super.set(stack);
                          }
                      });
                    index++;
                }
            }
        }


        index = 3;
        for (int j = 0; j < 4; ++j)
        {
                final EquipmentSlot equipmentSlot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, index);
                    this.addSlot(
                      new Slot(new SimpleContainer(inventory.getArmorInSlot(equipmentSlot)), 0,INVENTORY_BAR_SIZE + 215,
                        23 + j * PLAYER_INVENTORY_OFFSET_EACH)
                      {
                          @Override
                          public void set(@NotNull final ItemStack stack)
                          {
                              if (workBuilding != null && !playerInventory.player.level.isClientSide && !ItemStackUtils.isEmpty(stack))
                              {
                                  final IBuilding building = colony.getBuildingManager().getBuilding(workBuilding);
                                  final ICitizenData citizenData = colony.getCitizenManager().getCivilian(citizenId);

                                  building.overruleNextOpenRequestOfCitizenWithStack(citizenData, stack);
                              }
                              super.set(stack);
                              inventory.forceArmorStackToSlot(equipmentSlot, stack);
                          }

                          @Override
                          public ItemStack remove(final int slot)
                          {
                              final ItemStack stack = inventory.getArmorInSlot(equipmentSlot);
                              inventory.forceClearArmorInSlot(equipmentSlot, stack);
                              return stack;
                          }

                          @Override
                          public boolean mayPlace(final ItemStack stack)
                          {
                              if (stack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == equipmentSlot)
                              {
                                  for (final GuardGear gear : guardGear)
                                  {
                                      if (gear.test(stack))
                                      {
                                        return true;
                                      }
                                  }
                                  return false;
                              }
                              return false;
                          }
                      });
                    index--;
        }

        // Player inventory slots
        // Note: The slot numbers are within the player inventory and may be the same as the field inventory.
        int i;
        for (i = 0; i < INVENTORY_ROWS; i++)
        {
            for (int j = 0; j < INVENTORY_COLUMNS; j++)
            {
                addSlot(new Slot(
                  playerInventory,
                  j + i * INVENTORY_COLUMNS + INVENTORY_COLUMNS,
                  PLAYER_INVENTORY_INITIAL_X_OFFSET + j * PLAYER_INVENTORY_OFFSET_EACH,
                  PLAYER_INVENTORY_INITIAL_Y_OFFSET + newOffset + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * Math.min(this.inventorySize, INVENTORY_BAR_SIZE)
                    + i * PLAYER_INVENTORY_OFFSET_EACH
                ));
            }
        }

        for (i = 0; i < INVENTORY_COLUMNS; i++)
        {
            addSlot(new Slot(
              playerInventory, i,
              PLAYER_INVENTORY_INITIAL_X_OFFSET + i * PLAYER_INVENTORY_OFFSET_EACH,
              PLAYER_INVENTORY_HOTBAR_OFFSET + newOffset + extraOffset + PLAYER_INVENTORY_OFFSET_EACH * Math.min(this.inventorySize,
                INVENTORY_BAR_SIZE)
            ));
        }
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player inventory and the other inventory(s).
     *
     * @param playerIn Player that interacted with this {@code Container}.
     * @param index    Index of the {@link Slot}. This index is relative to the list of slots in this {@code Container}, {@link #slots}.
     */
    @NotNull
    @Override
    public ItemStack quickMoveStack(final Player playerIn, final int index)
    {
        final Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem())
        {
            return ItemStackUtils.EMPTY;
        }

        final ItemStack stackCopy = slot.getItem().copy();

        final int maxIndex = this.inventorySize * INVENTORY_COLUMNS;

        if (index < maxIndex)
        {
            if (!this.moveItemStackTo(stackCopy, maxIndex, this.slots.size(), true))
            {
                return ItemStackUtils.EMPTY;
            }
        }
        else if (!this.moveItemStackTo(stackCopy, 0, maxIndex, false))
        {
            return ItemStackUtils.EMPTY;
        }

        if (ItemStackUtils.getSize(stackCopy) == 0)
        {
            slot.set(ItemStackUtils.EMPTY);
        }
        else
        {
            slot.set(stackCopy);
        }

        return stackCopy;
    }

    /**
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean stillValid(@NotNull final Player playerIn)
    {
        return true;
    }

    /**
     * Getter for the display name.
     *
     * @return the display name.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Get the entity of this container.
     * @return the entity.
     */
    public Optional<? extends Entity> getEntity()
    {
        return this.entity;
    }
}