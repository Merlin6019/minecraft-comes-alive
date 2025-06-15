package net.mca.resources;

import com.google.gson.JsonElement;
import net.mca.MCA;
import net.mca.entity.VillagerLike;
import net.mca.entity.ai.relationship.Gender;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.biome.Biome;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Names extends JsonDataLoader {
    protected static final Identifier ID = MCA.locate("mca_names");

    public static final Map<String, Map<Gender, WeightedPool<String>>> NAMES_MAP = new HashMap<>();
    public static final List<String> REGION_NAMES = new LinkedList<>();

    public Names() {
        super(Resources.GSON, ID.getPath());
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        NAMES_MAP.clear();
        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            String[] split = entry.getKey().getPath().split("/");
            Gender gender = Gender.byName(split[1]);

            Map<Gender, WeightedPool<String>> map = NAMES_MAP.computeIfAbsent(split[0], a -> new HashMap<>());

            WeightedPool.Mutable<String> names = new WeightedPool.Mutable<>("?");
            for (Map.Entry<String, JsonElement> elementEntry : entry.getValue().getAsJsonObject().entrySet()) {
                names.add(elementEntry.getKey(), (float)Math.pow(elementEntry.getValue().getAsInt(), 0.5));
            }

            map.put(gender, names);
        }

        REGION_NAMES.clear();
        Arrays.stream(NAMES_MAP.keySet().toArray()).sorted().forEach(n -> REGION_NAMES.add((String)n));
    }

    static final Random random = Random.create();

    public static String getCitizenNation(Entity entity) {
        String race = getRaceByBiome(entity);
        
        return race;
    }

    public static String pickCitizenName(@NotNull Gender gender, Entity entity) {
        return NAMES_MAP.isEmpty() ? "Unnamed" : NAMES_MAP.get(getCitizenNation(entity)).get(gender.binary()).pickOne();
    }

    public static String pickCitizenName(@NotNull Gender gender) {
        return NAMES_MAP.isEmpty() ? "Unnamed" : NAMES_MAP.get(REGION_NAMES.get(random.nextInt(REGION_NAMES.size()))).get(gender.binary()).pickOne();
    }

    private static String getRaceByBiome(Entity entity){
        Biome biome = entity.getWorld().getBiome(entity.getBlockPos()).value();

        Identifier biomeId = entity.getWorld().getRegistryManager()
        .get(RegistryKeys.BIOME)
        .getId(biome);

        String biomeName = biomeId != null ? biomeId.toString() : "unknown";

        Integer random_selection = MathHelper.nextBetween(Random.create(), 0, 1);
        
        switch (biomeName) {
            case "bleakisles:folken_isles_orbeitor_taiga", "bleakisles:folken_isles_ithlaer_taiga", "bleakisles:folken_isles_ithlaer_dark_forest", "bleakisles:folken_isles_orbeitor_snowy_taiga", "bleakisles:folken_isles_ithlaer_snowy_taiga", "bleakisles:folken_isles_naranir_dark_forest":
                return "folk_northerner";

            case "bleakisles:folken_isles_flower_forest", "bleakisles:folken_isles_aetlis_forest", "bleakisles:folken_isles_aetlis_swamp":
                return "folk_mainlander";

            case "bleakisles:sandstone_basin_savannah", "bleakisles:sandstone_basin_desert", "bleakisles:sandstone_basin_jungle":
                return "folk_northern_ahrathi";


            case "bleakisles:ahrathi_isles_badlands", "bleakisles:ahrathi_isles_desert", "bleakisles:ahrathi_isles_jungle_south", "bleakisles:ahrathi_isles_jungle_east":
                if (random_selection == 0){
                    return "folk_southern_ahrathi";
                }
                else{
                    return "elf_sand";
                }

            case "bleakisles:blundercast_jungle":
                return "folk_murgish";

            case "bleakisles:vingoria_dark_forest", "bleakisles:vingoria_jungle", "bleakisles:vingoria_ashland", "bleakisles:vingoria_plains":
                return "folk_ashen";

            case "bleakisles:crimson_isles_mangrove_swamp", "bleakisles:crimson_isles_plains", "bleakisles:crimson_isles_forest":
                return "elf_crimson";


            case "bleakisles:crimson_isles_jungle":
                return "orc_crimson";


            case "bleakisles:eidlihas_wooded_badlands", "bleakisles:eidlihas_badlands":
                return "orc_mainlander";


            case "bleakisles:eidlihas_birch_forest":
                if (random_selection == 0){
                    return "elf_gray";
                }
                else{
                    return "elf_sun";
                }


            case "bleakisles:omn_sunflower_plains":
                return "elf_sea";


            case "bleakisles:shattered_coasts_snowy_taiga_west", "bleakisles:shattered_coasts_snowy_plains", "bleakisles:shattered_coasts_snowy_taiga_east", "bleakisles:shattered_coasts_taiga_west", "bleakisles:shattered_coasts_taiga_east", "bleakisles:shattered_coasts_plains_north":
                return "elf_pale";


            case "bleakisles:silver_isles_plains", "bleakisles:silver_isles_mushroom_fields", "bleakisles:silver_isles_forest":
                return "orc_silver";


            case "bleakisles:eidlihas_plains":
                return "elf_sun";


            default:
                if (random_selection == 0){
                    return "folk_murgish";
                }
                else{
                    return "elf_sea";
                }
        }
    }
}
