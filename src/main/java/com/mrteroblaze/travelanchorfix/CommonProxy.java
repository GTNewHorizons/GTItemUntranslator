package com.mrteroblaze.travelanchorfix.client;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        // Общая инициализация для клиента и сервера
    }

    public void init(FMLInitializationEvent event) {
        // Общая инициализация для клиента и сервера
    }

    public World getClientWorld() {
        // Переопределяется в ClientProxy
        return null;
    }

    public EntityPlayer getClientPlayer() {
        // Переопределяется в ClientProxy
        return null;
    }
}
