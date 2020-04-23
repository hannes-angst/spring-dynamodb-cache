package cloud.angst.dbcache.app.service;

import org.jetbrains.annotations.NotNull;

public interface ISimpleService {
    @NotNull
    String getSimple(@NotNull String id);
}
