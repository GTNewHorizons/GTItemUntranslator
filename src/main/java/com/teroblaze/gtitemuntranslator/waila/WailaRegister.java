package com.teroblaze.gtitemuntranslator.waila;

import net.minecraft.block.Block;

import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaRegister {

    public static void register(IWailaRegistrar registrar) {
        // Register our DataProvider for the Waila head section (the top line in Waila tooltip)
        registrar.registerHeadProvider(new DataProvider(), Block.class);
    }
}
