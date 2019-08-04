package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenuState;
import com.minecolonies.coremod.commands.IActionCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Created by asie on 2/16/17.
 */
public class LootGenCommand extends AbstractSingleCommand implements IActionCommand
{
    public static final String DESC = "lootGen";
    public static final String TADA = "TADA! Well done you just got yourself a nice treat!";
    private static final String NO_PERMISSION_MESSAGE  = "You do not have permission to generate this sweet loot!";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public LootGenCommand()
    {
        super();
    }

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public LootGenCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
    {
        final String building = actionMenuState.getStringForArgument("building");
        final boolean paste = actionMenuState.getBooleanValueForArgument("paste", false);
        final int level = actionMenuState.getIntValueForArgument("level", 0);

        if (isPlayerOpped(sender))
        {
            server.addScheduledTask(() ->
            {
                final Item item = Item.getByNameOrId(Constants.MOD_ID + ":" + building);
                final CompoundNBT compound = new CompoundNBT();
                final StringBuilder nameString = new StringBuilder();
                if (paste)
                {
                    compound.putBoolean(TAG_PASTEABLE, true);
                    nameString.append("Insta ");
                }
                nameString.append(building.replace("blockHut", ""));
                if (level > 0)
                {
                    nameString.append(" Level: ").append(level);
                    compound.putInt(TAG_OTHER_LEVEL, level);
                }

                final CompoundNBT nameCompound = new CompoundNBT();
                nameCompound.putString(TAG_STRING_NAME, nameString.toString());
                compound.put(TAG_DISPLAY, nameCompound);

                if (sender instanceof ServerPlayerEntity && item != null)
                {
                    final ItemStack stack = new ItemStack(item, 1);
                    stack.put(compound);
                    ((ServerPlayerEntity) sender).inventory.addItemStackToInventory(stack);
                }
            });
        }
        else
        {
            sender.sendMessage(new StringTextComponent(NO_PERMISSION_MESSAGE));
        }
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final String... args) throws CommandException
    {

    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final CommandSource sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
