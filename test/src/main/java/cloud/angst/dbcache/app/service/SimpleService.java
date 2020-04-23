package cloud.angst.dbcache.app.service;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
public class SimpleService implements ISimpleService {
    @NotNull
    @SneakyThrows
    @Cacheable("simple")
    public String getSimple(@NotNull String id) {
        SECONDS.sleep(10);
        return UUID.randomUUID().toString();
    }
}
