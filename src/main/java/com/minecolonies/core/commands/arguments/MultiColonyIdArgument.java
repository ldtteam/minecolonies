package com.minecolonies.core.commands.arguments;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A command argument that can dynamically resolve to a colony id.
 */
public class MultiColonyIdArgument extends MultipleOptionsArgument<List<Integer>>
{
    private MultiColonyIdArgument()
    {
        super(List.of(new AllOption(),
            WrappedSingleClass.of(ColonyIdArgument.HereOption::new),
            WrappedSingleClass.of(ColonyIdArgument.MineOption::new),
            WrappedSingleClass.of(ColonyIdArgument.ColonyIdOption::new),
            WrappedSingleClass.of(ColonyIdArgument.PlayerUuidOption::new),
            WrappedSingleClass.of(ColonyIdArgument.PlayerNameOption::new)));
    }

    public static MultiColonyIdArgument id()
    {
        return new MultiColonyIdArgument();
    }

    /**
     * Resolve the actual argument value into a list of colonies.
     *
     * @param context the command context.
     * @param name    the argument name.
     * @return the list of colonies.
     */
    @NotNull
    public static List<IColony> getColonies(@NotNull final CommandContext<CommandSourceStack> context, @NotNull final String name)
    {
        final List<Integer> colonyIds = getColonyIds(context, name);
        final List<IColony> colonies = new ArrayList<>();

        for (final int colonyId : colonyIds)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, context.getSource().getLevel());
            if (colony != null)
            {
                colonies.add(colony);
            }
        }

        return colonies;
    }

    /**
     * Resolve the actual argument value into a list of colony ids.
     *
     * @param context the command context.
     * @param name    the argument name.
     * @return the list of colony ids.
     */
    @NotNull
    public static List<Integer> getColonyIds(@NotNull final CommandContext<CommandSourceStack> context, @NotNull final String name)
    {
        try
        {
            return getValue(context, name);
        }
        catch (CommandSyntaxException e)
        {
            final Component message = ComponentUtils.fromMessage(e.getRawMessage());
            context.getSource().sendFailure(message);
            throw new RuntimeException(message.getString());
        }
    }

    public static class AllOption implements ArgumentOption<List<Integer>>
    {
        @Override
        public boolean matches(final String value)
        {
            return Objects.equals(value, "@all");
        }

        @Override
        public List<Integer> resolveValue(final CommandSourceStack source, final String value)
        {
            return IColonyManager.getInstance().getAllColonies().stream().map(IColony::getID).toList();
        }

        @Override
        public void createSuggestions(final Level world, final SharedSuggestionProvider suggestionProvider, final SuggestionsBuilder builder)
        {
            builder.suggest("@all", Component.translatable("com.minecolonies.command.argument.colony.all"));
        }
    }

    private record WrappedSingleClass(ArgumentOption<Integer> wrapped) implements ArgumentOption<List<Integer>>
    {
        private static WrappedSingleClass of(final Supplier<ArgumentOption<Integer>> wrapped)
        {
            return new WrappedSingleClass(wrapped.get());
        }

        @Override
        public boolean matches(final String value)
        {
            return wrapped.matches(value);
        }

        @Override
        public List<Integer> resolveValue(final CommandSourceStack source, final String value) throws CommandSyntaxException
        {
            return List.of(wrapped.resolveValue(source, value));
        }

        @Override
        public void createSuggestions(final Level world, final SharedSuggestionProvider suggestionProvider, final SuggestionsBuilder builder)
        {
            wrapped.createSuggestions(world, suggestionProvider, builder);
        }
    }
}
