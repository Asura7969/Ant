package com.github.ant.utils;

import org.junit.Assert;
import org.junit.Test;

public class ParameterToolTest {

    @Test
    public void testFromCliArgs() {
        ParameterTool parameter = ParameterTool.fromArgs(new String[]{"--input", "myInput", "-expectedCount", "15", "--withoutValues",
                "--negativeFloat", "-0.58", "-isWorking", "true", "--maxByte", "127", "-negativeShort", "-1024"});
        Assert.assertTrue(parameter.has("withoutValues"));
        Assert.assertEquals(-0.58, parameter.getFloat("negativeFloat"), 0.1);
        Assert.assertTrue(parameter.getBoolean("isWorking"));
    }
}
