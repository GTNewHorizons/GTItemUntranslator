package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorTESRFix;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class ClientProxy extends com.mrteroblaze.travelanchorfix.server.ServerProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // Перебиндим TESR — наш рендер станет активным и перекроет стандартный
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());
    }
}
