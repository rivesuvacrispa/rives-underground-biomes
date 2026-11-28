package com.rivesuvacrispa.rivesundergroundbiomes.world;

import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.SeededNoise;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

// runs right after carvers, before any feature
// caves and aquifers already exist at this point
// every block depends only on its position so chunks agree at their borders
public final class ReefLayers {
    // mask nodes every 8 blocks
    private static final int CELL = 8;
    // a cave needs every node this many cells around inside the reef
    // 16+ blocks of rock to anything else
    private static final int ERODE_XZ = 2;
    private static final int ERODE_Y = 1;
    // nodes before the chunk
    // the erosion window around tube anchors up to one node outside the chunk
    private static final int PAD = ERODE_XZ + 1;
    // the 3 nodes a chunk interpolates between plus the padding on both sides
    private static final int NODES_XZ = 3 + 2 * PAD;
    // the fill of other caves ends on a noisy surface about a node inside the reef
    private static final double FILL_DEPTH = 1; // erosion steps a node survives
    private static final double FILL_WOBBLE = 0.6;
    private static final double FILL_ROUGH = 0.25;
    private static final int STRUCTURE_MARGIN = 4;
    // structure pieces reach about 8 chunks from their start
    // every chunk that close has its starts at the carver stage
    // so the result does not depend on generation order
    private static final int STRUCTURE_CHUNKS = 8;

    private static final int ROOF = 15;
    private static final int PERIOD = 24;
    private static final double THICKNESS = 9;
    // carvers and ocean floor dips above this stay open to the sea, water touching water
    private static final int FILL_BELOW_FLOOR = 6;
    // the smoothed floor sits above dips in the real one
    private static final int MIN_ROOF = 10;
    private static final int BEDROCK_CLEARANCE = 6;
    private static final double MIN_HEIGHT = 2.5;

    // one tube chance per gap between two layers in each cell
    private static final int TUBE_CELL = 22;
    private static final float TUBE_CHANCE = 0.8F;
    private static final int TUBE_MARGIN = 3;
    // bend + radius + wall
    private static final int TUBE_REACH = 8;
    // both layers this high at the anchor
    // without the roof limit that only the anchor chunk knows
    private static final double TUBE_MIN_LAYER = 4;
    private static final int HOLE_CELL = 51;
    private static final float HOLE_CHANCE = 0.33F;
    private static final int HOLE_MARGIN = 12;
    private static final double HOLE_MIN_RADIUS = 2.5;
    private static final double HOLE_MAX_RADIUS = 4;
    // funnel widening at the sea floor + the rock seal around it
    private static final double HOLE_FLARE = 1.5;
    private static final double HOLE_SEAL = 2;

    private static final long DRIFT_SALT = 0x5EEF_0001L;
    private static final long THICKNESS_SALT = 0x5EEF_0002L;
    private static final long CHAMBER_SALT = 0x5EEF_0003L;
    private static final long PILLAR_SALT = 0x5EEF_0004L;
    private static final long ROUGH_SALT = 0x5EEF_0005L;
    private static final long TUBE_SALT = 0x5EEF_0006L;
    private static final long HOLE_SALT = 0x5EEF_0007L;
    private static final long EDGE_SALT = 0x5EEF_0008L;

    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
    private static final List<BlockState> CORALS = List.of(
            Blocks.TUBE_CORAL_BLOCK.defaultBlockState(),
            Blocks.BRAIN_CORAL_BLOCK.defaultBlockState(),
            Blocks.BUBBLE_CORAL_BLOCK.defaultBlockState(),
            Blocks.FIRE_CORAL_BLOCK.defaultBlockState(),
            Blocks.HORN_CORAL_BLOCK.defaultBlockState());

    // per world
    // a mask column is shared by ~20 chunks, a floor corner by 4
    private static final Map<RandomState, WorldCache> CACHES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final int CACHE_LIMIT = 65536;

    private final ChunkAccess chunk;
    private final long seed;
    private final ChunkGenerator generator;
    private final RandomState randomState;
    private final WorldCache cache;
    private final int minX;
    private final int minZ;
    private final int minY;
    private final int nodeX0;
    private final int nodeZ0;
    private final int nodeY0;
    private final int nodesY;
    // a reef biome or reef surface biome away from avoided structures
    private final boolean[] reef;
    // reef nodes with the whole erosion window inside the reef
    private final float[] cave;
    private final ImprovedNoise drift;
    private final ImprovedNoise thickness;
    private final ImprovedNoise chambers;
    private final ImprovedNoise pillars;
    private final ImprovedNoise rough;
    private final ImprovedNoise edge;

    private record Layer(double center, double thick, double open) {}

    private record BlueHole(double x, double z, double radius) {}

    private ReefLayers(
            ChunkAccess chunk, long seed, ChunkGenerator generator, RandomState randomState, WorldCache cache) {
        this.chunk = chunk;
        this.seed = seed;
        this.generator = generator;
        this.randomState = randomState;
        this.cache = cache;
        ChunkPos pos = chunk.getPos();
        this.minX = pos.getMinBlockX();
        this.minZ = pos.getMinBlockZ();
        this.minY = chunk.getMinBuildHeight();
        this.nodeX0 = (minX >> 3) - PAD;
        this.nodeZ0 = (minZ >> 3) - PAD;
        this.nodeY0 = Math.floorDiv(minY, CELL);
        this.nodesY = Math.floorDiv(generator.getSeaLevel(), CELL) - nodeY0 + 2;
        this.reef = new boolean[NODES_XZ * NODES_XZ * nodesY];
        this.cave = new float[reef.length];
        this.drift = SeededNoise.get(seed, DRIFT_SALT);
        this.thickness = SeededNoise.get(seed, THICKNESS_SALT);
        this.chambers = SeededNoise.get(seed, CHAMBER_SALT);
        this.pillars = SeededNoise.get(seed, PILLAR_SALT);
        this.rough = SeededNoise.get(seed, ROUGH_SALT);
        this.edge = SeededNoise.get(seed, EDGE_SALT);
    }

    public static void carve(
            ChunkGenerator generator, WorldGenRegion region, long seed, RandomState randomState, ChunkAccess chunk) {
        // a chunk without any reef cell has no layer of its own
        // the solid margin around neighbor layers is inside that neighbor
        if (!hasReef(chunk)) return;
        WorldCache cache = CACHES.computeIfAbsent(randomState, state -> new WorldCache());
        ReefLayers layers = new ReefLayers(chunk, seed, generator, randomState, cache);
        if (!layers.findReef(generator.getBiomeSource(), randomState.sampler())) return;
        layers.avoidStructures(region);
        layers.fill();
    }

    // a reef biome or a reef surface biome
    public static boolean isReef(Holder<Biome> biome) {
        return Zone.REEF.isBiome(biome) || Zone.ofSurface(biome) == Zone.REEF;
    }

    // below the sea floor of a reef next to water
    // air there would drain the layers
    public static boolean keepsWaterAt(WorldGenLevel level, BlockPos pos) {
        if (pos.getY() >= level.getSeaLevel()
                || !level.hasChunk(
                        SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()))
                || pos.getY() >= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ())
                || !isReef(level.getBiome(pos))) return false;
        for (Direction direction : Direction.values()) {
            BlockPos next = pos.relative(direction);
            if (level.hasChunk(SectionPos.blockToSectionCoord(next.getX()), SectionPos.blockToSectionCoord(next.getZ()))
                    && level.getFluidState(next).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    private static boolean hasReef(ChunkAccess chunk) {
        for (LevelChunkSection section : chunk.getSections()) {
            if (section.getBiomes().maybeHas(ReefLayers::isReef)) return true;
        }
        return false;
    }

    // neighbors may not have their biomes yet at the carver stage, so the nodes ask the biome source
    private boolean findReef(BiomeSource source, Climate.Sampler sampler) {
        boolean any = false;
        for (int ix = 0; ix < NODES_XZ; ix++) {
            for (int iz = 0; iz < NODES_XZ; iz++) {
                int nx = nodeX0 + ix;
                int nz = nodeZ0 + iz;
                boolean[] column = cache.column(ChunkPos.asLong(nx, nz), () -> sampleColumn(nx, nz, source, sampler));
                for (int iy = 0; iy < nodesY; iy++) {
                    reef[index(ix, iy, iz)] = column[iy];
                    any |= column[iy];
                }
            }
        }
        return any;
    }

    private boolean[] sampleColumn(int nx, int nz, BiomeSource source, Climate.Sampler sampler) {
        boolean[] column = new boolean[nodesY];
        int maxY = chunk.getMaxBuildHeight() - 1;
        int qx = QuartPos.fromBlock(nx * CELL);
        int qz = QuartPos.fromBlock(nz * CELL);
        for (int iy = 0; iy < nodesY; iy++) {
            int qy = QuartPos.fromBlock(Mth.clamp((nodeY0 + iy) * CELL, minY, maxY));
            column[iy] = isReef(source.getNoiseBiome(qx, qy, qz, sampler));
        }
        return column;
    }

    private void avoidStructures(WorldGenRegion region) {
        Registry<Structure> registry = region.registryAccess().registryOrThrow(Registries.STRUCTURE);
        BoundingBox area = new BoundingBox(
                        nodeX0 * CELL,
                        minY,
                        nodeZ0 * CELL,
                        (nodeX0 + NODES_XZ - 1) * CELL,
                        (nodeY0 + nodesY - 1) * CELL,
                        (nodeZ0 + NODES_XZ - 1) * CELL)
                .inflatedBy(STRUCTURE_MARGIN);
        List<BoundingBox> boxes = new ArrayList<>();
        ChunkPos pos = chunk.getPos();
        for (int dx = -STRUCTURE_CHUNKS; dx <= STRUCTURE_CHUNKS; dx++) {
            for (int dz = -STRUCTURE_CHUNKS; dz <= STRUCTURE_CHUNKS; dz++) {
                for (StructureStart start :
                        region.getChunk(pos.x + dx, pos.z + dz).getAllStarts().values()) {
                    if (!start.isValid()
                            || !start.getBoundingBox().intersects(area)
                            || !registry.wrapAsHolder(start.getStructure()).is(ModStructureTags.REEF_AVOIDS)) continue;
                    for (StructurePiece piece : start.getPieces())
                        boxes.add(piece.getBoundingBox().inflatedBy(STRUCTURE_MARGIN));
                }
            }
        }
        if (boxes.isEmpty()) return;
        for (int ix = 0; ix < NODES_XZ; ix++) {
            for (int iz = 0; iz < NODES_XZ; iz++) {
                for (int iy = 0; iy < nodesY; iy++) {
                    int x = (nodeX0 + ix) * CELL;
                    int y = (nodeY0 + iy) * CELL;
                    int z = (nodeZ0 + iz) * CELL;
                    for (BoundingBox box : boxes) {
                        if (box.isInside(x, y, z)) {
                            reef[index(ix, iy, iz)] = false;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void fill() {
        float[] solid = new float[reef.length];
        boolean anySolid = false;
        for (int ix = PAD; ix <= PAD + 2; ix++) {
            for (int iz = PAD; iz <= PAD + 2; iz++) {
                for (int iy = 0; iy < nodesY; iy++) {
                    int i = index(ix, iy, iz);
                    solid[i] = depth(ix, iy, iz);
                    anySolid |= reef[i];
                }
            }
        }
        if (!anySolid) return;
        for (int ix = ERODE_XZ; ix < NODES_XZ - ERODE_XZ; ix++) {
            for (int iz = ERODE_XZ; iz < NODES_XZ - ERODE_XZ; iz++) {
                for (int iy = 0; iy < nodesY; iy++) cave[index(ix, iy, iz)] = reefAround(ix, iy, iz, ERODE_XZ) ? 1 : 0;
            }
        }

        int maxLayers = (int) ((maxCornerFloor() - minY) / PERIOD) + 1;
        List<ReefTube> tubes = tubes();
        List<BlueHole> holes = holes();

        int[] fillTop = new int[256];
        float[] solidColumn = new float[256 * nodesY];
        double[] bottoms = new double[256 * maxLayers];
        double[] tops = new double[256 * maxLayers];
        ReefTube[][] columnTubes = new ReefTube[256][];
        double[] holeDistance = new double[256];
        double[] holeRadius = new double[256];
        int[] holeBottom = new int[256];
        int[] holeTop = new int[256];
        float[] caveColumn = new float[nodesY];
        int highest = minY - 1;
        for (int lz = 0; lz < 16; lz++) {
            for (int lx = 0; lx < 16; lx++) {
                int column = lz * 16 + lx;
                int x = minX + lx;
                int z = minZ + lz;
                double floor = floorAt(x, z);
                fillTop[column] = Mth.floor(floor) - FILL_BELOW_FLOOR;
                // the real floor after the carvers, canyons deeper than the fill line ignored
                int realFloor = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, lx, lz) + 1;
                double roofBottom = Math.max(Math.min(realFloor, floor), fillTop[column]) - MIN_ROOF;
                highest = Math.max(highest, fillTop[column]);
                double gx = (x - nodeX0 * CELL) / (double) CELL;
                double gz = (z - nodeZ0 * CELL) / (double) CELL;
                for (int iy = 0; iy < nodesY; iy++) {
                    solidColumn[column * nodesY + iy] = bilerp(solid, gx, iy, gz);
                    caveColumn[iy] = bilerp(cave, gx, iy, gz);
                }
                int offset = column * maxLayers;
                layers(x, z, floor, roofBottom, caveColumn, bottoms, tops, offset, maxLayers);
                columnTubes[column] =
                        tubes.stream().filter(tube -> tube.reaches(x, z)).toArray(ReefTube[]::new);

                holeDistance[column] = Double.NaN;
                // only above an open first layer since elsewhere the hole would end in rock
                if (!Double.isNaN(tops[offset])) {
                    for (BlueHole hole : holes) {
                        double distance = Math.hypot(x + 0.5 - hole.x(), z + 0.5 - hole.z());
                        if (distance > hole.radius() + HOLE_FLARE + HOLE_SEAL) continue;
                        holeDistance[column] = distance;
                        holeRadius[column] = hole.radius();
                        holeBottom[column] = Mth.floor(tops[offset]);
                        holeTop[column] = realFloor - 1;
                        highest = Math.max(highest, holeTop[column]);
                    }
                }
            }
        }

        boolean changed = false;
        for (int sectionY = SectionPos.blockToSectionCoord(minY);
                sectionY <= SectionPos.blockToSectionCoord(highest);
                sectionY++) {
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
            int baseY = SectionPos.sectionToBlockCoord(sectionY);
            section.acquire();
            try {
                for (int lz = 0; lz < 16; lz++) {
                    for (int lx = 0; lx < 16; lx++) {
                        int column = lz * 16 + lx;
                        int x = minX + lx;
                        int z = minZ + lz;
                        boolean hole = !Double.isNaN(holeDistance[column]);
                        int top = Math.min(Math.max(fillTop[column], hole ? holeTop[column] : minY), baseY + 15);
                        for (int y = baseY; y <= top; y++) {
                            BlockState state = section.getBlockState(lx, y & 15, lz);
                            if (state.is(Blocks.BEDROCK)) continue;
                            double holeReach = hole ? holeReach(column, y, holeRadius, holeBottom, holeTop) : -1;
                            BlockState placed;
                            if (inCave(y, bottoms, tops, column * maxLayers, maxLayers)
                                    || inTube(columnTubes[column], x, y, z)
                                    || hole && y >= holeBottom[column] && holeDistance[column] <= holeReach) {
                                placed = state == WATER ? null : WATER;
                            } else {
                                placed = tubeWall(columnTubes[column], x, y, z);
                                if (placed == null && isOpen(state)) {
                                    boolean filled =
                                            y <= fillTop[column] && fills(lerpY(solidColumn, column, y), x, y, z);
                                    // the band between the fill line and the sea floor around a blue hole
                                    boolean sealed = hole && holeDistance[column] <= holeReach + HOLE_SEAL;
                                    if (filled || sealed) placed = solidAt(x, y, z);
                                }
                            }
                            if (placed != null) {
                                section.setBlockState(lx, y & 15, lz, placed, false);
                                changed = true;
                            }
                        }
                    }
                }
            } finally {
                section.release();
            }
        }
        for (ReefTube tube : tubes) changed |= placeVent(tube);
        // filled canyons and blue holes change the sea floor that later features read
        if (changed)
            Heightmap.primeHeightmaps(
                    chunk, EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
    }

    // air, fluids and falling blocks
    // sand and gravel floors of old caves would fall into the layers
    private static boolean isOpen(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty() || state.getBlock() instanceof FallingBlock;
    }

    private static double holeReach(int column, int y, double[] radius, int[] bottom, int[] top) {
        double t = top[column] > bottom[column]
                ? Mth.clamp((y - bottom[column]) / (double) (top[column] - bottom[column]), 0, 1)
                : 1;
        return radius[column] + HOLE_FLARE * t;
    }

    private Layer layer(int x, int z, double floor, int k) {
        double ceiling = floor - ROOF - k * PERIOD;
        double slot = k * 13.7;
        double thick = THICKNESS + 1.5 * thickness.noise(x / 24.0, slot, z / 24.0);
        double center = ceiling - thick / 2 + 4 * drift.noise(x / 48.0, slot, z / 48.0);
        // mostly one open floor with a few closed chambers and some rock pillars
        double open = smoothstep(-0.42, -0.25, chambers.noise(x / 40.0, slot, z / 40.0))
                * (1 - smoothstep(0.32, 0.45, pillars.noise(x / 10.0, slot, z / 10.0)));
        return new Layer(center, thick, open);
    }

    private void layers(
            int x,
            int z,
            double floor,
            double roofBottom,
            float[] caveColumn,
            double[] bottoms,
            double[] tops,
            int offset,
            int maxLayers) {
        Arrays.fill(bottoms, offset, offset + maxLayers, Double.NaN);
        Arrays.fill(tops, offset, offset + maxLayers, Double.NaN);
        for (int k = 0; k < maxLayers; k++) {
            if (floor - ROOF - k * PERIOD < minY + BEDROCK_CLEARANCE) break;
            Layer layer = layer(x, z, floor, k);
            double center = layer.center();
            double h = layer.thick() * layer.open() * smoothstep(0, 1, lerpNodes(caveColumn, center));
            // shrink towards bedrock and the sea floor instead of flattening against them
            h = Math.min(h, 2 * (center - (minY + BEDROCK_CLEARANCE)));
            // the bumps add up to 1 block on top
            h = Math.min(h, 2 * (roofBottom - 1 - center));
            if (h < MIN_HEIGHT) continue;
            double bumps = Math.min(1, h / 4);
            double slot = k * 13.7;
            double bottom = center - h / 2 + bumps * rough.noise(x / 6.0, slot + 100, z / 6.0);
            double top = center + h / 2 + bumps * rough.noise(x / 6.0, slot + 200, z / 6.0);
            if (top - bottom < MIN_HEIGHT) continue;
            bottoms[offset + k] = bottom;
            tops[offset + k] = top;
        }
    }

    private static boolean inCave(int y, double[] bottoms, double[] tops, int offset, int count) {
        double center = y + 0.5;
        for (int i = offset; i < offset + count; i++) {
            if (center > bottoms[i] && center < tops[i]) return true;
        }
        return false;
    }

    // tubes from cells whose anchors are within reach of this chunk
    private List<ReefTube> tubes() {
        List<ReefTube> tubes = new ArrayList<>();
        int cellX0 = Math.floorDiv(minX - TUBE_REACH, TUBE_CELL);
        int cellX1 = Math.floorDiv(minX + 15 + TUBE_REACH, TUBE_CELL);
        int cellZ0 = Math.floorDiv(minZ - TUBE_REACH, TUBE_CELL);
        int cellZ1 = Math.floorDiv(minZ + 15 + TUBE_REACH, TUBE_CELL);
        for (int cellX = cellX0; cellX <= cellX1; cellX++) {
            for (int cellZ = cellZ0; cellZ <= cellZ1; cellZ++) {
                for (int k = 0; ; k++) {
                    RandomSource random = cellRandom(TUBE_SALT, cellX, cellZ, k);
                    int ax = cellX * TUBE_CELL + TUBE_MARGIN + random.nextInt(TUBE_CELL - 2 * TUBE_MARGIN);
                    int az = cellZ * TUBE_CELL + TUBE_MARGIN + random.nextInt(TUBE_CELL - 2 * TUBE_MARGIN);
                    double floor = floorAt(ax, az);
                    if (floor - ROOF - (k + 1) * PERIOD < minY + BEDROCK_CLEARANCE) break;
                    if (random.nextFloat() >= TUBE_CHANCE) continue;
                    if (ax < minX - TUBE_REACH || ax > minX + 15 + TUBE_REACH) continue;
                    if (az < minZ - TUBE_REACH || az > minZ + 15 + TUBE_REACH) continue;
                    Layer upper = layer(ax, az, floor, k);
                    Layer lower = layer(ax, az, floor, k + 1);
                    if (!opensAt(ax, az, upper) || !opensAt(ax, az, lower)) continue;
                    if (lower.center() - lower.thick() / 2 < minY + BEDROCK_CLEARANCE) continue;
                    float kind = random.nextFloat();
                    // a quarter carries soul sand bubbling up, a quarter magma pulling down
                    BlockState vent = kind < 0.25F
                            ? Blocks.SOUL_SAND.defaultBlockState()
                            : kind < 0.5F ? Blocks.MAGMA_BLOCK.defaultBlockState() : null;
                    // vent tubes stay straight so the bubble column runs through them
                    double bend = vent == null ? 1.5 + random.nextDouble() * 1.5 : 0;
                    double angle = random.nextDouble() * Math.PI * 2;
                    // 2 to 5 blocks wide
                    // an axis off the block center gives the even widths
                    double radius = 1.0 + random.nextDouble() * 1.5;
                    double offsetX = vent == null ? random.nextDouble() : 0.5;
                    double offsetZ = vent == null ? random.nextDouble() : 0.5;
                    BlockState wall = CORALS.get(random.nextInt(CORALS.size()));
                    tubes.add(new ReefTube(
                            ax + offsetX,
                            az + offsetZ,
                            Math.cos(angle) * bend,
                            Math.sin(angle) * bend,
                            upper.center(),
                            lower.center(),
                            radius,
                            wall,
                            vent));
                }
            }
        }
        return tubes;
    }

    private boolean opensAt(int x, int z, Layer layer) {
        return layer.thick() * layer.open() * smoothstep(0, 1, caveAt(x + 0.5, layer.center(), z + 0.5))
                >= TUBE_MIN_LAYER;
    }

    private List<BlueHole> holes() {
        List<BlueHole> holes = new ArrayList<>();
        int reach = (int) Math.ceil(HOLE_MAX_RADIUS + HOLE_FLARE + HOLE_SEAL);
        for (int cellX = Math.floorDiv(minX - reach, HOLE_CELL);
                cellX <= Math.floorDiv(minX + 15 + reach, HOLE_CELL);
                cellX++) {
            for (int cellZ = Math.floorDiv(minZ - reach, HOLE_CELL);
                    cellZ <= Math.floorDiv(minZ + 15 + reach, HOLE_CELL);
                    cellZ++) {
                RandomSource random = cellRandom(HOLE_SALT, cellX, cellZ, 0);
                if (random.nextFloat() >= HOLE_CHANCE) continue;
                double x = cellX * HOLE_CELL + HOLE_MARGIN + random.nextInt(HOLE_CELL - 2 * HOLE_MARGIN) + 0.5;
                double z = cellZ * HOLE_CELL + HOLE_MARGIN + random.nextInt(HOLE_CELL - 2 * HOLE_MARGIN) + 0.5;
                holes.add(new BlueHole(
                        x, z, HOLE_MIN_RADIUS + random.nextDouble() * (HOLE_MAX_RADIUS - HOLE_MIN_RADIUS)));
            }
        }
        return holes;
    }

    private RandomSource cellRandom(long salt, int cellX, int cellZ, int k) {
        return RandomSource.create(seed ^ salt ^ cellX * 341873128712L ^ cellZ * 132897987541L ^ k * 42317861L);
    }

    @Nullable
    private static BlockState tubeWall(ReefTube[] tubes, int x, int y, int z) {
        for (ReefTube tube : tubes) {
            if (tube.isWall(x, y, z)) return tube.wall();
        }
        return null;
    }

    private static boolean inTube(ReefTube[] tubes, int x, int y, int z) {
        for (ReefTube tube : tubes) {
            if (tube.inside(x, y, z)) return true;
        }
        return false;
    }

    private boolean placeVent(ReefTube tube) {
        BlockState vent = tube.vent();
        int x = Mth.floor(tube.x());
        int z = Mth.floor(tube.z());
        if (vent == null || x < minX || x >= minX + 16 || z < minZ || z >= minZ + 16) return false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, Mth.floor(tube.bottom()), z);
        if (chunk.getBlockState(pos) != WATER) return false;
        while (pos.getY() > minY && chunk.getBlockState(pos.below()) == WATER) pos.move(0, -1, 0);
        BlockPos floor = pos.below();
        if (chunk.getBlockState(floor).is(Blocks.BEDROCK)) return false;
        chunk.setBlockState(floor, vent, false);
        BlockState bubbles = Blocks.BUBBLE_COLUMN
                .defaultBlockState()
                .setValue(BubbleColumnBlock.DRAG_DOWN, vent.is(Blocks.MAGMA_BLOCK));
        for (; chunk.getBlockState(pos) == WATER; pos.move(0, 1, 0)) chunk.setBlockState(pos, bubbles, false);
        return true;
    }

    // the noise only floor ignores carvers so a canyon does not drag the layers down
    private double floorAt(int x, int z) {
        int cx = SectionPos.blockToSectionCoord(x);
        int cz = SectionPos.blockToSectionCoord(z);
        LevelHeightAccessor height = chunk.getHeightAccessorForGeneration();
        return Mth.lerp2(
                (x & 15) / 16.0,
                (z & 15) / 16.0,
                cache.floor(cx, cz, generator, height, randomState),
                cache.floor(cx + 1, cz, generator, height, randomState),
                cache.floor(cx, cz + 1, generator, height, randomState),
                cache.floor(cx + 1, cz + 1, generator, height, randomState));
    }

    private double maxCornerFloor() {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        LevelHeightAccessor height = chunk.getHeightAccessorForGeneration();
        return Math.max(
                Math.max(
                        cache.floor(cx, cz, generator, height, randomState),
                        cache.floor(cx + 1, cz, generator, height, randomState)),
                Math.max(
                        cache.floor(cx, cz + 1, generator, height, randomState),
                        cache.floor(cx + 1, cz + 1, generator, height, randomState)));
    }

    private static BlockState solidAt(int x, int y, int z) {
        if (y >= 8) return STONE;
        if (y < 0) return DEEPSLATE;
        return Math.floorMod(Mth.getSeed(x, y, z), 8) >= y ? DEEPSLATE : STONE;
    }

    private boolean fills(double depth, int x, int y, int z) {
        if (depth >= FILL_DEPTH + FILL_WOBBLE + FILL_ROUGH) return true;
        if (depth <= FILL_DEPTH - FILL_WOBBLE - FILL_ROUGH) return false;
        double wobble = FILL_WOBBLE * edge.noise(x / 16.0, y / 12.0, z / 16.0)
                + FILL_ROUGH * edge.noise(x / 5.0, y / 5.0 + 100, z / 5.0);
        return depth + wobble > FILL_DEPTH;
    }

    private int depth(int ix, int iy, int iz) {
        if (!reef[index(ix, iy, iz)]) return 0;
        for (int r = 1; r <= ERODE_XZ; r++) {
            if (!reefAround(ix, iy, iz, r)) return r;
        }
        return ERODE_XZ + 1;
    }

    // every node r around in x z and ERODE_Y around in y is reef
    private boolean reefAround(int ix, int iy, int iz, int r) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -ERODE_Y; dy <= ERODE_Y; dy++) {
                    int y = Mth.clamp(iy + dy, 0, nodesY - 1);
                    if (!reef[index(ix + dx, y, iz + dz)]) return false;
                }
            }
        }
        return true;
    }

    private float caveAt(double x, double y, double z) {
        double gx = (x - nodeX0 * CELL) / CELL;
        double gz = (z - nodeZ0 * CELL) / CELL;
        double gy = Mth.clamp((y - nodeY0 * CELL) / CELL, 0, nodesY - 1.001);
        int iy = Mth.floor(gy);
        return (float) Mth.lerp(gy - iy, bilerp(cave, gx, iy, gz), bilerp(cave, gx, iy + 1, gz));
    }

    private float bilerp(float[] nodes, double gx, int iy, double gz) {
        int ix = Mth.floor(gx);
        int iz = Mth.floor(gz);
        return (float) Mth.lerp2(
                gx - ix,
                gz - iz,
                nodes[index(ix, iy, iz)],
                nodes[index(ix + 1, iy, iz)],
                nodes[index(ix, iy, iz + 1)],
                nodes[index(ix + 1, iy, iz + 1)]);
    }

    private float lerpNodes(float[] column, double y) {
        double gy = Mth.clamp((y - nodeY0 * CELL) / CELL, 0, nodesY - 1.001);
        int iy = Mth.floor(gy);
        return (float) Mth.lerp(gy - iy, column[iy], column[iy + 1]);
    }

    private float lerpY(float[] columns, int column, int y) {
        double gy = Mth.clamp((y - nodeY0 * CELL) / (double) CELL, 0, nodesY - 1.001);
        int iy = Mth.floor(gy);
        int base = column * nodesY;
        return (float) Mth.lerp(gy - iy, columns[base + iy], columns[base + iy + 1]);
    }

    private int index(int ix, int iy, int iz) {
        return (iy * NODES_XZ + iz) * NODES_XZ + ix;
    }

    private static double smoothstep(double from, double to, double value) {
        double t = Mth.clamp((value - from) / (to - from), 0, 1);
        return t * t * (3 - 2 * t);
    }

    // shared by worldgen threads
    private static final class WorldCache {
        private final ConcurrentHashMap<Long, boolean[]> columns = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<Long, Integer> floors = new ConcurrentHashMap<>();

        boolean[] column(long key, Supplier<boolean[]> sample) {
            if (columns.size() > CACHE_LIMIT) columns.clear();
            return columns.computeIfAbsent(key, k -> sample.get());
        }

        int floor(int cx, int cz, ChunkGenerator generator, LevelHeightAccessor height, RandomState randomState) {
            if (floors.size() > CACHE_LIMIT) floors.clear();
            return floors.computeIfAbsent(
                    ChunkPos.asLong(cx, cz),
                    k -> generator.getBaseHeight(
                            SectionPos.sectionToBlockCoord(cx),
                            SectionPos.sectionToBlockCoord(cz),
                            Heightmap.Types.OCEAN_FLOOR_WG,
                            height,
                            randomState));
        }
    }
}
