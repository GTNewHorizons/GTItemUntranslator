package com.mrteroblaze.travelanchorfix.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorFixRenderer;

public class ClientProxy {
    public void initClient() {
        // Переопределяем TESR для TileTravelAnchor
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorFixRenderer());
    }
}
