package com.promotrack.api.controller;

import com.promotrack.api.domain.model.Supermarket;
import com.promotrack.api.dto.request.SupermarketCreateRequest;
import com.promotrack.api.dto.request.SupermarketUpdateRequest;
import com.promotrack.api.dto.response.ErrorResponse;
import com.promotrack.api.dto.response.SupermarketResponse;
import com.promotrack.api.mapper.SupermarketMapper;
import com.promotrack.api.service.SupermarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/supermarkets")
@Tag(name = "Supermarkets", description = "Operations for managing supermarket catalog records")
public class SupermarketController {

    private final SupermarketService supermarketService;
    private final SupermarketMapper supermarketMapper;

    public SupermarketController(SupermarketService supermarketService, SupermarketMapper supermarketMapper) {
        this.supermarketService = supermarketService;
        this.supermarketMapper = supermarketMapper;
    }

    @GetMapping
    @Operation(summary = "List active supermarkets", description = "Returns every supermarket that has not been logically deleted.")
    @ApiResponse(responseCode = "200", description = "Active supermarkets returned")
    public ResponseEntity<List<SupermarketResponse>> findAll() {
        List<SupermarketResponse> supermarkets = supermarketService.findAllActive()
                .stream()
                .map(supermarketMapper::toResponse)
                .toList();
        return ResponseEntity.ok(supermarkets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supermarket by id", description = "Returns a single supermarket by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supermarket found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Supermarket not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SupermarketResponse> findById(@PathVariable Long id) {
        Supermarket supermarket = supermarketService.findById(id);
        return ResponseEntity.ok(supermarketMapper.toResponse(supermarket));
    }

    @PostMapping
    @Operation(summary = "Create supermarket", description = "Creates a supermarket and marks it as active.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supermarket created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid supermarket request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SupermarketResponse> create(
            @Valid @RequestBody SupermarketCreateRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        Supermarket created = supermarketService.create(supermarketMapper.toModel(request));
        URI location = uriBuilder.path("/api/supermarkets/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(supermarketMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supermarket", description = "Updates editable supermarket fields. Omitted fields remain unchanged.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supermarket updated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid supermarket request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Supermarket not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SupermarketResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SupermarketUpdateRequest request
    ) {
        Supermarket updated = supermarketService.update(id, supermarketMapper.toModel(request));
        return ResponseEntity.ok(supermarketMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supermarket", description = "Performs a logical delete by setting active=false.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supermarket logically deleted"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Supermarket not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supermarketService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
