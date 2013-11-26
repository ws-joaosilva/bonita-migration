package org.quartz;

import org.quartz.utils.StringKeyDirtyFlagMap

public class JobDataMap extends StringKeyDirtyFlagMap implements Serializable {

    private static final long serialVersionUID = -6939901990106713909L;

    public JobDataMap() {
        super();
    }

    public JobDataMap(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public JobDataMap(final int initialCapacity) {
        super(initialCapacity);
    }
}
