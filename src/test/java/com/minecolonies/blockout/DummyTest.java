package com.ldtteam.blockout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;

//TODO improve this test: check the value is correct, better edge cases
@RunWith(MockitoJUnitRunner.class)
public class DummyTest
{
    @Test
    public void ExecuteDummyTest()
    {
        assertThat("Dummy test", true);
    }
}