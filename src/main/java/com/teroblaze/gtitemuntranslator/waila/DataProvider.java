package com.teroblaze.gtitemuntranslator.waila;

import java.util.List;

import com.teroblaze.gtitemuntranslator.TooltipEventHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayerMP;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class DataProvider implements IWailaDataProvider {

    private final TooltipEventHandler tooltipHandler = new TooltipEventHandler();

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (itemStack != null) {
            try {
                String unloc = itemStack.getUnlocalizedName();
                String englishName = tooltipHandler.getOriginalEnglishName(itemStack, unloc);
                if (englishName != null && !currenttip.contains("[EN] " + englishName)) {
                    // Добавляем сразу под основное название
                    currenttip.add(1, "[EN] " + englishName);
                }
            } catch (Throwable ignored) {}
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag,
                                     World world, int x, int y, int z) {
        return tag;
    }
}
