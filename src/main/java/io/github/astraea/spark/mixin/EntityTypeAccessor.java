package io.github.astraea.spark.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityType.class)
public interface EntityTypeAccessor {
    @Accessor("CLASS_NAME_MAP")
    static Map<Class<? extends Entity>, String> spark$getClassNameMap() {
        throw new AssertionError();
    }
}
