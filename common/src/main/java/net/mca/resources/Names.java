package net.mca.resources;

import com.google.gson.JsonElement;
import net.mca.MCA;
import net.mca.entity.VillagerLike;
import net.mca.entity.ai.Genetics;
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

import static net.mca.client.model.CommonVillagerModel.getVillager;

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
        
        String[] raceArray = {
            // Folk
            "folk_northerner",
            "folk_mainlander",
            "folk_northern_ahrathi",
            "folk_southern_ahrathi",
            "folk_murgish",
            "folk_ashen",
            // Elves
            "elf_crimson",
            "elf_gray",
            "elf_sun",
            "elf_sand",
            "elf_pale",
            "elf_sea",
            // Orcs
            "orc_silver",
            "orc_crimson",
            "orc_mainlander",
        };

        int raceIndex = (int) getVillager(entity).getGenetics().getGene(Genetics.RACE);
        String race = raceArray[raceIndex];

        if (race != null){
            return race;
        }
        else{
            return "sun_elf";   
        }

    }

    public static String pickCitizenName(@NotNull Gender gender, Entity entity) {
        return NAMES_MAP.isEmpty() ? "Unnamed" : NAMES_MAP.get(getCitizenNation(entity)).get(gender.binary()).pickOne();
    }

    public static String pickCitizenName(@NotNull Gender gender) {
        return NAMES_MAP.isEmpty() ? "Unnamed" : NAMES_MAP.get(REGION_NAMES.get(random.nextInt(REGION_NAMES.size()))).get(gender.binary()).pickOne();
    }
}
