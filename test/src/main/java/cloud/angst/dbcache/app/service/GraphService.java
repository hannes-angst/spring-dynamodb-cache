package cloud.angst.dbcache.app.service;

import cloud.angst.dbcache.app.model.Graph;
import cloud.angst.dbcache.app.model.Node;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
public class GraphService implements IGraphService {
    @NotNull
    @SneakyThrows
    @Cacheable("graph")
    public Graph getGraph(@NotNull String id) {
        var node = new Node();
        node.setId("");

        generateChildren(node, 4);

        return Graph.builder()
                .id("0")
                .name("Fancy Graph")
                .children(node.getChildren())
                .build();
    }


    private static final Random RND = new Random();


    @SneakyThrows
    private void generateChildren(Node node, int depth) {
        if (depth <= 0) {
            return;
        }

        MILLISECONDS.sleep(250);

        int amount = RND.nextInt(5) + 1;

        List<Node> children = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            var child = new Node();
            child.setId(node.getId() + "" + (i + 1));
            child.setData(new HashMap<>());
            if (RND.nextBoolean()) {
                child.getData().put("int", 1234);
            }
            if (RND.nextBoolean()) {
                child.getData().put("long", 1234567L);
            }
            if (RND.nextBoolean()) {
                child.getData().put("double", 123.5D);
            }
            if (RND.nextBoolean()) {
                child.getData().put("float", 12.1F);
            }
            if (RND.nextBoolean()) {
                child.getData().put("byte", (byte) 123);
            }
            if (RND.nextBoolean()) {
                child.getData().put("string", "Hello");
            }
            if (RND.nextBoolean()) {
                child.getData().put("set", Set.of("a", "b", "c"));
            }

            children.add(child);
            generateChildren(child, depth - 1);
        }
        node.setChildren(children);
    }
}
