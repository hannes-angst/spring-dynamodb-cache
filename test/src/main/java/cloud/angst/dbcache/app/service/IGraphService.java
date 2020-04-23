package cloud.angst.dbcache.app.service;

import cloud.angst.dbcache.app.model.Graph;
import org.jetbrains.annotations.NotNull;

public interface IGraphService {
    @NotNull
    Graph getGraph(@NotNull String id);
}
