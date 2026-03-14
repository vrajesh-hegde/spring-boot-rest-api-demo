package com.example.springbootrestapi.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * Allows admins to import application configuration (feature flags, limits, etc.)
 * via YAML. The uploaded YAML is parsed and merged into runtime settings.
 */
@RestController
@RequestMapping("/api/admin/config")
public class YamlConfigController {

    /**
     * Import configuration from YAML body. Accepts arbitrary YAML and deserializes
     * it into Java objects for use as app settings.
     */
    @PostMapping(value = "/import", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> importConfig(@RequestBody String yamlBody) {
        if (yamlBody == null || yamlBody.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "YAML body required"));
        }
        try {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(yamlBody);
            return ResponseEntity.ok(Map.of(
                    "parsed", loaded != null,
                    "result", loaded != null ? loaded : "null"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
