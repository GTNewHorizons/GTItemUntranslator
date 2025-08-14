package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.server.ServerProxy;
import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorTESRFix;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import cpw.mods.fml.client.registry.ClientRegistry;

public class ClientProxy extends ServerProxy {
    @Override
    public void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());
    }
}
