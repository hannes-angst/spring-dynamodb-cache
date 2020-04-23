package cloud.angst.dbcache.app.controller;

import cloud.angst.dbcache.app.model.Graph;
import cloud.angst.dbcache.app.service.IGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GraphController {

    private final IGraphService service;


    @GetMapping(value = "/graph/{id}")
    public ResponseEntity<Graph> getGraph(@PathVariable() String id) {
        return ResponseEntity.ok(service.getGraph(id));
    }
}
