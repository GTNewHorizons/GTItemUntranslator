package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.server.ServerProxy;
import com.mrteroblaze.travelanchorfix.client.render.MyTravelEntitySpecialRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class ClientProxy extends ServerProxy {
    @Override
    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new MyTravelEntitySpecialRenderer());
    }
}
