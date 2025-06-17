package net.mca.entity.ai;

import net.mca.Config;
import net.mca.entity.VillagerLike;
import net.mca.entity.ai.relationship.Gender;
import net.mca.util.network.datasync.CDataManager;
import net.mca.util.network.datasync.CDataParameter;
import net.mca.util.network.datasync.CEnumParameter;
import net.mca.util.network.datasync.CParameter;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Villagerized Genetic Diversity.
 */
public class Genetics implements Iterable<Genetics.Gene> {
    private static final Set<GeneType> GENOMES = new HashSet<>();

    public static final GeneType SIZE = new GeneType("gene_size");
    public static final GeneType WIDTH = new GeneType("gene_width");
    public static final GeneType BREAST = new GeneType("gene_breast");
    public static final GeneType MELANIN = new GeneType("gene_melanin");
    public static final GeneType HEMOGLOBIN = new GeneType("gene_hemoglobin");
    public static final GeneType EUMELANIN = new GeneType("gene_eumelanin");
    public static final GeneType PHEOMELANIN = new GeneType("gene_pheomelanin");
    public static final GeneType SKIN = new GeneType("gene_skin");
    public static final GeneType FACE = new GeneType("gene_face");
    public static final GeneType VOICE = new GeneType("gene_voice");
    public static final GeneType VOICE_TONE = new GeneType("gene_voice_tone");
    public static final GeneType RACE = new GeneType("gene_race");

    public int face_count = 22;
    public int skin_count = 5;

    // MELANIN range for Bleak Isles Skin Tones (Y)
    // FOLK
    // Northern Folk Melanin (0)
    public float melanin_min_northern_folk = 0;
    public float melanin_max_northern_folk = 0.25F;

    // Sea Nomad Folk Melanin (1)
    public float melanin_min_sea_nomad = 0.26F;
    public float melanin_max_sea_nomad = 0.5F;

    // Ahrathi Folk Melanin (2)
    public float melanin_min_ahrathi = 0.16F;
    public float melanin_max_ahrathi = 0.36F;

    // Ashen Melanin (3)
    public float melanin_min_ashen = 0;
    public float melanin_max_ashen = 0.16F;

    // ELF
    // Sun Elf Melanin (4)
    public float melanin_min_sun_elf = 0.16F;
    public float melanin_max_sun_elf = 0.32F;

    // Pale Elves Melanin (5)
    public float melanin_min_pale_elf = 0.51F;
    public float melanin_max_pale_elf = 0.66F;

    // Gray Elves Melanin (6)
    public float melanin_min_gray_elf = 0.7F;
    public float melanin_max_gray_elf = 0.8F;

    // Crimson Elves Melanin (7)
    public float melanin_min_crimson_elf = 0.75F;
    public float melanin_max_crimson_elf = 0.9F;

    // Sea Elves Melanin (8)
    public float melanin_sea_elf = 0.51F;

    // ORC
    // Elder Orc Melanin (9)
    public float melanin_min_elder_orc = 0F;
    public float melanin_max_elder_orc = 0.16F;

    // Silver Orcs Melanin (10)
    public float melanin_min_silver_orc = 0.51F;
    public float melanin_max_silver_orc = 0.66F;

    // Crimson Orcs Melanin (11)
    public float melanin_min_crimson_orc = 0.7F;
    public float melanin_max_crimson_orc = 0.8F;

    // HEMOGLOBIN Range (X)
    // Human Skin Tones = 0 - 0.25
    public float hemoglobin_min_human = 0;
    public float hemoglobin_max_human = 0.25F;

    // Yellow Skin Tones = 0.26 - 0.37
    public float hemoglobin_min_yellow = 0.26F;
    public float hemoglobin_max_yellow = 0.37F;

    // Green Skin Tones = 0.37 - 0.5
    public float hemoglobin_min_green = 0.37F;
    public float hemoglobin_max_green = 0.5F;

    // Gray Skin Tones = 0 - 0.12
    public float hemoglobin_min_gray = 0;
    public float hemoglobin_max_gray = 0.12F;

    // Blue Skin Tones = 0.13 - 0.25
    public float hemoglobin_min_blue = 0.13F;
    public float hemoglobin_max_blue = 0.25F;

    // Pink-Red Skin Tones - 0.26 - 0.5
    public float hemoglobin_min_pinkRed = 0.26F;
    public float hemoglobin_max_pinkRed = 0.5F;

    private static final CEnumParameter<Gender> GENDER = CParameter.create("gender", Gender.UNASSIGNED);

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        GENOMES.forEach(g -> builder.addAll(g.getParam()));
        return builder.addAll(GENDER);
    }

    private Random random = Random.create();

    private final Map<GeneType, Gene> genes = new HashMap<>();

    private final VillagerLike<?> entity;

    public Genetics(VillagerLike<?> entity) {
        this.entity = entity;
    }

    public float getVerticalScaleFactor() {
        return 0.75F + getGene(SIZE) / 2;
    }

    public float getHorizontalScaleFactor() {
        return 0.75F + getGene(WIDTH) / 2;
    }

    public void setGender(Gender gender) {
        entity.setTrackedValue(GENDER, gender);
    }

    public Gender getGender() {
        return entity.getTrackedValue(GENDER);
    }

    public float getBreastSize() {
        return getGender() == Gender.FEMALE ? getGene(BREAST) : 0;
    }

    @Override
    public Iterator<Gene> iterator() {
        return genes.values().iterator();
    }

    public void setGene(GeneType type, float value) {
        getGenome(type).set(value);
    }

    public float getGene(GeneType type) {
        return getGenome(type).get();
    }

    public Gene getGenome(GeneType type) {
        return genes.computeIfAbsent(type, Gene::new);
    }

    //initializes the genes with random numbers
    public void randomize() {

        for (GeneType type : GENOMES) {
            getGenome(type).randomize();
        }

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

        int[] skinElfArray = {
            1,
            2,
            3
        };

        int[] skinFolkArray = {
            5,
            6,
            7,
            8,
            9
        };

        int[] skinOrcArray = {
            0,
            1,
            2,
            3,
            4
        };

        boolean hasHeterochromia = entity.getTraits().hasTrait(Traits.HETEROCHROMIA);
        Integer random_eye = MathHelper.nextInt(Random.create(), 0, 2);

        String race = "folk_murgish"; // Default fallback race
        int skin = 3; // Default fallback skin
        int randomSkinIndex = 0;
        int raceIndex = 0;

        // Immigrant Race
        if (random.nextFloat() < 0.01F) {

            int randomRaceIndex = random.nextInt(raceArray.length);

            race = raceArray[randomRaceIndex];
        }
        else{
            race = getRaceByBiome();
            raceIndex = Arrays.asList(raceArray).indexOf(race);
        }

        switch(race){
            case "folk_northerner":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_northern_folk, melanin_max_northern_folk));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
            setGene(SIZE, centeredRandom(0.5F));
            setGene(WIDTH, centeredRandom(0.4F));

            randomSkinIndex = random.nextInt(skinFolkArray.length);
            skin = skinFolkArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 1F / face_count); // Hazel
                if (hasHeterochromia){
                    setGene(FACE, 17F / face_count); // Brown/Blind
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 16F / face_count); // Light Blue
                if (hasHeterochromia){
                    setGene(FACE, 3F / face_count); // Brown/Blue
                }
            }
            else{
                setGene(FACE, 3F / face_count); // Light Green
                if (hasHeterochromia){
                    setGene(FACE, 1F / face_count); // Blue/Green
                }
            }
            break;

            case "folk_mainlander":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_northern_folk, melanin_max_northern_folk));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
            setGene(SIZE, centeredRandom(0.5F));
            setGene(WIDTH, centeredRandom(0.4F));

            randomSkinIndex = random.nextInt(skinFolkArray.length);
            skin = skinFolkArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 1F / face_count); // Hazel
                if (hasHeterochromia){
                    setGene(FACE, 17F / face_count); // Brown/Blind
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 16F / face_count); // Light Blue
                if (hasHeterochromia){
                    setGene(FACE, 3F / face_count); // Brown/Blue
                }
            }
            else{
                setGene(FACE, 3F / face_count); // Light Green
                if (hasHeterochromia){
                    setGene(FACE, 1F / face_count); // Blue/Green
                }
            }
            break;

            case "folk_northern_ahrathi":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_sea_nomad, melanin_max_sea_nomad));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
            setGene(SIZE, centeredRandom(0.6F));
            setGene(WIDTH, centeredRandom(0.5F));

            randomSkinIndex = random.nextInt(skinFolkArray.length);
            skin = skinFolkArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 0F / face_count); // Brown
                if (hasHeterochromia){
                    setGene(FACE, 17F / face_count); // Brown/Blind
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 1F / face_count); // Hazel
                if (hasHeterochromia){
                    setGene(FACE, 14F / face_count); // Light Blue/Brown
                }
            }
            else{
                setGene(FACE, 11F / face_count); // Gold
                if (hasHeterochromia){
                    setGene(FACE, 7F / face_count); // Gray/Gold
                }
            }
            break;

            case "folk_southern_ahrathi":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_ahrathi, melanin_max_ahrathi));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
            setGene(SIZE, centeredRandom(0.6F));
            setGene(WIDTH, centeredRandom(0.5F));

            randomSkinIndex = random.nextInt(skinFolkArray.length);
            skin = skinFolkArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 0F / face_count); // Brown
                if (hasHeterochromia){
                    setGene(FACE, 17F / face_count); // Brown/Blind
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 1F / face_count); // Hazel
                if (hasHeterochromia){
                    setGene(FACE, 14F / face_count); // Light Blue/Brown
                }
            }
            else{
                setGene(FACE, 11F / face_count); // Gold
                if (hasHeterochromia){
                    setGene(FACE, 7F / face_count); // Gray/Gold
                }
            }
            break;

            case "folk_murgish":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_sea_nomad, melanin_max_sea_nomad));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
            setGene(SIZE, centeredRandom(0.65F));
            setGene(WIDTH, centeredRandom(0.55F));

            randomSkinIndex = random.nextInt(skinFolkArray.length);
            skin = skinFolkArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 0F / face_count); // Brown
                if (hasHeterochromia){
                    setGene(FACE, 17F / face_count); // Brown/Blind
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 3F / face_count); // Light Green
                if (hasHeterochromia){
                    setGene(FACE, 2F / face_count); // Light Green/Teal
                }
            }
            else{
                setGene(FACE, 9F / face_count); // Gray
                if (hasHeterochromia){
                    setGene(FACE, 7F / face_count); // Gray/Gold
                }
            }
            break;

            case "folk_ashen":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_ashen, melanin_max_ashen));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_yellow, hemoglobin_max_yellow));
            setGene(SIZE, centeredRandom(0.45F));
            setGene(WIDTH, centeredRandom(0.35F));

            randomSkinIndex = random.nextInt(skinElfArray.length);
            skin = skinElfArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 9F / face_count); // Gray
                if (hasHeterochromia){
                    setGene(FACE, 9F / face_count); // Purple/Gray
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 6F / face_count); // Red
                if (hasHeterochromia){
                    setGene(FACE, 13F / face_count); // Salmon/Gray
                }
            }
            else{
                setGene(FACE, 1F / face_count); // Brown
                if (hasHeterochromia){
                    setGene(FACE, 5F / face_count); // Brown/Purple
                }
            }
            break;

            case "elf_crimson":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_crimson_elf, melanin_max_crimson_elf));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_gray, hemoglobin_max_gray));
            setGene(SIZE, centeredRandom(0.75F));
            setGene(WIDTH, centeredRandom(0.55F));

            randomSkinIndex = random.nextInt(skinElfArray.length);
            skin = skinElfArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 6F / face_count); // Red
                if (hasHeterochromia){
                    setGene(FACE, 4F / face_count); // Red/Purple
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 8F / face_count); // Purple
                if (hasHeterochromia){
                    setGene(FACE, 4F / face_count); // Red/Purple
                }
            }
            else{
                setGene(FACE, 9F / face_count); // Gray
                if (hasHeterochromia){
                    setGene(FACE, 5F / face_count); // Brown/Purple
                }
            }
            break;

            case "elf_gray":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_gray_elf, melanin_max_gray_elf));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_gray, hemoglobin_max_gray));
            setGene(SIZE, centeredRandom(0.77F));
            setGene(WIDTH, centeredRandom(0.57F));

            randomSkinIndex = random.nextInt(skinElfArray.length);
            skin = skinElfArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 6F / face_count); // Red
                if (hasHeterochromia){
                    setGene(FACE, 4F / face_count); // Red/Purple
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 8F / face_count); // Purple
                if (hasHeterochromia){
                    setGene(FACE, 4F / face_count); // Red/Purple
                }
            }
            else{
                setGene(FACE, 9F / face_count); // Gray
                if (hasHeterochromia){
                    setGene(FACE, 5F / face_count); // Brown/Purple
                }
            }
            break;

            case "elf_sun":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_sun_elf, melanin_max_sun_elf));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_yellow, hemoglobin_max_yellow));
            setGene(SIZE, centeredRandom(0.8F));
            setGene(WIDTH, centeredRandom(0.6F));

            randomSkinIndex = random.nextInt(skinElfArray.length);
            skin = skinElfArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

                if (random_eye == 0){
                    setGene(FACE, 11F / face_count); // Gold
                    if (hasHeterochromia){
                        setGene(FACE, 10F / face_count); // Gold/Red
                    }
                }
                else if (random_eye == 1){
                    setGene(FACE, 2F / face_count); // Blue
                    if (hasHeterochromia){
                        setGene(FACE, 14F / face_count); // Light Blue/Brown
                    }
                }
                else{
                    setGene(FACE, 3F / face_count); // Green
                    if (hasHeterochromia){
                        setGene(FACE, 2F / face_count); // Teal/Light Green
                    }
                }
            break;

            case "elf_pale":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_pale_elf, melanin_max_pale_elf));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_gray, hemoglobin_max_gray));
            setGene(SIZE, centeredRandom(0.75F));
            setGene(WIDTH, centeredRandom(0.55F));

            randomSkinIndex = random.nextInt(skinElfArray.length);
            skin = skinElfArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 14F / face_count); // Light pink
                if (hasHeterochromia){
                    setGene(FACE, 9F / face_count); // Purple/Gray
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 12F / face_count); // Yellow
                if (hasHeterochromia){
                    setGene(FACE, 10F / face_count); // Gold/Red
                }
            }
            else{
                setGene(FACE, 16F / face_count); // Light blue
                if (hasHeterochromia){
                    setGene(FACE, 13F / face_count); // Salmon/Gray
                }
            }
            break;

            case "elf_sand":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_ahrathi, melanin_max_ahrathi));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
            setGene(SIZE, centeredRandom(0.8F));
            setGene(WIDTH, centeredRandom(0.6F));

            randomSkinIndex = random.nextInt(skinElfArray.length);
            skin = skinElfArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 11F / face_count); // Gold
                if (hasHeterochromia){
                    setGene(FACE, 10F / face_count); // Gold/Red
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 2F / face_count); // Blue
                if (hasHeterochromia){
                    setGene(FACE, 14F / face_count); // Light Blue/Brown
                }
            }
            else{
                setGene(FACE, 3F / face_count); // Green
                if (hasHeterochromia){
                    setGene(FACE, 2F / face_count); // Teal/Light Green
                }
            }
            break;

            case "elf_sea":
            setGene(RACE, raceIndex);
            setGene(MELANIN, melanin_sea_elf);
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_blue, hemoglobin_max_blue));
            setGene(SIZE, centeredRandom(0.85F));
            setGene(WIDTH, centeredRandom(0.65F));

            randomSkinIndex = random.nextInt(skinElfArray.length);
            skin = skinElfArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            setGene(FACE, 10F / face_count); // Blind
            break;

            case "orc_silver":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_silver_orc, melanin_max_silver_orc));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_blue, hemoglobin_max_blue));
            setGene(SIZE, centeredRandom(0.87F));
            setGene(WIDTH, centeredRandom(0.97F));

            randomSkinIndex = random.nextInt(skinOrcArray.length);
            skin = skinOrcArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 17F / face_count); // Red
                if (hasHeterochromia){
                    setGene(FACE, 18F / face_count); // Red/Blind
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 19F / face_count); // Blue
                if (hasHeterochromia){
                    setGene(FACE, 21F / face_count); // Blue/Green
                }
            }
            else{
                setGene(FACE, 18F / face_count); // Green
                if (hasHeterochromia){
                    setGene(FACE, 20F / face_count); // Brown/Blind
                }
            }
            break;

            case "orc_crimson":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_crimson_orc, melanin_max_crimson_orc));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_pinkRed, hemoglobin_max_pinkRed));
            setGene(SIZE, centeredRandom(0.85F));
            setGene(WIDTH, centeredRandom(95F));

            randomSkinIndex = random.nextInt(skinOrcArray.length);
            skin = skinOrcArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            setGene(FACE, 17F / face_count); // Red Orc
            if (hasHeterochromia){
                setGene(FACE, 18F / face_count); // Blind/Red Orc
            }
            break;

            case "orc_mainlander":
            setGene(RACE, raceIndex);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_elder_orc, melanin_max_elder_orc));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_green, hemoglobin_max_green));
            setGene(SIZE, centeredRandom(0.9F));
            setGene(WIDTH, centeredRandom(1F));

            randomSkinIndex = random.nextInt(skinOrcArray.length);
            skin = skinOrcArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 17F / face_count); // Red
                if (hasHeterochromia){
                    setGene(FACE, 18F / face_count); // Red/Blind
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 19F / face_count); // Blue
                if (hasHeterochromia){
                    setGene(FACE, 21F / face_count); // Blue/Green
                }
            }
            else{
                setGene(FACE, 20F / face_count); // Green
                if (hasHeterochromia){
                    setGene(FACE, 19F / face_count); // Red/Green
                }
            }
            break;

            default:
            // Murgish by default
            setGene(RACE, 4);
            setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_sea_nomad, melanin_max_sea_nomad));
            setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
            setGene(SIZE, centeredRandom(0.65F));
            setGene(WIDTH, centeredRandom(0.55F));

            randomSkinIndex = random.nextInt(skinFolkArray.length);
            skin = skinFolkArray[randomSkinIndex];
            setGene(SKIN, skin / skin_count);

            if (random_eye == 0){
                setGene(FACE, 0F / face_count); // Brown
                if (hasHeterochromia){
                    setGene(FACE, 17F / face_count); // Brown/Blind
                }
            }
            else if (random_eye == 1){
                setGene(FACE, 3F / face_count); // Light Green
                if (hasHeterochromia){
                    setGene(FACE, 2F / face_count); // Light Green/Teal
                }
            }
            else{
                setGene(FACE, 9F / face_count); // Gray
                if (hasHeterochromia){
                    setGene(FACE, 7F / face_count); // Gray/Gold
                }
            }
            break;
        }

        setGene(EUMELANIN, random.nextFloat());
        setGene(PHEOMELANIN, random.nextFloat());
    }

    private String getRaceByBiome(){
            Biome biome = entity.asEntity().getWorld().getBiome(entity.asEntity().getBlockPos()).value();

            Identifier biomeId = entity.asEntity().getWorld().getRegistryManager()
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

    /**
     * Produces a float between 0 and 1, weighted at 0.5
     */
    private float centeredRandom( Float centeredFloat ) {
        return Math.min(1, Math.max(0, (random.nextFloat() - 0.5F) * (random.nextFloat() - 0.5F) + centeredFloat));
    }

    public void combine(Genetics mother, Genetics father) {
        for (GeneType type : GENOMES) {
            getGenome(type).mutate(mother, father);
        }
    }

    public void combine(Genetics mother, Genetics father, long seed) {
        Random old = random;
        random = Random.create(seed);
        combine(mother, father);
        random = old;
    }

    public class Gene {
        private final GeneType type;

        public Gene(GeneType type) {
            this.type = type;
        }

        public GeneType getType() {
            return type;
        }

        public float get() {
            return entity.getTrackedValue(type.parameter);
        }

        public void set(float value) {
            entity.setTrackedValue(type.parameter, value);
        }

        public void randomize() {
            set(random.nextFloat());
        }

        public void mutate(Genetics mother, Genetics father) {
            float m = mother.getGene(type);
            float f = father.getGene(type);
            float interpolation = random.nextFloat();
            float mutation = (random.nextFloat() - 0.5f) * 0.2f;
            float g = m * interpolation + f * (1.0f - interpolation) + mutation;

            set((float) Math.min(1.0, Math.max(0.0, g)));
        }
    }

    public static class GeneType implements Comparable<GeneType> {
        private final String key;
        private final CDataParameter<Float> parameter;

        public GeneType(String key) {
            this.key = key;
            parameter = CParameter.create(key, 0.5f);
            GENOMES.add(this);
        }

        public String key() {
            return key;
        }

        public String getTranslationKey() {
            return key().replace("_", ".");
        }

        public CDataParameter<Float> getParam() {
            return parameter;
        }

        @Override
        public int compareTo(GeneType o) {
            return key().compareTo(o.key());
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GeneType geneType && geneType.key().equals(key());
        }
    }
}
