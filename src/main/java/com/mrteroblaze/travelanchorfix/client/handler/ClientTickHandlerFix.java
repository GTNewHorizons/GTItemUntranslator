package com.mrteroblaze.travelanchorfix.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import crazypants.enderio.machine.travel.TileTravelAnchor;
import crazypants.enderio.machine.travel.TravelController;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class ClientTickHandlerFix {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        if (player != null && mc.currentScreen == null) {
            // Обновляем список якорей для отображения
            TravelController.instance.onClientTick();

            // Проверка: игрок стоит на якоре?
            TileEntity te = mc.theWorld.getTileEntity(
                    (int) Math.floor(player.posX),
                    (int) Math.floor(player.posY - 1),
                    (int) Math.floor(player.posZ)
            );

            if (te instanceof TileTravelAnchor) {
                // Если присел на якоре — телепортируем
                if (player.isSneaking()) {
                    TravelController.instance.activateSelectedTravelTarget(player);
                }
            }
        }
    }
}
