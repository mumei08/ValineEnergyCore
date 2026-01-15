package kaede.valineenergycore.common.content.network;

import net.minecraft.core.BlockPos;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Acceptor (エネルギー受容側) のデータ
 */

public class AcceptorData {
    private final BlockPos position;
    private final IEnergyStorage storage;

    public AcceptorData(BlockPos position, IEnergyStorage storage) {
        this.position = position;
        this.storage = storage;
    }

    public BlockPos getPosition() {
        return position;
    }

    public IEnergyStorage getStorage() {
        return storage;
    }
}