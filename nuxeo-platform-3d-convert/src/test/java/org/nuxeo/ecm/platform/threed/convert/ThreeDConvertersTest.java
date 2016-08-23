/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Tiago Cardoso <tcardoso@nuxeo.com>
 */
package org.nuxeo.ecm.platform.threed.convert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandAvailability;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.platform.threed.convert.Constants.*;

/**
 * Test 3D converters
 *
 * @since 8.4
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@LocalDeploy({ "org.nuxeo.ecm.platform.threed.convert", "org.nuxeo.ecm.platform.threed.api",
    "org.nuxeo.ecm.platform.commandline.executor" })
public class ThreeDConvertersTest {

    protected static final String TEST_MODEL = "suzane";

    private static final String renderId1 = "renderId1";

    private static final String renderId2 = "renderId2";

    @Inject
    protected ConversionService cs;

    @Inject
    protected CommandLineExecutorService commandLES;

    protected static BlobHolder getTestThreeDBlobs() throws IOException {
        List<Blob> blobs = new ArrayList<>();
        Blob blob;
        try (InputStream is = ThreeDConvertersTest.class.getResourceAsStream("/test-data/" + TEST_MODEL + ".obj")) {
            assertNotNull(String.format("Failed to load resource: " + TEST_MODEL + ".obj"), is);
            blob = Blobs.createBlob(is);
            blob.setFilename(TEST_MODEL + ".obj");
            blobs.add(blob);
        }
        try (InputStream is = ThreeDConvertersTest.class.getResourceAsStream("/test-data/" + TEST_MODEL + ".mtl")) {
            assertNotNull(String.format("Failed to load resource: " + TEST_MODEL + ".mtl"), is);
            blob = Blobs.createBlob(is);
            blob.setFilename(TEST_MODEL + ".mtl");
            blobs.add(blob);
        }
        return new SimpleBlobHolder(blobs);
    }

    protected static BlobHolder getTestColladaBlob() throws IOException {
        List<Blob> blobs = new ArrayList<>();
        try (InputStream is = ThreeDConvertersTest.class.getResourceAsStream("/test-data/" + TEST_MODEL + ".dae")) {
            assertNotNull(String.format("Failed to load resource: " + TEST_MODEL + ".dae"), is);
            Blob blob = Blobs.createBlob(is);
            blob.setFilename(TEST_MODEL + ".dae");
            blobs.add(blob);
        }
        return new SimpleBlobHolder(blobs);
    }

    protected BlobHolder applyConverter(String converter, BlobHolder blobs) throws Exception {
        assertNotNull(cs.getRegistredConverters().contains(converter));
        Map<String, Serializable> params = new HashMap<>();
        params.put(RENDER_IDS_PARAMETER, renderId1 + " " + renderId2);
        BlobHolder result = cs.convert(converter, blobs, params);
        assertNotNull(result);
        return result;
    }

    @Test
    public void testBlenderPipelineCommand() throws Exception {
        CommandAvailability ca = commandLES.getCommandAvailability(BLENDER_PIPELINE_COMMAND);
        assertTrue("blender_pipeline is not available, skipping test", ca.isAvailable());
    }

    @Test
    public void testDae2GltfCommand() throws Exception {
        CommandAvailability ca = commandLES.getCommandAvailability(COLLADA2GLTF_COMMAND);
        assertTrue("dae2gltf is not available, skipping test", ca.isAvailable());
    }

    @Test
    public void testRenderConverter() throws Exception {
        BlobHolder result = applyConverter(RENDER_3D_CONVERTER, getTestThreeDBlobs());
        List<Blob> blobs = result.getBlobs();
        assertEquals(1, blobs.size());
        assertEquals("render-" + renderId1 + "-100-0-0-100-62.png", blobs.get(0).getFilename());
    }

    @Test
    public void testCollada2glTFConverter() throws Exception {
        BlobHolder result = applyConverter(COLLADA2GLTF_CONVERTER, getTestColladaBlob());
        List<Blob> blobs = result.getBlobs();
        assertEquals(1, blobs.size());
        assertEquals(TEST_MODEL + ".gltf", blobs.get(0).getFilename());
    }

    @Test
    public void testColladaConverter() throws Exception {
        BlobHolder result = applyConverter(COLLADA_CONVERTER, getTestThreeDBlobs());
        List<Blob> blobs = result.getBlobs();
        assertEquals(1, blobs.size());
        assertEquals("conversion-100.dae", blobs.get(0).getFilename());
    }

    @Test
    public void testLODConverter() throws Exception {
        BlobHolder result = applyConverter(LOD_CONVERTER, getTestThreeDBlobs());
        List<Blob> blobs = result.getBlobs();
        assertEquals(3, blobs.size());
        List<String> fileNames = blobs.stream().map(Blob::getFilename).collect(Collectors.toList());
        assertEquals("conversion-11.dae", fileNames.get(0));
        assertEquals("conversion-3.dae", fileNames.get(1));
        assertEquals("conversion-33.dae", fileNames.get(2));
    }

    @Test
    public void testBatchConverter() throws Exception {
        BlobHolder result = applyConverter(BATCH_CONVERTER, getTestThreeDBlobs());
        List<Blob> blobs = result.getBlobs();
        assertEquals(6, blobs.size());
        List<String> fileNames = blobs.stream().map(Blob::getFilename).collect(Collectors.toList());
        assertEquals("conversion-100.dae", fileNames.get(0));
        assertEquals("conversion-11.dae", fileNames.get(1));
        assertEquals("conversion-3.dae", fileNames.get(2));
        assertEquals("conversion-33.dae", fileNames.get(3));
        assertEquals("render-" + renderId1 + "-100-0-0-100-62.png", fileNames.get(4));
        assertEquals("render-" + renderId2 + "-100-90-0-100-62.png", fileNames.get(5));
    }
}
