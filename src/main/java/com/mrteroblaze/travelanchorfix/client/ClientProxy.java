package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.client.handler.ClientTickHandlerFix;
import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorTESRFix;
import com.mrteroblaze.travelanchorfix.common.CommonProxy;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    public void init() {
        // Регистрируем рендер для нашего фикса Travel Anchor
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());

        // Регистрируем обработчик тиков
        ClientTickHandlerFix tickHandler = new ClientTickHandlerFix();
        FMLCommonHandler.instance().bus().register(tickHandler);
        MinecraftForge.EVENT_BUS.register(tickHandler);
    }
}
