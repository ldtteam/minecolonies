package com.minecolonies.core.commands.generalcommands;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.BlockPosUtil;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.commands.AbstractCommand;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.ScanToolData;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.core.network.messages.client.SaveStructureNBTMessage;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.constants.Constants.MOD_ID;
import static com.ldtteam.structurize.api.constants.TranslationConstants.ANCHOR_POS_OUTSIDE_SCHEMATIC;
import static com.ldtteam.structurize.api.constants.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;
import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;

/**
 * Command for opening WindowScanTool or scanning a structure into a file
 */
public class ScanCommand extends AbstractCommand
{
    /**
     * Descriptive string.
     */
    public static final String NAME = "scan";

    /**
     * The player not found for a scan message.
     */
    private static final String PLAYER_NOT_FOUND = "com.structurize.command.playernotfound";

    /**
     * The scan success message.
     */
    private static final String SCAN_SUCCESS_MESSAGE = "com.structurize.command.scan.success";

    /**
     * The no permission message reply.
     */
    private static final String NO_PERMISSION_MESSAGE = "com.structurize.command.scan.no.perm";

    /**
     * The filename command argument.
     */
    public static final String FILE_NAME = "filename";

    /**
     * The player name command argument.
     */
    public static final String PLAYER_NAME = "player";

    /**
     * Position 1 command argument.
     */
    public static final String POS1 = "pos1";

    /**
     * Position 2 command argument.
     */
    public static final String POS2 = "pos2";

    /**
     * Anchor position command argument.
     */
    public static final String ANCHOR_POS = "anchor_pos";

    /**
     * Scan the structure and save it to the disk.
     *
     * @param world        Current world.
     * @param player       causing this action.
     * @param slot         the scan data.
     * @param saveEntities whether to scan in entities
     */
    public static void saveStructure(
      final Level world,
      final Player player,
      final ScanToolData.Slot slot,
      final boolean saveEntities)
    {
        if (slot.getBox().getAnchor().isPresent())
        {
            if (!BlockPosUtil.isInbetween(slot.getBox().getAnchor().get(), slot.getBox().getPos1(), slot.getBox().getPos2()))
            {
                player.displayClientMessage(Component.translatable(ANCHOR_POS_OUTSIDE_SCHEMATIC), false);
                return;
            }
        }

        final BoundingBox box = BoundingBox.fromCorners(slot.getBox().getPos1(), slot.getBox().getPos2());
        if (box.getXSpan() * box.getYSpan() * box.getZSpan() > Structurize.getConfig().getServer().schematicBlockLimit.get())
        {
            player.displayClientMessage(Component.translatable(MAX_SCHEMATIC_SIZE_REACHED, Structurize.getConfig().getServer().schematicBlockLimit.get()), false);
            return;
        }

        final long currentMillis = System.currentTimeMillis();
        final String currentMillisString = Long.toString(currentMillis);
        String fileName;
        if (slot.getName().isEmpty())
        {
            fileName = Component.translatable("item.sceptersteel.scanformat", "", currentMillisString).getString();
        }
        else
        {
            fileName = slot.getName();
        }

        if (!fileName.contains(".blueprint"))
        {
            fileName+= ".blueprint";
        }

        final String[] split = fileName.split("/");
        final String style = split.length <= 1 ? "" : split[0];

        final BlockPos zero = new BlockPos(box.minX(), box.minY(), box.minZ());
        final Blueprint
          bp = BlueprintUtil.createBlueprint(world, zero, saveEntities, (short) box.getXSpan(), (short) box.getYSpan(), (short) box.getZSpan(), fileName, slot.getBox().getAnchor());

        if (slot.getBox().getAnchor().isEmpty() && bp.getPrimaryBlockOffset().equals(new BlockPos(bp.getSizeX() / 2, 0, bp.getSizeZ() / 2)))
        {
            final List<BlockInfo> list = bp.getBlockInfoAsList().stream()
                                           .filter(blockInfo -> blockInfo.hasTileEntityData() && blockInfo.getTileEntityData().contains(TAG_BLUEPRINTDATA))
                                           .collect(Collectors.toList());

            if (list.size() > 1)
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.scanbadanchor", fileName), false);
            }
        }

        new SaveScanMessage(BlueprintUtil.writeBlueprintToNBT(bp), fileName.toLowerCase(Locale.US)).sendToPlayer((ServerPlayer) player);
        if (style.isEmpty())
        {
            return;
        }

        boolean hasJigsaw = false;
        for (BlockState state : bp.getPalette())
        {
            if (state.getBlock() == Blocks.JIGSAW)
            {
                hasJigsaw = true;
                break;
            }
        }

        if (!hasJigsaw)
        {
            return;
        }

        final BlockState primary = world.getBlockState(zero.offset(bp.getPrimaryBlockOffset()));
        final boolean isHut;
        if (primary.getBlock() instanceof AbstractBlockHut<?>)
        {
            isHut = true;
            final AbstractTileEntityColonyBuilding building = (AbstractTileEntityColonyBuilding) world.getBlockEntity(zero.offset(bp.getPrimaryBlockOffset()));
            building.addTag(new BlockPos(0, 0, 0), "deactivated");
            building.setPackName(style);
            building.setBlueprintPath(fileName.replace( style + "/", ""));
        }
        else
        {
            isHut = false;
        }

        int lowestY = box.maxY();
        final String piecesName = style.replace(" ", "").toLowerCase(Locale.US);
        for (final BlockPos mutablePos : BlockPos.betweenClosed(zero, zero.offset(box.getXSpan() - 1, box.getYSpan() - 1, box.getZSpan() - 1)))
        {
            BlockState state = world.getBlockState(mutablePos);
            if (state.getBlock() == Blocks.JIGSAW)
            {
                if (mutablePos.getY() < lowestY)
                {
                    lowestY = mutablePos.getY();
                }
                JigsawBlockEntity jigsawBlockEntity = (JigsawBlockEntity) world.getBlockEntity(mutablePos);
                if (jigsawBlockEntity.getName().getPath().isEmpty() || jigsawBlockEntity.getName().getPath().equals("empty"))
                {
                    if (jigsawBlockEntity.getFinalState().equals("minecraft:air"))
                    {
                        jigsawBlockEntity.setFinalState("minecraft:structure_void");
                    }
                    if (isHut)
                    {
                        jigsawBlockEntity.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation("minecolonies:" + piecesName+ "/roads")));
                        jigsawBlockEntity.setName(new ResourceLocation("minecolonies:building_entrance"));
                        jigsawBlockEntity.setTarget(new ResourceLocation("minecolonies:building_entrance"));
                    }
                    else if (jigsawBlockEntity.getPool().location().getPath().contains("building"))
                    {
                        jigsawBlockEntity.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation("minecolonies:" + piecesName+ "/buildings")));
                        jigsawBlockEntity.setName(new ResourceLocation("minecolonies:building_entrance"));
                        jigsawBlockEntity.setTarget(new ResourceLocation("minecolonies:building_entrance"));
                    }
                    else
                    {
                        jigsawBlockEntity.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation("minecolonies:" + piecesName+ "/roads")));
                        jigsawBlockEntity.setName(new ResourceLocation("minecolonies:street"));
                        jigsawBlockEntity.setTarget(new ResourceLocation("minecolonies:street"));
                    }
                }
                jigsawBlockEntity.setChanged();
                world.sendBlockUpdated(mutablePos, state, state, 3);
            }
        }

        final StructureTemplateManager structuretemplatemanager = ((ServerLevel) world).getStructureManager();
        final BlockPos newZero = new BlockPos(zero.getX(), Math.max(zero.getY(), lowestY - 1), zero.getZ());
        final int yDif = newZero.getY() - zero.getY();
        StructureTemplate structuretemplate;
        try
        {
            final ResourceLocation location = new ResourceLocation(Constants.MOD_ID, fileName.replace(".blueprint", "").replace(" ", "").toLowerCase(Locale.US));
            structuretemplate = structuretemplatemanager.getOrCreate(location);
            structuretemplate.fillFromWorld(world, newZero, new BlockPos(box.getXSpan(), box.getYSpan() - yDif, box.getZSpan()), false, Blocks.STRUCTURE_VOID);
            new SaveStructureNBTMessage(structuretemplate.save(new CompoundTag()), fileName.replace(".blueprint", ".nbt").toLowerCase(Locale.US)).sendToPlayer((ServerPlayer) player);
        }
        catch (final ResourceLocationException resLocEx)
        {
            Log.getLogger().warn("Couldnt save nbt.");
        }

        if (isHut)
        {
            final AbstractTileEntityColonyBuilding building = (AbstractTileEntityColonyBuilding) world.getBlockEntity(zero.offset(bp.getPrimaryBlockOffset()));
            building.removeTag(new BlockPos(0, 0, 0), "deactivated");
        }
    }

    private static int execute(final CommandSourceStack source, final BlockPos from, final BlockPos to, final Optional<BlockPos> anchorPos, final GameProfile profile, final String name) throws CommandSyntaxException
    {
        @Nullable final Level world = source.getLevel();
        if (source.getEntity() instanceof Player && !source.getPlayerOrException().isCreative())
        {
            source.sendFailure(Component.literal(NO_PERMISSION_MESSAGE));
        }

        final Player player;
        if (profile != null && world.getServer() != null)
        {
            player = world.getServer().getPlayerList().getPlayer(profile.getId());
            if (player == null)
            {
                source.sendFailure(Component.translatable(PLAYER_NOT_FOUND, profile.getName()));
                return 0;
            }
        } 
        else if (source.getEntity() instanceof Player)
        {
            player = source.getPlayerOrException();
        } 
        else
        {
            source.sendFailure(Component.translatable(PLAYER_NOT_FOUND));
            return 0;
        }


        saveStructure(world, player, new ScanToolData.Slot(name, new BoxPreviewData(from, to, anchorPos)), true);
        source.sendFailure(Component.translatable(SCAN_SUCCESS_MESSAGE));
        return 1;
    }

    private static int onExecute(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getSpawnablePos(context, POS1);
        final BlockPos to = BlockPosArgument.getSpawnablePos(context, POS2);
        return execute(context.getSource(), from, to, Optional.empty(), null, null);
    }

    private static int onExecuteWithAnchor(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getSpawnablePos(context, POS1);
        final BlockPos to = BlockPosArgument.getSpawnablePos(context, POS2);
        final BlockPos anchorPos = BlockPosArgument.getSpawnablePos(context, ANCHOR_POS);
        return execute(context.getSource(), from, to, Optional.of(anchorPos), null, null);
    }

    private static int onExecuteWithPlayerName(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getSpawnablePos(context, POS1);
        final BlockPos to = BlockPosArgument.getSpawnablePos(context, POS2);
        GameProfile profile = GameProfileArgument.getGameProfiles(context, PLAYER_NAME).stream().findFirst().orElse(null);
        return execute(context.getSource(), from, to, Optional.empty(), profile, null);
    }

    private static int onExecuteWithPlayerNameAndFileName(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getSpawnablePos(context, POS1);
        final BlockPos to = BlockPosArgument.getSpawnablePos(context, POS2);
        GameProfile profile = GameProfileArgument.getGameProfiles(context, PLAYER_NAME).stream().findFirst().orElse(null);
        String name = StringArgumentType.getString(context, FILE_NAME);
        return execute(context.getSource(), from, to, Optional.empty(), profile, name);
    }

    private static int onExecuteWithPlayerNameAndFileNameAndAnchorPos(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getSpawnablePos(context, POS1);
        final BlockPos to = BlockPosArgument.getSpawnablePos(context, POS2);
        final BlockPos anchorPos = BlockPosArgument.getSpawnablePos(context, ANCHOR_POS);
        GameProfile profile = GameProfileArgument.getGameProfiles(context, PLAYER_NAME).stream().findFirst().orElse(null);
        String name = StringArgumentType.getString(context, FILE_NAME);
        return execute(context.getSource(), from, to, Optional.of(anchorPos), profile, name);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return newLiteral(NAME)
                .then(newArgument(POS1, BlockPosArgument.blockPos())
                        .then(newArgument(POS2, BlockPosArgument.blockPos())
                                .executes(ScanCommand::onExecute)
                                .then(newArgument(ANCHOR_POS, BlockPosArgument.blockPos())
                                        .executes(ScanCommand::onExecuteWithAnchor))
                                .then(newArgument(PLAYER_NAME, GameProfileArgument.gameProfile())
                                        .executes(ScanCommand::onExecuteWithPlayerName)
                                        .then(newArgument(FILE_NAME, StringArgumentType.string())
                                                .executes(ScanCommand::onExecuteWithPlayerNameAndFileName)
                                                .then(newArgument(ANCHOR_POS, BlockPosArgument.blockPos())
                                                        .executes(ScanCommand::onExecuteWithPlayerNameAndFileNameAndAnchorPos))))));
    }

    /**
     * Generates a command string for the given parameters.
     *
     * @param slot The scan slot data.
     * @return The command string.
     */
    @NotNull
    public static String format(@NotNull final ScanToolData.Slot slot)
    {
        final String name = slot.getName().chars().anyMatch(c -> !StringReader.isAllowedInUnquotedString((char)c))
                ? StringTag.quoteAndEscape(slot.getName()) : slot.getName();

        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("/%s %s %s %s @p %s", MOD_ID, NAME,
                BlockPosUtil.format(slot.getBox().getPos1()),
                BlockPosUtil.format(slot.getBox().getPos2()),
                name));
        if (slot.getBox().getAnchor().isPresent() && BlockPosUtil.isInbetween(slot.getBox().getAnchor().get(), slot.getBox().getPos1(), slot.getBox().getPos2()))
        {
            builder.append(' ');
            builder.append(BlockPosUtil.format(slot.getBox().getAnchor().get()));
        }
        return builder.toString();
    }
}
