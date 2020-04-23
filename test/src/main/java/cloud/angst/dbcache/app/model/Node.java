package cloud.angst.dbcache.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Node {
    private String id;
    @JsonInclude(NON_EMPTY)
    private List<Node> children;
    @JsonInclude(NON_EMPTY)
    private Map<String, Object> data;
}
