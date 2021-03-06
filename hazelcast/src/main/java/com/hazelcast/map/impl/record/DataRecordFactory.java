/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.map.impl.record;

import com.hazelcast.config.CacheDeserializedValues;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.internal.serialization.SerializationService;
import com.hazelcast.map.impl.MapContainer;

import static com.hazelcast.map.impl.eviction.Evictor.NULL_EVICTOR;

public class DataRecordFactory implements RecordFactory<Data> {

    private final MapContainer mapContainer;
    private final SerializationService ss;

    public DataRecordFactory(MapContainer mapContainer, SerializationService ss) {
        this.ss = ss;
        this.mapContainer = mapContainer;
    }

    @Override
    public Record<Data> newRecord(Object value) {
        MapConfig mapConfig = mapContainer.getMapConfig();
        boolean statisticsEnabled = mapConfig.isStatisticsEnabled();
        CacheDeserializedValues cacheDeserializedValues = mapConfig.getCacheDeserializedValues();
        boolean hasEviction = mapContainer.getEvictor() != NULL_EVICTOR;

        Data valueData = ss.toData(value);

        switch (cacheDeserializedValues) {
            case NEVER:
                return newSimpleRecord(valueData, mapConfig, statisticsEnabled, hasEviction);
            default:
                return newCachedSimpleRecord(valueData, mapConfig, statisticsEnabled, hasEviction);
        }
    }

    @Override
    public MapContainer geMapContainer() {
        return mapContainer;
    }

    private Record<Data> newCachedSimpleRecord(Data valueData, MapConfig mapConfig,
                                               boolean statisticsEnabled, boolean hasEviction) {
        if (statisticsEnabled || isClusterV41()) {
            return new CachedDataRecordWithStats(valueData);
        }

        if (hasEviction) {
            if (mapConfig.getEvictionConfig().getEvictionPolicy() == EvictionPolicy.LRU) {
                return new CachedSimpleRecordWithLRUEviction(valueData);
            }

            if (mapConfig.getEvictionConfig().getEvictionPolicy() == EvictionPolicy.LFU) {
                return new CachedSimpleRecordWithLFUEviction(valueData);
            }
        }

        return new CachedSimpleRecord(valueData);
    }

    private Record<Data> newSimpleRecord(Data valueData, MapConfig mapConfig,
                                         boolean statisticsEnabled, boolean hasEviction) {
        if (statisticsEnabled || isClusterV41()) {
            return new DataRecordWithStats(valueData);
        }

        if (hasEviction) {
            if (mapConfig.getEvictionConfig().getEvictionPolicy() == EvictionPolicy.LRU) {
                return new SimpleRecordWithLRUEviction<>(valueData);
            }

            if (mapConfig.getEvictionConfig().getEvictionPolicy() == EvictionPolicy.LFU) {
                return new SimpleRecordWithLFUEviction<>(valueData);
            }
        }

        return new SimpleRecord<>(valueData);
    }
}
