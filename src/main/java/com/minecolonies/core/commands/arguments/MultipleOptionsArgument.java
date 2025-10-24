package com.minecolonies.core.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class MultipleOptionsArgument<TValue> implements ArgumentType<MultipleOptionsArgument.OptionContainer<TValue>>
{
    public record OptionContainer<TValue>(
        ArgumentOption<TValue> option,
        String value)
    {}

    private final List<ArgumentOption<TValue>> allowedOptions;

    protected MultipleOptionsArgument(final List<ArgumentOption<TValue>> allowedOptions)
    {
        this.allowedOptions = allowedOptions;
    }

    @SuppressWarnings("unchecked")
    protected static <TValue> TValue getValue(
        final CommandContext<CommandSourceStack> context,
        final String name) throws CommandSyntaxException
    {
        final OptionContainer<TValue> container = context.getArgument(name, OptionContainer.class);
        return container.option.resolveValue(context.getSource(), container.value);
    }

    @Override
    public OptionContainer<TValue> parse(final StringReader reader) throws CommandSyntaxException
    {
        if (reader.canRead())
        {
            final String argumentValue = reader.readString();
            for (final ArgumentOption<TValue> allowedOption : allowedOptions)
            {
                if (allowedOption.matches(argumentValue))
                {
                    return new OptionContainer<>(allowedOption, argumentValue);
                }
            }
            throw EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(reader, argumentValue);
        }

        throw EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE.createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        if (context.getSource() instanceof final CommandSourceStack source)
        {
            for (final ArgumentOption<TValue> allowedOption : allowedOptions)
            {
                allowedOption.createSuggestions(source.getLevel(), source, builder);
            }
        }
        else if (context.getSource() instanceof ClientSuggestionProvider suggestionProvider)
        {
            for (final ArgumentOption<TValue> allowedOption : allowedOptions)
            {
                allowedOption.createSuggestions(Minecraft.getInstance().level, suggestionProvider, builder);
            }
        }

        return builder.buildFuture();
    }

    public interface ArgumentOption<TValue>
    {
        boolean matches(final String value);

        TValue resolveValue(final CommandSourceStack source, final String value) throws CommandSyntaxException;

        void createSuggestions(final Level world, final SharedSuggestionProvider suggestionProvider, final SuggestionsBuilder builder);
    }
}
