package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorTESRFix;
import com.mrteroblaze.travelanchorfix.server.ServerProxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class ClientProxy extends ServerProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // Регистрируем наш фиксированный рендер Travel Anchor
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());
    }
}
