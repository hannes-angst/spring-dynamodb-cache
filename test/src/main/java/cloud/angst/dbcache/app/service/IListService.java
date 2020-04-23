package cloud.angst.dbcache.app.service;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IListService {
    @NotNull
    List<Object> getList(@NotNull String id);
}
