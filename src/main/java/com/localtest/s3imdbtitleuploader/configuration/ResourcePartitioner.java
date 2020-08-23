package com.localtest.s3imdbtitleuploader.configuration;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class ResourcePartitioner implements Partitioner {

    private final String PARTITION_KEY = "SLAVE-";

    final private Integer rowCount;

    public ResourcePartitioner(Integer rowCount) {
        this.rowCount = rowCount;
    }


    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        gridSize =  gridSize < 1 ? (int) Math.round(Math.log(rowCount)) : gridSize;

        Map<String, ExecutionContext> map = new HashMap<>(gridSize);

        long currentItemCount = 0L;
        long itemCountPerPartition = (long) Math.ceil((double) rowCount / gridSize);
        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();

            context.putLong("currentItemCount", currentItemCount);
            currentItemCount += itemCountPerPartition;
            context.putLong("maxItemCount", Math.min(currentItemCount, rowCount));

            map.put(PARTITION_KEY + i, context);
        }

        return map;
    }
}
