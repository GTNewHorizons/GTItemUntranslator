package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.client.handler.ClientTickHandlerFix;
import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorTESRFix;
import com.mrteroblaze.travelanchorfix.proxy.CommonProxy;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    public void init() {
        // Bind our TESR to Travel Anchor
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());

        // Register tick handler (both FML bus and Forge bus for safety in GTNH)
        ClientTickHandlerFix tickHandler = new ClientTickHandlerFix();
        FMLCommonHandler.instance().bus().register(tickHandler);
        MinecraftForge.EVENT_BUS.register(tickHandler);
    }
}
