package com.promotrack.api.controller;

import com.promotrack.api.domain.model.Offer;
import com.promotrack.api.dto.request.OfferCreateRequest;
import com.promotrack.api.dto.request.OfferUpdateRequest;
import com.promotrack.api.dto.response.ErrorResponse;
import com.promotrack.api.dto.response.OfferResponse;
import com.promotrack.api.mapper.OfferMapper;
import com.promotrack.api.service.OfferService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/offers")
@Tag(name = "Offers", description = "Operations for managing supermarket offers and offer calendar views")
@Validated
public class OfferController {

    private final OfferService offerService;
    private final OfferMapper offerMapper;

    public OfferController(OfferService offerService, OfferMapper offerMapper) {
        this.offerService = offerService;
        this.offerMapper = offerMapper;
    }

    @GetMapping
    @Operation(summary = "List active offers", description = "Returns every offer that has not been logically deleted.")
    @ApiResponse(responseCode = "200", description = "Active offers returned")
    public ResponseEntity<List<OfferResponse>> findAll() {
        return ResponseEntity.ok(toResponseList(offerService.findAllActive()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get offer by id", description = "Returns a single offer by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Offer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<OfferResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(offerMapper.toResponse(offerService.findById(id)));
    }

    @PostMapping
    @Operation(summary = "Create offer", description = "Creates an offer and associates it to an existing supermarket.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Offer created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid offer request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Supermarket not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<OfferResponse> create(
            @Valid @RequestBody OfferCreateRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        Offer created = offerService.create(offerMapper.toModel(request), request.supermarketId());
        URI location = uriBuilder.path("/api/offers/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(offerMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update offer", description = "Updates editable offer fields. Omitted fields remain unchanged.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer updated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid offer request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Offer or supermarket not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<OfferResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OfferUpdateRequest request
    ) {
        Offer updated = offerService.update(id, offerMapper.toModel(request));
        return ResponseEntity.ok(offerMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete offer", description = "Performs a logical delete by setting active=false.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Offer logically deleted"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Offer not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/today")
    @Operation(summary = "List today's active offers", description = "Returns offers active for the current date.")
    @ApiResponse(responseCode = "200", description = "Today's offers returned")
    public ResponseEntity<List<OfferResponse>> findTodayOffers() {
        return ResponseEntity.ok(toResponseList(offerService.findTodayActiveOffers()));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "List upcoming offers", description = "Returns active offers whose start date is after today.")
    @ApiResponse(responseCode = "200", description = "Upcoming offers returned")
    public ResponseEntity<List<OfferResponse>> findUpcomingOffers() {
        return ResponseEntity.ok(toResponseList(offerService.findUpcomingOffers()));
    }

    @GetMapping("/expiring-soon")
    @Operation(
            summary = "List offers expiring soon",
            description = "Returns active offers ending between today and the next requested number of days. Defaults to 3 days."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expiring offers returned"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid days parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<OfferResponse>> findExpiringSoonOffers(
            @Parameter(description = "Number of days from today to include. Allowed range: 1 to 30.")
            @RequestParam(defaultValue = "3")
            @Min(value = 1, message = "days must be at least 1")
            @Max(value = 30, message = "days must be at most 30")
            int days
    ) {
        return ResponseEntity.ok(toResponseList(offerService.findExpiringSoonOffers(days)));
    }

    @GetMapping("/calendar")
    @Operation(summary = "List offers by date range", description = "Returns active offers overlapping the requested date range.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offers returned for date range"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or missing date range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<OfferResponse>> findCalendarOffers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(toResponseList(offerService.findCalendarOffers(from, to)));
    }

    @GetMapping("/supermarket/{supermarketId}")
    @Operation(summary = "List offers by supermarket", description = "Returns active offers associated with an existing supermarket.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offers returned for supermarket"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Supermarket not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<OfferResponse>> findOffersBySupermarket(@PathVariable Long supermarketId) {
        return ResponseEntity.ok(toResponseList(offerService.findOffersBySupermarket(supermarketId)));
    }

    private List<OfferResponse> toResponseList(List<Offer> offers) {
        return offers.stream()
                .map(offerMapper::toResponse)
                .toList();
    }
}
