package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.CommonProxy;
import com.mrteroblaze.travelanchorfix.client.render.TravelEntitySpecialRenderer;

public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        super.init();
        // Регистрируем наш фикс-рендерер
        TravelEntitySpecialRenderer.register();
    }
}
