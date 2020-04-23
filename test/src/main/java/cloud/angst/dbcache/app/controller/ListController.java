package cloud.angst.dbcache.app.controller;

import cloud.angst.dbcache.app.service.IListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ListController {

    private final IListService service;


    @GetMapping(value = "/list/{id}")
    public ResponseEntity<List<Object>> getGraph(@PathVariable() String id) {
        return ResponseEntity.ok(service.getList(id));
    }
}
