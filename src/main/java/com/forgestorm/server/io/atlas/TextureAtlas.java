package com.forgestorm.server.io.atlas;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * HEAVILY MODIFIED!!! ONLY NEED REGION INFO, NOT A TEXTURE FOR THE SERVER...
 *
 * @author Nathan Sweet
 */
public class TextureAtlas {
    static final String[] tuple = new String[4];

    private final List<AtlasRegion> regions = new ArrayList<>();

    public static class TextureAtlasData {
        public static class Page {
            public final FileHandle textureFile;
            public final float width, height;

            public Page(FileHandle handle, float width, float height) {
                this.width = width;
                this.height = height;
                this.textureFile = handle;
            }
        }

        public static class Region {
            public Page page;
            public int index;
            public String name;
            public float offsetX;
            public float offsetY;
            public int originalWidth;
            public int originalHeight;
            public boolean rotate;
            public int left;
            public int top;
            public int width;
            public int height;
            public boolean flip;
            public int[] splits;
            public int[] pads;
        }

        final List<Page> pages = new ArrayList<>();
        final List<Region> regions = new ArrayList<>();

        @SuppressWarnings("unused")
        public TextureAtlasData(FileHandle packFile, FileHandle imagesDir, boolean flip) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 64);
            try {
                Page pageImage = null;
                while (true) {
                    String line = reader.readLine();
                    if (line == null) break;
                    if (line.trim().length() == 0)
                        pageImage = null;
                    else if (pageImage == null) {
                        FileHandle file = imagesDir.child(line);

                        float width = 0, height = 0;
                        if (readTuple(reader) == 2) { // size is only optional for an atlas packed with an old TexturePacker.
                            width = Integer.parseInt(tuple[0]);
                            height = Integer.parseInt(tuple[1]);
                            readTuple(reader);
                        }

                        // Read and skip these...
                        String format = String.valueOf(tuple[0]);
                        readTuple(reader);
                        String min = String.valueOf(tuple[0]);
                        String max = String.valueOf(tuple[1]);
                        String direction = readValue(reader);

                        pageImage = new Page(file, width, height);
                        pages.add(pageImage);
                    } else {
                        boolean rotate = Boolean.parseBoolean(readValue(reader));

                        readTuple(reader);
                        int left = Integer.parseInt(tuple[0]);
                        int top = Integer.parseInt(tuple[1]);

                        readTuple(reader);
                        int width = Integer.parseInt(tuple[0]);
                        int height = Integer.parseInt(tuple[1]);

                        Region region = new Region();
                        region.page = pageImage;
                        region.left = left;
                        region.top = top;
                        region.width = width;
                        region.height = height;
                        region.name = line;
                        region.rotate = rotate;

                        if (readTuple(reader) == 4) { // split is optional
                            region.splits = new int[]{Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
                                    Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

                            if (readTuple(reader) == 4) { // pad is optional, but only present with splits
                                region.pads = new int[]{Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
                                        Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

                                readTuple(reader);
                            }
                        }

                        region.originalWidth = Integer.parseInt(tuple[0]);
                        region.originalHeight = Integer.parseInt(tuple[1]);

                        readTuple(reader);
                        region.offsetX = Integer.parseInt(tuple[0]);
                        region.offsetY = Integer.parseInt(tuple[1]);

                        region.index = Integer.parseInt(readValue(reader));

                        if (flip) region.flip = true;

                        regions.add(region);
                    }
                }
            } catch (Exception ex) {
                throw new GdxRuntimeException("Error reading pack file: " + packFile, ex);
            } finally {
                StreamUtils.closeQuietly(reader);
            }

            regions.sort(indexComparator);
        }
    }


    /**
     * @param data May be null.
     */
    public TextureAtlas(TextureAtlasData data) {
        if (data != null) load(data);
    }

    private void load(TextureAtlasData data) {
        for (TextureAtlasData.Region region : data.regions) {
            int width = region.width;
            int height = region.height;
            AtlasRegion atlasRegion = new AtlasRegion(width, height);
            atlasRegion.index = region.index;
            atlasRegion.name = region.name;
            atlasRegion.offsetX = region.offsetX;
            atlasRegion.offsetY = region.offsetY;
            atlasRegion.originalHeight = region.originalHeight;
            atlasRegion.originalWidth = region.originalWidth;
            atlasRegion.rotate = region.rotate;
            atlasRegion.splits = region.splits;
            atlasRegion.pads = region.pads;
            regions.add(atlasRegion);
        }
    }

    /**
     * Returns the first region found with the specified name. This method uses string comparison to find the region, so the result
     * should be cached rather than calling this method multiple times.
     *
     * @return The region, or null.
     */
    public AtlasRegion findRegion(String name) {
        for (AtlasRegion region : regions) if (region.name.equals(name)) return region;
        return null;
    }

    static final Comparator<TextureAtlasData.Region> indexComparator = (region1, region2) -> {
        int i1 = region1.index;
        if (i1 == -1) i1 = Integer.MAX_VALUE;
        int i2 = region2.index;
        if (i2 == -1) i2 = Integer.MAX_VALUE;
        return i1 - i2;
    };

    static String readValue(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
        return line.substring(colon + 1).trim();
    }

    /**
     * Returns the number of tuple values read (1, 2 or 4).
     */
    static int readTuple(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if (colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
        int i, lastMatch = colon + 1;
        for (i = 0; i < 3; i++) {
            int comma = line.indexOf(',', lastMatch);
            if (comma == -1) break;
            tuple[i] = line.substring(lastMatch, comma).trim();
            lastMatch = comma + 1;
        }
        tuple[i] = line.substring(lastMatch).trim();
        return i + 1;
    }

    /**
     * Describes the region of a packed image and provides information about the original image before it was packed.
     */
    static public class AtlasRegion {

        public int index;

        /**
         * The name of the original image file, up to the first underscore. Underscores denote special instructions to the texture
         * packer.
         */
        public String name;

        /**
         * The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing.
         */
        public float offsetX;

        /**
         * The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
         * packing.
         */
        public float offsetY;

        /**
         * The width of the image, after whitespace was removed for packing.
         */
        public int packedWidth;

        /**
         * The height of the image, after whitespace was removed for packing.
         */
        public int packedHeight;

        /**
         * The width of the image, before whitespace was removed and rotation was applied for packing.
         */
        public int originalWidth;

        /**
         * The height of the image, before whitespace was removed for packing.
         */
        public int originalHeight;

        /**
         * If true, the region has been rotated 90 degrees counter clockwise.
         */
        public boolean rotate;

        /**
         * The ninepatch splits, or null if not a ninepatch. Has 4 elements: left, right, top, bottom.
         */
        public int[] splits;

        /**
         * The ninepatch pads, or null if not a ninepatch or the has no padding. Has 4 elements: left, right, top, bottom.
         */
        public int[] pads;

        public AtlasRegion(int width, int height) {
            originalWidth = width;
            originalHeight = height;
            packedWidth = width;
            packedHeight = height;
        }

        public String toString() {
            return name;
        }
    }
}
