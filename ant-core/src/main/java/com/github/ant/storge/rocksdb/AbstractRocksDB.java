package com.github.ant.storge.rocksdb;

import com.github.ant.storge.StateBackend;
import com.github.ant.utils.FileUtils;
import com.github.ant.utils.IOUtils;
import com.github.ant.utils.Preconditions;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRocksDB implements StateBackend {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRocksDB.class);
    static final String DB_INSTANCE_DIR_STRING = "db";
    private final File instanceBasePath;
    private final File instanceRocksDBPath;
    private RocksDB db;
    protected ColumnFamilyHandle defaultColumnFamilyHandle;
    protected List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>(1);;

    public AbstractRocksDB(File instanceBasePath) {
        this.instanceBasePath = instanceBasePath;
        this.instanceRocksDBPath = new File(instanceBasePath, DB_INSTANCE_DIR_STRING);
    }

    public DBOptions getDbOptions() {
        DBOptions opt = PredefinedOptions.DEFAULT.createDBOptions(new ArrayList<>());
        opt = opt.setCreateIfMissing(true);
        return opt;
    }

    public void start() {
        try {
            prepare();
            List<ColumnFamilyDescriptor> columnFamilyDescriptors =
                    new ArrayList<>(1);
            try {
                db = RocksDB.open(
                        Preconditions.checkNotNull(getDbOptions()),
                        Preconditions.checkNotNull(instanceRocksDBPath.getAbsolutePath()),
                        columnFamilyDescriptors,
                        columnFamilyHandles);
                defaultColumnFamilyHandle = columnFamilyHandles.remove(0);

            } catch (RocksDBException e) {
                e.printStackTrace();
                logger.error("RocksDB open failed!", e);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("No directory or created failed!", e);
        }
    }


    public void stop() {
        IOUtils.closeQuietly(defaultColumnFamilyHandle);
        IOUtils.closeQuietly(db);
    }

    private void prepare() throws IOException {
        checkAndCreateDirectory(instanceBasePath);
        if (instanceRocksDBPath.exists()) {
            // Clear the base directory when the backend is created
            // in case something crashed and the backend never reached dispose()
            FileUtils.deleteDirectory(instanceBasePath);
        }
    }

    private static void checkAndCreateDirectory(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                throw new IOException("Not a directory: " + directory);
            }
        } else if (!directory.mkdirs()) {
            throw new IOException(String.format("Could not create RocksDB data directory at %s.", directory));
        }
    }
}
