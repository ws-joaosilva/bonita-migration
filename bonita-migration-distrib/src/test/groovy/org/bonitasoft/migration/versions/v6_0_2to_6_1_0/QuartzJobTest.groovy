package org.bonitasoft.migration.versions.v6_0_2to_6_1_0;

import static org.junit.Assert.*

import org.bonitasoft.engine.scheduler.JobIdentifier
import org.bonitasoft.migration.core.IOUtil
import org.junit.Test
import org.quartz.JobDataMap

public class QuartzJobTest {

    private final byte[] jobDataMapBytes = [
        -84,
        -19,
        0,
        5,
        115,
        114,
        0,
        21,
        111,
        114,
        103,
        46,
        113,
        117,
        97,
        114,
        116,
        122,
        46,
        74,
        111,
        98,
        68,
        97,
        116,
        97,
        77,
        97,
        112,
        -97,
        -80,
        -125,
        -24,
        -65,
        -87,
        -80,
        -53,
        2,
        0,
        0,
        120,
        114,
        0,
        38,
        111,
        114,
        103,
        46,
        113,
        117,
        97,
        114,
        116,
        122,
        46,
        117,
        116,
        105,
        108,
        115,
        46,
        83,
        116,
        114,
        105,
        110,
        103,
        75,
        101,
        121,
        68,
        105,
        114,
        116,
        121,
        70,
        108,
        97,
        103,
        77,
        97,
        112,
        -126,
        8,
        -24,
        -61,
        -5,
        -59,
        93,
        40,
        2,
        0,
        1,
        90,
        0,
        19,
        97,
        108,
        108,
        111,
        119,
        115,
        84,
        114,
        97,
        110,
        115,
        105,
        101,
        110,
        116,
        68,
        97,
        116,
        97,
        120,
        114,
        0,
        29,
        111,
        114,
        103,
        46,
        113,
        117,
        97,
        114,
        116,
        122,
        46,
        117,
        116,
        105,
        108,
        115,
        46,
        68,
        105,
        114,
        116,
        121,
        70,
        108,
        97,
        103,
        77,
        97,
        112,
        19,
        -26,
        46,
        -83,
        40,
        118,
        10,
        -50,
        2,
        0,
        2,
        90,
        0,
        5,
        100,
        105,
        114,
        116,
        121,
        76,
        0,
        3,
        109,
        97,
        112,
        116,
        0,
        15,
        76,
        106,
        97,
        118,
        97,
        47,
        117,
        116,
        105,
        108,
        47,
        77,
        97,
        112,
        59,
        120,
        112,
        1,
        115,
        114,
        0,
        17,
        106,
        97,
        118,
        97,
        46,
        117,
        116,
        105,
        108,
        46,
        72,
        97,
        115,
        104,
        77,
        97,
        112,
        5,
        7,
        -38,
        -63,
        -61,
        22,
        96,
        -47,
        3,
        0,
        2,
        70,
        0,
        10,
        108,
        111,
        97,
        100,
        70,
        97,
        99,
        116,
        111,
        114,
        73,
        0,
        9,
        116,
        104,
        114,
        101,
        115,
        104,
        111,
        108,
        100,
        120,
        112,
        63,
        64,
        0,
        0,
        0,
        0,
        0,
        12,
        119,
        8,
        0,
        0,
        0,
        16,
        0,
        0,
        0,
        1,
        116,
        0,
        13,
        106,
        111,
        98,
        73,
        100,
        101,
        110,
        116,
        105,
        102,
        105,
        101,
        114,
        115,
        114,
        0,
        45,
        111,
        114,
        103,
        46,
        98,
        111,
        110,
        105,
        116,
        97,
        115,
        111,
        102,
        116,
        46,
        101,
        110,
        103,
        105,
        110,
        101,
        46,
        115,
        99,
        104,
        101,
        100,
        117,
        108,
        101,
        114,
        46,
        74,
        111,
        98,
        73,
        100,
        101,
        110,
        116,
        105,
        102,
        105,
        101,
        114,
        82,
        -107,
        79,
        118,
        58,
        -10,
        72,
        -47,
        2,
        0,
        3,
        74,
        0,
        2,
        105,
        100,
        74,
        0,
        8,
        116,
        101,
        110,
        97,
        110,
        116,
        73,
        100,
        76,
        0,
        7,
        106,
        111,
        98,
        78,
        97,
        109,
        101,
        116,
        0,
        18,
        76,
        106,
        97,
        118,
        97,
        47,
        108,
        97,
        110,
        103,
        47,
        83,
        116,
        114,
        105,
        110,
        103,
        59,
        120,
        112,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        1,
        116,
        0,
        16,
        66,
        80,
        77,
        69,
        118,
        101,
        110,
        116,
        72,
        97,
        110,
        100,
        108,
        105,
        110,
        103,
        120,
        0
    ];

    @Test
    public void deserialize() throws IOException, ClassNotFoundException {
        JobDataMap map = IOUtil.deserialize(jobDataMapBytes, JobIdentifier.class.getClassLoader());
        println map
        JobIdentifier jobIdent = map.get("jobIdentifier");
        assertEquals(1,jobIdent.getId());
        assertEquals(1,jobIdent.getTenantId());
        assertEquals("BPMEventHandling",jobIdent.getJobName());

        def newMap = new JobDataMap();
        newMap.put("jobId", 1);
        newMap.put("tenantId", 1);
        newMap.put("jobName", "BPMEventHandling");
        byte[] out = IOUtil.serialize(newMap);

        map = IOUtil.deserialize(out,JobIdentifier.class.getClassLoader());
        assertEquals(1,map.get("jobId"));
        assertEquals(1,map.get("tenantId"));
        assertEquals("BPMEventHandling",map.get("jobName"));
    }
}
