package cloud.angst.dbcache.app.service;

import cloud.angst.dbcache.app.model.Node;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
public class ListService implements IListService {
    private static final Random RND = new Random();

    @NotNull
    @SneakyThrows
    @Cacheable("list")
    public List<Object> getList(@NotNull String id) {
        SECONDS.sleep(10);
        var result = new ArrayList<>();
        if (RND.nextBoolean()) {
            result.add(1234);
        }
        if (RND.nextBoolean()) {
            result.add(1234567L);
        }
        if (RND.nextBoolean()) {
            result.add(123.5D);
        }
        if (RND.nextBoolean()) {
            result.add(12.1F);
        }
        if (RND.nextBoolean()) {
            result.add((byte) 123);
        }
        if (RND.nextBoolean()) {
            result.add("Hello");
        }
        if (RND.nextBoolean()) {
            result.add(Set.of(1L, 2, "3", 4.1D, 5.2F));
        }
        if (RND.nextBoolean()) {
            result.add(List.of(1L, 2, "3", 4.1D, 5.2F));
        }

        result.add(Node.builder()
                .id(UUID.randomUUID().toString())
                .data(Map.of("key", "value"))
                .build());

        return result;
    }
}
