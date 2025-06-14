package net.mca.entity.ai;

import net.mca.Config;
import net.mca.client.model.CommonVillagerModel;
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

    // FIR
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

    // FYKH
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

        // temperature
        float temp = entity.asEntity().getWorld().getBiome(entity.asEntity().getBlockPos()).value().getTemperature();

        float height = entity.asEntity().getBlockPos().getY();
        height -= entity.asEntity().getWorld().getSeaLevel();
        height /= 128;

        Biome biome = entity.asEntity().getWorld().getBiome(entity.asEntity().getBlockPos()).value();

        Identifier biomeId = entity.asEntity().getWorld().getRegistryManager()
            .get(RegistryKeys.BIOME)
            .getId(biome);

        String biomeName = biomeId != null ? biomeId.toString() : "unknown";

        // immigrants
        if (random.nextFloat() < Config.getInstance().geneticImmigrantChance) {
            String[] raceArray = {
                "bleakisles:folken_isles_orbeitor_taiga", // Northern Folk
                "bleakisles:ahrathi_isles_desert", // Ahrathi
                "bleakisles:blundercast_jungle", // Sea Nomad
                "bleakisles:vingoria_ashland", // Ashen
                "bleakisles:crimson_isles_forest", // Crimson Elf
                "bleakisles:eidlihas_plains", // Sun Elf
                "bleakisles:shattered_coasts_plains_north", // Pale Elf
                "bleakisles:omn_sunflower_plains", // Sea Elf
                "bleakisles:silver_isles_plains", // Silver Orc
                "bleakisles:crimson_isles_jungle", // Crimson Orc
                "bleakisles:eidlihas_wooded_badlands", // Elder Orc
            };

            int randomRaceIndex = random.nextInt(raceArray.length);

            biomeName = raceArray[randomRaceIndex];
        }

        boolean hasHeterochromia = entity.getTraits().hasTrait(Traits.HETEROCHROMIA);
        Integer random_eye = MathHelper.nextInt(Random.create(), 0, 2);

        switch (biomeName) {
            case "bleakisles:ahrathi_isles_badlands", "bleakisles:sandstone_basin_savannah", "bleakisles:ahrathi_isles_desert", "bleakisles:sandstone_basin_desert", "bleakisles:sandstone_basin_jungle", "bleakisles:ahrathi_isles_jungle_south", "bleakisles:ahrathi_isles_jungle_east":
                // Ahrathi
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_ahrathi, melanin_max_ahrathi));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
                setGene(SIZE, centeredRandom(0.6F));
                setGene(WIDTH, centeredRandom(0.5F));

                if (random_eye == 0){
                    setGene(FACE, 0); // Brown
                    if (hasHeterochromia){
                        setGene(FACE, 17); // Brown/Blind
                    }
                }
                else if (random_eye == 1){
                    setGene(FACE, 1); // Hazel
                    if (hasHeterochromia){
                        setGene(FACE, 14); // Light Blue/Brown
                    }
                }
                else{
                    setGene(FACE, 11); // Gold
                    if (hasHeterochromia){
                        setGene(FACE, 7); // Gray/Gold
                    }
                }
                break;

            case "bleakisles:blundercast_jungle":
                // Sea Nomad
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_sea_nomad, melanin_max_sea_nomad));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
                setGene(SIZE, centeredRandom(0.65F));
                setGene(WIDTH, centeredRandom(0.55F));

                if (random_eye == 0){
                    setGene(FACE, 0); // Brown
                    if (hasHeterochromia){
                        setGene(FACE, 17); // Brown/Blind
                    }
                }
                else if (random_eye == 1){
                    setGene(FACE, 3); // Light Green
                    if (hasHeterochromia){
                        setGene(FACE, 2); // Light Green/Teal
                    }
                }
                else{
                    setGene(FACE, 9); // Gray
                    if (hasHeterochromia){
                        setGene(FACE, 7); // Gray/Gold
                    }
                }
                break;

            case "bleakisles:crimson_isles_mangrove_swamp", "bleakisles:crimson_isles_plains", "bleakisles:crimson_isles_forest":
                // Crimson Elf
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_crimson_elf, melanin_max_crimson_elf));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_gray, hemoglobin_max_gray));
                setGene(SIZE, centeredRandom(0.75F));
                setGene(WIDTH, centeredRandom(0.55F));

                if (random_eye == 0){
                    setGene(FACE, 6); // Red
                    if (hasHeterochromia){
                        setGene(FACE, 4); // Red/Purple
                    }
                }
                else if (random_eye == 1){
                    setGene(FACE, 8); // Purple
                    if (hasHeterochromia){
                        setGene(FACE, 4); // Red/Purple
                    }
                }
                else{
                    setGene(FACE, 9); // Gray
                    if (hasHeterochromia){
                        setGene(FACE, 5); // Brown/Purple
                    }
                }
                break;

            case "bleakisles:crimson_isles_jungle":
                // Crimson Orc
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_crimson_orc, melanin_max_crimson_orc));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_pinkRed, hemoglobin_max_pinkRed));
                setGene(SIZE, centeredRandom(0.85F));
                setGene(WIDTH, centeredRandom(95F));

                setGene(FACE, 17); // Red Orc
                if (hasHeterochromia){
                    setGene(FACE, 18); // Blind/Red Orc
                }
                break;

            case "bleakisles:eidlihas_wooded_badlands", "bleakisles:eidlihas_badlands":
                // Elder Orcs
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_elder_orc, melanin_max_elder_orc));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_green, hemoglobin_max_green));
                setGene(SIZE, centeredRandom(0.9F));
                setGene(WIDTH, centeredRandom(1F));

                if (random_eye == 0){
                    setGene(FACE, 17); // Red
                    if (hasHeterochromia){
                        setGene(FACE, 18); // Red/Blind
                    }
                }
                else if (random_eye == 1){
                    setGene(FACE, 19); // Blue
                    if (hasHeterochromia){
                        setGene(FACE, 21); // Blue/Green
                    }
                }
                else{
                    setGene(FACE, 20); // Green
                    if (hasHeterochromia){
                        setGene(FACE, 19); // Red/Green
                    }
                }
                break;

            case "bleakisles:eidlihas_birch_forest":
                // Gray or Sun Elves
                Integer random_selection = MathHelper.nextBetween(Random.create(), 0, 1);
                if (random_selection == 0){ // Gray Elf
                    setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_gray_elf, melanin_max_gray_elf));
                    setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_gray, hemoglobin_max_gray));
                    setGene(SIZE, centeredRandom(0.77F));
                    setGene(WIDTH, centeredRandom(0.57F));

                    if (random_eye == 0){
                        setGene(FACE, 6); // Red
                        if (hasHeterochromia){
                            setGene(FACE, 4); // Red/Purple
                        }
                    }
                    else if (random_eye == 1){
                        setGene(FACE, 8); // Purple
                        if (hasHeterochromia){
                            setGene(FACE, 4); // Red/Purple
                        }
                    }
                    else{
                        setGene(FACE, 9); // Gray
                        if (hasHeterochromia){
                            setGene(FACE, 5); // Brown/Purple
                        }
                    }
                }
                else{ // Sun Elf
                    setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_sun_elf, melanin_max_sun_elf));
                    setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_yellow, hemoglobin_max_yellow));
                    setGene(SIZE, centeredRandom(0.8F));
                    setGene(WIDTH, centeredRandom(0.6F));

                    if (random_eye == 0){
                        setGene(FACE, 11); // Gold
                        if (hasHeterochromia){
                            setGene(FACE, 10); // Gold/Red
                        }
                    }
                    else if (random_eye == 1){
                        setGene(FACE, 2); // Blue
                        if (hasHeterochromia){
                            setGene(FACE, 14); // Light Blue/Brown
                        }
                    }
                    else{
                        setGene(FACE, 3); // Green
                        if (hasHeterochromia){
                            setGene(FACE, 2); // Teal/Light Green
                        }
                    }
                }
                break;

            case "bleakisles:folken_isles_orbeitor_taiga", "bleakisles:folken_isles_ithlaer_taiga", "bleakisles:folken_isles_ithlaer_dark_forest", "bleakisles:folken_isles_orbeitor_snowy_taiga", "bleakisles:folken_isles_ithlaer_snowy_taiga", "bleakisles:folken_isles_naranir_dark_forest", "bleakisles:folken_isles_flower_forest", "bleakisles:folken_isles_aetlis_forest", "bleakisles:folken_isles_aetlis_swamp":
                // Northern Folk
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_northern_folk, melanin_max_northern_folk));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_human, hemoglobin_max_human));
                setGene(SIZE, centeredRandom(0.5F));
                setGene(WIDTH, centeredRandom(0.4F));

                if (random_eye == 0){
                    setGene(FACE, 1); // Hazel
                    if (hasHeterochromia){
                        setGene(FACE, 17); // Brown/Blind
                    }
                }
                else if (random_eye == 1){
                    setGene(FACE, 16); // Light Blue
                    if (hasHeterochromia){
                        setGene(FACE, 3); // Brown/Blue
                    }
                }
                else{
                    setGene(FACE, 3); // Light Green
                    if (hasHeterochromia){
                        setGene(FACE, 1); // Blue/Green
                    }
                }
                break;

            case "bleakisles:omn_sunflower_plains":
                // Sea Elves
                setGene(MELANIN, melanin_sea_elf);
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_blue, hemoglobin_max_blue));
                setGene(SIZE, centeredRandom(0.85F));
                setGene(WIDTH, centeredRandom(0.65F));

                setGene(FACE, 10); // Blind
                break;

            case "bleakisles:shattered_coasts_snowy_taiga_west", "bleakisles:shattered_coasts_snowy_plains", "bleakisles:shattered_coasts_snowy_taiga_east", "bleakisles:shattered_coasts_taiga_west", "bleakisles:shattered_coasts_taiga_east", "bleakisles:shattered_coasts_plains_north":
                // Pale Elf
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_pale_elf, melanin_max_pale_elf));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_gray, hemoglobin_max_gray));
                setGene(SIZE, centeredRandom(0.75F));
                setGene(WIDTH, centeredRandom(0.55F));

                if (random_eye == 0){
                    setGene(FACE, 14); // Light pink
                    if (hasHeterochromia){
                        setGene(FACE, 9); // Purple/Gray
                    }
                }
                else if (random_eye == 1){
                    setGene(FACE, 12); // Yellow
                    if (hasHeterochromia){
                        setGene(FACE, 10); // Gold/Red
                    }
                }
                else{
                    setGene(FACE, 16); // Light blue
                    if (hasHeterochromia){
                        setGene(FACE, 13); // Salmon/Gray
                    }
                }
                break;

            case "bleakisles:silver_isles_plains", "bleakisles:silver_isles_mushroom_fields", "bleakisles:silver_isles_forest":
                // Silver Orcs
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_silver_orc, melanin_max_silver_orc));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_blue, hemoglobin_max_blue));
                setGene(SIZE, centeredRandom(0.87F));
                setGene(WIDTH, centeredRandom(0.97F));

                if (random_eye == 0){
                    setGene(FACE, 17); // Red
                    if (hasHeterochromia){
                        setGene(FACE, 18); // Red/Blind
                    }
                }
                else if (random_eye == 1){
                    setGene(FACE, 19); // Blue
                    if (hasHeterochromia){
                        setGene(FACE, 21); // Blue/Green
                    }
                }
                else{
                    setGene(FACE, 18); // Green
                    if (hasHeterochromia){
                        setGene(FACE, 20); // Brown/Blind
                    }
                }
                break;

            case "bleakisles:eidlihas_plains":
                // Sun Elf
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_sun_elf, melanin_max_sun_elf));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_yellow, hemoglobin_max_yellow));
                setGene(SIZE, centeredRandom(0.8F));
                setGene(WIDTH, centeredRandom(0.6F));

                    if (random_eye == 0){
                        setGene(FACE, 11); // Gold
                        if (hasHeterochromia){
                            setGene(FACE, 10); // Gold/Red
                        }
                    }
                    else if (random_eye == 1){
                        setGene(FACE, 2); // Blue
                        if (hasHeterochromia){
                            setGene(FACE, 14); // Light Blue/Brown
                        }
                    }
                    else{
                        setGene(FACE, 3); // Green
                        if (hasHeterochromia){
                            setGene(FACE, 2); // Teal/Light Green
                        }
                    }
                break;

            case "bleakisles:vingoria_dark_forest", "bleakisles:vingoria_jungle", "bleakisles:vingoria_ashland", "bleakisles:vingoria_plains":
                // Ashen
                setGene(MELANIN, MathHelper.nextFloat(random, melanin_min_ashen, melanin_max_ashen));
                setGene(HEMOGLOBIN, MathHelper.nextFloat(random, hemoglobin_min_yellow, hemoglobin_max_yellow));
                setGene(SIZE, centeredRandom(0.45F));
                setGene(WIDTH, centeredRandom(0.35F));

                if (random_eye == 0){
                    setGene(FACE, 9); // Gray
                    if (hasHeterochromia){
                        setGene(FACE, 9); // Purple/Gray
                    }
                }
                else if (random_eye == 1){
                    setGene(FACE, 6); // Red
                    if (hasHeterochromia){
                        setGene(FACE, 13); // Salmon/Gray
                    }
                }
                else{
                    setGene(FACE, 1); // Brown
                    if (hasHeterochromia){
                        setGene(FACE, 5); // Brown/Purple
                    }
                }
                break;

            default:
                setGene(MELANIN, MathHelper.clamp(temperatureBaseRandom(temp) - height * 0.2f, 0, 1));
                setGene(HEMOGLOBIN, MathHelper.clamp(temperatureBaseRandom(temp) * 0.5f + height * 0.5f, 0, 1));
                // size is more centered
                setGene(SIZE, centeredRandom(0.5F));
                setGene(WIDTH, centeredRandom(0.5F));
                break;
        }

        setGene(EUMELANIN, random.nextFloat());
        setGene(PHEOMELANIN, random.nextFloat());
    }

    /**
     * Produces a float between 0 and 1, weighted at 0.5
     */
    private float centeredRandom( Float centeredFloat ) {
        return Math.min(1, Math.max(0, (random.nextFloat() - 0.5F) * (random.nextFloat() - 0.5F) + centeredFloat));
    }

    private float temperatureBaseRandom(float temp) {
        return (random.nextFloat() - 0.5F) * 0.35F + temp * 0.4F + 0.1F;
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
