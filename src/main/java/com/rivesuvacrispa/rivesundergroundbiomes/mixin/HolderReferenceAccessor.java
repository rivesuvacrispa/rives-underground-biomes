package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// the holder owner is the registry lookup which is also a HolderGetter
@Mixin(Holder.Reference.class)
public interface HolderReferenceAccessor {
    @Accessor("owner")
    HolderOwner<?> rub$owner();
}
