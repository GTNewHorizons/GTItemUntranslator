package com.teroblaze.gtitemuntranslator.waila;

import com.teroblaze.gtitemuntranslator.GTItemUntranslator;
import com.teroblaze.gtitemuntranslator.OriginalLanguageStore;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public class DataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!GTItemUntranslator.tooltipsEnabled) return currenttip;
        if (itemStack == null) return currenttip;

        TileEntity te = accessor.getTileEntity();
        if (te instanceof BaseMetaTileEntity) {
            IMetaTileEntity meta = ((BaseMetaTileEntity) te).getMetaTileEntity();
            if (meta != null) {
                String rawKey = meta.getMetaName(); // например "multimachine.blastfurnace"
                if (rawKey != null && !rawKey.isEmpty()) {
                    String fullKey = "gt.blockmachines." + rawKey;
                    System.out.println("[GT Item Untranslator][Waila] metaKey: " + fullKey);
                    String enName = OriginalLanguageStore.getOriginal(fullKey);
                    if (enName != null && !enName.equals(fullKey)) {
                        System.out.println("[GT Item Untranslator][Waila] metaKey resolved: " + enName);
                        currenttip.add(1, "§7[EN] " + enName);
                        return currenttip;
                    } else {
                        System.out.println("[GT Item Untranslator][Waila] metaKey NOT resolved: " + fullKey);
                    }
                }
            }
        }

        // === ФОЛБЭК через itemStack.getUnlocalizedName() ===
        String unloc = itemStack.getUnlocalizedName(); // gt.blockmachines.multimachine.blastfurnace
        if (unloc != null && !unloc.isEmpty()) {
            String fullKey = unloc + ".name"; // gt.blockmachines.multimachine.blastfurnace.name
            System.out.println("[GT Item Untranslator][Waila] fallbackKey: " + fullKey);
            String enName = OriginalLanguageStore.getOriginal(fullKey);
            if (enName != null && !enName.equals(fullKey)) {
                System.out.println("[GT Item Untranslator][Waila] fallbackKey resolved: " + enName);
                currenttip.add(1, "§7[EN] " + enName);
            } else {
                System.out.println("[GT Item Untranslator][Waila] fallbackKey NOT resolved: " + fullKey);
            }
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
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te,
                                     NBTTagCompound tag, World world, int x, int y, int z) {
        return tag;
    }
}
