package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.handler.ClientTickHandlerFix;
import cpw.mods.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        super.init();
        // Регистрируем обработчик тиков
        FMLCommonHandler.instance().bus().register(new ClientTickHandlerFix());
    }
}
