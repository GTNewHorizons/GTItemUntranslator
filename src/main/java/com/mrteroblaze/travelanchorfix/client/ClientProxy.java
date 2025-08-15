package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.CommonProxy;
import com.mrteroblaze.travelanchorfix.client.render.MyTravelEntitySpecialRenderer;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        // Перерегистрируем TESR для Travel Anchor — наш рендер грузится последним и заменит оригинальный
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new MyTravelEntitySpecialRenderer());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}
