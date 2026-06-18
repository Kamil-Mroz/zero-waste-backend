package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.OfferDto;
import com.kamilpm.zero_waste.domain.entity.OfferStatus;
import com.kamilpm.zero_waste.domain.response.PageResponse;
import com.kamilpm.zero_waste.service.OfferService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(path = "/api/v{version}/offers", version = "1")
@RequiredArgsConstructor
public class OfferController {

  private final OfferService offerService;

  @PostMapping("/{id}")
  public ResponseEntity<Void> makeOffer(@PathVariable UUID id) {
    offerService.makeOffer(id);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/accept")
  public ResponseEntity<Void> acceptOffer(@PathVariable UUID id) {
    offerService.acceptOffer(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<Void> rejectOffer(@PathVariable UUID id) {
    offerService.rejectOffer(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<Void> cancelOffer(@PathVariable UUID id) {
    offerService.cancelOffer(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/own")
  public ResponseEntity<PageResponse<OfferDto>> getOwnOffers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) OfferStatus status) {

    Page<OfferDto> offersDto = offerService.getMyOffers(PageRequest.of(page, size, Sort.Direction.DESC, "createdAt"),
        status);

    return ResponseEntity.ok(new PageResponse<>(offersDto.getContent(), offersDto.getNumber(), offersDto.getSize(),
        offersDto.getTotalElements(), offersDto.getTotalPages()));
  }

  @GetMapping("/received")
  public ResponseEntity<PageResponse<OfferDto>> getReceivedOffers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) OfferStatus status) {

    Page<OfferDto> offersDto = offerService.getReceivedOffers(
        PageRequest.of(page, size, Sort.Direction.DESC, "createdAt"),
        status);

    return ResponseEntity.ok(new PageResponse<>(offersDto.getContent(), offersDto.getNumber(), offersDto.getSize(),
        offersDto.getTotalElements(), offersDto.getTotalPages()));
  }
}
