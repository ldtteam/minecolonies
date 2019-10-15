package com.minecolonies.api.colony.requestsystem;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.requestsystem.token.StandardToken;
import com.minecolonies.api.colony.requestsystem.token.StandardTokenFactory;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.locations.EntityLocation;
import com.minecolonies.coremod.colony.requestsystem.locations.StaticLocation;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequestFactories;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Test for the initializer of {@link IFactoryController} in {@link StandardFactoryController}
 * Uses the {@link StandardTokenFactory} to Test the {@link StandardFactoryController}
 */
public class StandardFactoryControllerTest
{
    private StandardTokenFactory factory;

    @Before
    public void setUp() throws Exception
    {
        factory = new StandardTokenFactory();
        StandardFactoryController.getInstance().registerNewFactory(factory);
    }

    @After
    public void tearDown() throws Exception
    {
        StandardFactoryController.reset();
        factory = null;
    }

    @Test
    public void testGetFactoryForInput()
    {
        final IFactory<UUID, ?> inputBasedFactory = StandardFactoryController.getInstance().getFactoryForInput(TypeConstants.UUID);
        assertNotNull(inputBasedFactory);
        //assertEquals(inputBasedFactory, factory);
    }

    @Test
    public void testGetFactoryForOutput()
    {
        final IFactory<?, StandardToken> outputBasedFactory = StandardFactoryController.getInstance().getFactoryForOutput(TypeConstants.STANDARDTOKEN);
        assertEquals(outputBasedFactory, factory);
    }

    @Test
    public void testRegisterNewFactory()
    {
        StandardFactoryController.getInstance().registerNewFactory(new StaticLocation.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new EntityLocation.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.ItemStackRequestFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.DeliveryRequestFactory());
        assertNotNull(StandardFactoryController.getInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterNewFactoryDuplicate()
    {
        StandardFactoryController.getInstance().registerNewFactory(factory);
        assertNotNull(StandardFactoryController.getInstance());
    }

    @Test
    public void testSerialize()
    {
        final StandardToken standardToken = new StandardToken(UUID.randomUUID());
        final IToken token = standardToken;

        final NBTTagCompound compound = StandardFactoryController.getInstance().serialize(token);

        assertTrue(compound.hasKey(StandardFactoryController.NBT_TYPE));
        assertTrue(compound.hasKey(StandardFactoryController.NBT_DATA));
        assertEquals(compound.getString(StandardFactoryController.NBT_TYPE), new TypeToken<StandardToken>() {}.toString());
        assertEquals(compound.getCompoundTag(StandardFactoryController.NBT_DATA).getLong(StandardTokenFactory.NBT_MSB), standardToken.getIdentifier().getMostSignificantBits());
        assertEquals(compound.getCompoundTag(StandardFactoryController.NBT_DATA).getLong(StandardTokenFactory.NBT_LSB), standardToken.getIdentifier().getLeastSignificantBits());
    }

    @Test
    public void testDeserialize()
    {
        final StandardToken standardToken = new StandardToken(UUID.randomUUID());
        final IToken token = standardToken;

        final NBTTagCompound compound = StandardFactoryController.getInstance().serialize(token);
        final IToken deserialize = StandardFactoryController.getInstance().deserialize(compound);

        assertEquals(token, deserialize);
    }

    @Test
    public void testGetNewInstance()
    {
        final UUID id = UUID.randomUUID();
        final IToken token = new StandardToken(id);

        final IToken output = StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN, id);

        assertEquals(output, token);
    }
}