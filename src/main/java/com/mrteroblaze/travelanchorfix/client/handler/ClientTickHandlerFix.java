package com.mrteroblaze.travelanchorfix.handler;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import crazypants.enderio.machine.travel.TileTravelAnchor;
import crazypants.enderio.machine.travel.TravelController;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.EnumSet;

public class ClientTickHandlerFix implements ITickHandler {

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        if (type.contains(TickType.CLIENT)) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.thePlayer;

            if (player != null && mc.currentScreen == null) {
                // Обновляем отображение
                TravelController.instance.onClientTick();

                // Проверяем телепортацию
                TileEntity te = mc.theWorld.getTileEntity(
                        (int) Math.floor(player.posX),
                        (int) Math.floor(player.posY - 1),
                        (int) Math.floor(player.posZ)
                );

                if (te instanceof TileTravelAnchor) {
                    if (player.isSneaking()) {
                        // Присел на якоре — телепортируем
                        TravelController.instance.activateSelectedTravelTarget(player);
                    }
                }
            }
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {}

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel() {
        return "TravelAnchorFix_ClientTick";
    }
}
