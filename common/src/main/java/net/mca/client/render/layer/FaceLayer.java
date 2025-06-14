package net.mca.client.render.layer;

import net.mca.MCA;
import net.mca.client.model.CommonVillagerModel;
import net.mca.entity.ai.Genetics;
import net.mca.entity.ai.Traits;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

public class FaceLayer<T extends LivingEntity, M extends BipedEntityModel<T>> extends VillagerLayer<T, M> {
    private static final int FACE_COUNT = 22;

    private final String variant;

    public FaceLayer(FeatureRendererContext<T, M> renderer, M model, String variant) {
        super(renderer, model);
        this.variant = variant;
    }

    @Override
    public void render(MatrixStack transform, VertexConsumerProvider provider, int light, T villager, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        model.setVisible(false);
        model.head.visible = true;

        super.render(transform, provider, light, villager, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
    }

    @Override
    protected boolean isTranslucent() {
        return true;
    }

    @Override
    public Identifier getSkin(T villager) {
        
        Biome biome = villager.getWorld().getBiome(villager.getBlockPos()).value();

        Identifier biomeId = villager.getWorld().getRegistryManager()
            .get(RegistryKeys.BIOME)
            .getId(biome);

        String biomeName = biomeId != null ? biomeId.toString() : "unknown";
        
        int index = (int) Math.min(FACE_COUNT - 1, Math.max(0, CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.FACE) * FACE_COUNT));
        boolean hasHeterochromia = variant.equals("normal") && CommonVillagerModel.getVillager(villager).getTraits().hasTrait(Traits.HETEROCHROMIA);
        Integer random_eye = MathHelper.nextBetween(Random.create(), 0, 2);

        switch (biomeName) {
            case "bleakisles:ahrathi_isles_badlands", "bleakisles:sandstone_basin_savannah", "bleakisles:ahrathi_isles_desert", "bleakisles:sandstone_basin_desert", "bleakisles:sandstone_basin_jungle", "bleakisles:ahrathi_isles_jungle_south", "bleakisles:ahrathi_isles_jungle_east":
                // Ahrathi
                if (random_eye == 0){
                    index = 0; // Brown
                    if (hasHeterochromia){
                        index = 17; // Brown/Blind
                    }
                }
                else if (random_eye == 1){
                    index = 1; // Hazel
                    if (hasHeterochromia){
                        index = 14; // Light Blue/Brown
                    }
                }
                else{
                    index = 11; // Gold
                    if (hasHeterochromia){
                        index = 7; // Gray/Gold
                    }
                }
                break;

            case "bleakisles:blundercast_jungle":
                // Sea Nomad
                if (random_eye == 0){
                    index = 0; // Brown
                    if (hasHeterochromia){
                        index = 17; // Brown/Blind
                    }
                }
                else if (random_eye == 1){
                    index = 3; // Light Green
                    if (hasHeterochromia){
                        index = 2; // Light Green/Teal
                    }
                }
                else{
                    index = 9; // Gray
                    if (hasHeterochromia){
                        index = 7; // Gray/Gold
                    }
                }
                break;

            case "bleakisles:crimson_isles_mangrove_swamp", "bleakisles:crimson_isles_plains", "bleakisles:crimson_isles_forest":
                // Crimson Elf
                if (random_eye == 0){
                    index = 6; // Red
                    if (hasHeterochromia){
                        index = 4; // Red/Purple
                    }
                }
                else if (random_eye == 1){
                    index = 8; // Purple
                    if (hasHeterochromia){
                        index = 4; // Red/Purple
                    }
                }
                else{
                    index = 9; // Gray
                    if (hasHeterochromia){
                        index = 5; // Brown/Purple
                    }
                }
                break;

            case "bleakisles:crimson_isles_jungle":
                // Crimson Orc
                index = 17; // Red Orc
                if (hasHeterochromia){
                    index = 18; // Blind/Red Orc
                }
                break;

            case "bleakisles:eidlihas_wooded_badlands", "bleakisles:eidlihas_badlands":
                // Elder Orcs
                if (random_eye == 0){
                    index = 17; // Red
                    if (hasHeterochromia){
                        index = 18; // Red/Blind
                    }
                }
                else if (random_eye == 1){
                    index = 19; // Blue
                    if (hasHeterochromia){
                        index = 21; // Blue/Green
                    }
                }
                else{
                    index = 20; // Green
                    if (hasHeterochromia){
                        index = 19; // Red/Green
                    }
                }
                break;

            case "bleakisles:eidlihas_birch_forest":
                // Gray or Sun Elves
                Integer random_selection = MathHelper.nextBetween(Random.create(), 0, 1);
                if (random_selection == 0){ // Gray Elf
                    if (random_eye == 0){
                        index = 6; // Red
                        if (hasHeterochromia){
                            index = 4; // Red/Purple
                        }
                    }
                    else if (random_eye == 1){
                        index = 8; // Purple
                        if (hasHeterochromia){
                            index = 4; // Red/Purple
                        }
                    }
                    else{
                        index = 9; // Gray
                        if (hasHeterochromia){
                            index = 5; // Brown/Purple
                        }
                    }
                }
                else{ // Sun Elf
                    if (random_eye == 0){
                        index = 11; // Gold
                        if (hasHeterochromia){
                            index = 10; // Gold/Red
                        }
                    }
                    else if (random_eye == 1){
                        index = 2; // Blue
                        if (hasHeterochromia){
                            index = 14; // Light Blue/Brown
                        }
                    }
                    else{
                        index = 3; // Green
                        if (hasHeterochromia){
                            index = 2; // Teal/Light Green
                        }
                    }
                }
                break;

            case "bleakisles:folken_isles_orbeitor_taiga", "bleakisles:folken_isles_ithlaer_taiga", "bleakisles:folken_isles_ithlaer_dark_forest", "bleakisles:folken_isles_orbeitor_snowy_taiga", "bleakisles:folken_isles_ithlaer_snowy_taiga", "bleakisles:folken_isles_naranir_dark_forest", "bleakisles:folken_isles_flower_forest", "bleakisles:folken_isles_aetlis_forest", "bleakisles:folken_isles_aetlis_swamp":
                // Orbeitians
                if (random_eye == 0){
                    index = 1; // Hazel
                    if (hasHeterochromia){
                        index = 17; // Brown/Blind
                    }
                }
                else if (random_eye == 1){
                    index = 16; // Light Blue
                    if (hasHeterochromia){
                        index = 3; // Brown/Blue
                    }
                }
                else{
                    index = 3; // Light Green
                    if (hasHeterochromia){
                        index = 1; // Blue/Green
                    }
                }
                break;

            case "bleakisles:omn_sunflower_plains":
                // Sea Elves
                index = 10; // Blind
                break;

            case "bleakisles:shattered_coasts_snowy_taiga_west", "bleakisles:shattered_coasts_snowy_plains", "bleakisles:shattered_coasts_snowy_taiga_east", "bleakisles:shattered_coasts_taiga_west", "bleakisles:shattered_coasts_taiga_east", "bleakisles:shattered_coasts_plains_north":
                // Pale Elf
                if (random_eye == 0){
                    index = 14; // Light pink
                    if (hasHeterochromia){
                        index = 9; // Purple/Gray
                    }
                }
                else if (random_eye == 1){
                    index = 12; // Yellow
                    if (hasHeterochromia){
                        index = 10; // Gold/Red
                    }
                }
                else{
                    index = 16; // Light blue
                    if (hasHeterochromia){
                        index = 13; // Salmon/Gray
                    }
                }
                break;

            case "bleakisles:silver_isles_plains", "bleakisles:silver_isles_mushroom_fields", "bleakisles:silver_isles_forest":
                // Silver Orcs
                if (random_eye == 0){
                    index = 17; // Red
                    if (hasHeterochromia){
                        index = 18; // Red/Blind
                    }
                }
                else if (random_eye == 1){
                    index = 19; // Blue
                    if (hasHeterochromia){
                        index = 21; // Blue/Green
                    }
                }
                else{
                    index = 18; // Green
                    if (hasHeterochromia){
                        index = 20; // Brown/Blind
                    }
                }
                break;

            case "bleakisles:eidlihas_plains":
                // Sun Elf
                    if (random_eye == 0){
                        index = 11; // Gold
                        if (hasHeterochromia){
                            index = 10; // Gold/Red
                        }
                    }
                    else if (random_eye == 1){
                        index = 2; // Blue
                        if (hasHeterochromia){
                            index = 14; // Light Blue/Brown
                        }
                    }
                    else{
                        index = 3; // Green
                        if (hasHeterochromia){
                            index = 2; // Teal/Light Green
                        }
                    }
                break;

            case "bleakisles:vingoria_dark_forest", "bleakisles:vingoria_jungle", "bleakisles:vingoria_ashland", "bleakisles:vingoria_plains":
                // Ashen
                    if (random_eye == 0){
                        index = 9; // Gray
                        if (hasHeterochromia){
                            index = 9; // Purple/Gray
                        }
                    }
                    else if (random_eye == 1){
                        index = 6; // Red
                        if (hasHeterochromia){
                            index = 13; // Salmon/Gray
                        }
                    }
                    else{
                        index = 1; // Brown
                        if (hasHeterochromia){
                            index = 5; // Brown/Purple
                        }
                    }
                break;

            default:
                // Do nothing
                break;
        }

        int time = villager.age / 2 + (int) (CommonVillagerModel.getVillager(villager).getGenetics().getGene(Genetics.HEMOGLOBIN) * 65536);
        boolean blink = time % 50 == 1 || time % 57 == 1 || villager.isSleeping() || villager.isDead();
        String gender = CommonVillagerModel.getVillager(villager).getGenetics().getGender().getDataName();
        String blinkTexture = blink ? "_blink" : (hasHeterochromia ? "_hetero" : "");

        return cached("skins/face/" + variant + "/" + gender + "/" + index + blinkTexture + ".png", MCA::locate);
    }
}
