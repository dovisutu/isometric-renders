package com.glisco.isometricrenders.mixin;

import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockStateArgument.class)
public interface BlockStateArgumentAccessor {

    @Accessor
    CompoundTag getData();

}
