package com.teroblaze.gtitemuntranslator.waila;

import net.minecraft.block.Block;

import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaRegister {

    public static void register(IWailaRegistrar registrar) {
        // Регистрируем наш DataProvider для заголовка (верхняя строка в Waila)
        registrar.registerHeadProvider(new DataProvider(), Block.class);
    }
}
