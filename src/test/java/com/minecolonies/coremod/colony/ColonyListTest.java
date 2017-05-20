package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.test.ReflectionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests around {@link ColonyList}.
 * <p>Created by Colton on 2/28/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ColonyListTest
{
    @Mock
    private Colony colony1;

    @Mock
    private Colony colony1Copy;

    @Mock
    private Colony colony2;

    @Mock
    private WorldProvider worldProvider;

    @Mock
    private World world;

    @Mock
    private EventBus eventBus;

    private ColonyList<Colony> list;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException
    {
        list = new ColonyList<>();

        when(colony1.getID()).thenReturn(1);
        when(colony1Copy.getID()).thenReturn(1);
        when(colony2.getID()).thenReturn(2);

        when(worldProvider.getDimension()).thenReturn(1);
        ReflectionUtil.setFinalField(world, "provider", worldProvider);
        ReflectionUtil.setStaticFinalField(MinecraftForge.class, "EVENT_BUS", eventBus);
    }

    @Test
    public void testGet()
    {
        list.add(colony1);

        assertEquals(colony1, list.get(1));
    }

    @Test
    public void testGetIndexOutOfBounds()
    {
        assertNull(list.get(-1));
    }

    @Test
    public void testGetNull()
    {
        assertNull(list.get(1));
    }

    @Test
    public void testCreate()
    {
        Colony colony = list.create(world, BlockPos.ORIGIN);

        assertEquals(colony, list.get(colony.getID()));
    }

    @Test
    public void testExpandList()
    {
        for (int i = 0; i < ColonyList.INITIAL_SIZE + 1; i++)
        {
            Colony colony = list.create(world, BlockPos.ORIGIN);
            assertEquals(colony, list.get(colony.getID()));
        }

        assertEquals(ColonyList.INITIAL_SIZE + 1, list.size());
    }

    @Test
    public void testAdd()
    {
        list.add(colony1);

        assertEquals(colony1, list.get(1));
        assertEquals(1, list.size());
    }

    @Test
    public void testAddMultiple()
    {
        list.add(colony1);
        list.add(colony2);

        assertEquals(colony1, list.get(1));
        assertEquals(colony2, list.get(2));
    }

    @Test
    public void testAddSame()
    {
        list.add(colony1);
        list.add(colony1);

        assertEquals(colony1, list.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddConflict()
    {
        list.add(colony1);
        list.add(colony1Copy);
    }

    @Test
    public void testAddHighId()
    {
        Colony colony = mock(Colony.class);
        int id = 10 * ColonyList.INITIAL_SIZE;
        when(colony.getID()).thenReturn(id);
        list.add(colony);

        assertEquals(1, list.size());
        assertEquals(colony, list.get(id));
    }

    @Test
    public void testRemoveType()
    {
        list.add(colony1);
        list.remove(colony1);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testRemoveId()
    {
        list.add(colony1);
        list.remove(1);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testIdReAdd()
    {
        list.add(colony1);
        list.remove(colony1);

        assertTrue(list.isEmpty());

        list.add(colony1);

        assertEquals(1, list.size());
        assertEquals(colony1, list.get(1));
    }

    @Test
    public void testIdReuse()
    {
        list.add(colony1);
        list.remove(colony1);

        Colony colony = list.create(world, BlockPos.ORIGIN);

        assertEquals(1, colony.getID());
    }

    @Test
    public void testClear()
    {
        list.add(colony1);
        list.add(colony2);

        list.clear();

        assertTrue(list.isEmpty());
    }

    @Test
    public void testSize()
    {
        assertEquals(0, list.size());
        list.add(colony1);
        list.add(colony2);

        assertEquals(2, list.size());
    }

    @Test
    public void testIsEmpty()
    {
        assertTrue(list.isEmpty());
        list.add(colony1);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testGetCopyAsList()
    {
        list.add(colony1);
        list.add(colony2);

        List<Colony> copy = list.getCopyAsList();

        assertEquals(list.size(), copy.size());
        assertEquals(1, copy.get(0).getID());
        assertEquals(2, copy.get(1).getID());
    }

    @Test
    public void testIterator()
    {
        list.add(colony1);
        list.add(colony2);

        Iterator<Colony> itr = list.iterator();

        int count = 0;

        while (itr.hasNext())
        {
            count++;
            itr.next();
        }

        assertEquals(list.size(), count);
    }
}
