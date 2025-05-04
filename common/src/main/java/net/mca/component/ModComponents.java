package net.mca.component;

import net.mca.MCA;
import net.mca.component.UUIDComponent;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ModComponents {

    public static final Codec<UUIDComponent> UUID_CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
            Codec.LONG.fieldOf("mostSigBits").forGetter(UUIDComponent::mostSigBits),
            Codec.LONG.fieldOf("leastSigBits").forGetter(UUIDComponent::leastSigBits));
    });

    public static final ComponentType<UUIDComponent> FATHER = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(MCA.MOD_ID, "father"),
        ComponentType.<UUIDComponent>builder().codec(UUID_CODEC).build());

    public static final ComponentType<UUIDComponent> MOTHER = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(MCA.MOD_ID, "mother"),
        ComponentType.<UUIDComponent>builder().codec(UUID_CODEC).build());
    
    public static final ComponentType<String> FATHER_NAME = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(MCA.MOD_ID, "fatherName"),
        ComponentType.<String>builder().codec(Codec.STRING).build());
    
    public static final ComponentType<String> MOTHER_NAME = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(MCA.MOD_ID, "motherName"),
        ComponentType.<String>builder().codec(Codec.STRING).build());
}