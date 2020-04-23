package cloud.angst.dbcache.app.controller;

import cloud.angst.dbcache.app.service.ISimpleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SimpleController {

    private final ISimpleService service;


    @GetMapping(value = "/simple/{id}")
    public ResponseEntity<String> getGraph(@PathVariable() String id) {
        return ResponseEntity.ok(service.getSimple(id));
    }
}
