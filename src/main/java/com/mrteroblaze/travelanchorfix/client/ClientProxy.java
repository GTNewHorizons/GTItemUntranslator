package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.client.handler.ClientTickHandlerFix;
import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorTESRFix;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        super.init();

        // Регистрируем фиксированный TESR для Travel Anchor
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());

        // Регистрируем наш фикс тик-хендлера
        FMLCommonHandler.instance().bus().register(new ClientTickHandlerFix());
    }
}
