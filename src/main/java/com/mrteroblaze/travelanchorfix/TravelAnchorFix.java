package com.mrteroblaze.travelanchorfix;

import net.minecraftforge.common.MinecraftForge;

import com.mrteroblaze.travelanchorfix.client.render.AnchorLabelRenderer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;

@Mod(
    modid = "travelanchorfix",
    name = "Travel Anchor Fix",
    version = "1.0.0",
    dependencies = "required-after:EnderIO;required-after:EnderCore")
public class TravelAnchorFix {

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        // ничего
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        if (FMLCommonHandler.instance()
            .getEffectiveSide() == Side.CLIENT) {
            // ВАЖНО: НЕ подменяем TESR у TileTravelAnchor!
            // Просто вешаем наш отрисовщик подписи на RenderWorldLastEvent
            MinecraftForge.EVENT_BUS.register(new AnchorLabelRenderer());
            System.out.println("[TravelAnchorFix] AnchorLabelRenderer registered (RenderWorldLastEvent).");
        }
    }
}
