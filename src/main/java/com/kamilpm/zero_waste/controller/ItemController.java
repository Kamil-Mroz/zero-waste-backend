package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.ItemDto;
import com.kamilpm.zero_waste.domain.entity.ItemState;
import com.kamilpm.zero_waste.domain.request.ItemRequest;
import com.kamilpm.zero_waste.domain.request.UpdateItemRequest;
import com.kamilpm.zero_waste.domain.response.PageResponse;
import com.kamilpm.zero_waste.service.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(path = "/api/v{version}/items", version = "1")
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ItemDto> createItem(@Valid @ModelAttribute ItemRequest itemRequest) {

    ItemDto item = itemService.createItem(itemRequest);

    return new ResponseEntity<>(item, HttpStatus.CREATED);
  }

  @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ItemDto> updateItem(@PathVariable("id") UUID id,
      @Valid @ModelAttribute UpdateItemRequest itemRequest) {
    ItemDto item = itemService.updateItem(id, itemRequest);

    return ResponseEntity.ok(item);
  }

  @PatchMapping(path = "/{id}/publish")
  public ResponseEntity<ItemDto> publishItem(@PathVariable("id") UUID id) {
    ItemDto item = itemService.publishItem(id);
    return ResponseEntity.ok(item);

  }

  @PatchMapping(path = "/{id}/hide")
  public ResponseEntity<ItemDto> hideItem(@PathVariable("id") UUID id) {
    ItemDto item = itemService.hideItem(id);
    return ResponseEntity.ok(item);

  }

  @GetMapping
  public ResponseEntity<PageResponse<ItemDto>> getItems(@RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "text", required = false) String text,
      @RequestParam(value = "category", required = false) UUID category) {

    Page<ItemDto> items = itemService.getItems(PageRequest.of(page, size), text, category);

    return ResponseEntity.ok(new PageResponse<>(items.getContent(), items.getNumber(), items.getSize(),
        items.getTotalElements(), items.getTotalPages()));
  }

  @GetMapping("/own")
  public ResponseEntity<PageResponse<ItemDto>> getOwnItems(@RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "text", required = false) String text,
      @RequestParam(value = "category", required = false) UUID category,
      @RequestParam(value = "states", required = false) List<ItemState> states) {
    Page<ItemDto> itemsDto = itemService.getOwnItems(PageRequest.of(page, size), text, category, states);
    return ResponseEntity.ok(new PageResponse<>(itemsDto.getContent(), itemsDto.getNumber(), itemsDto.getSize(),
        itemsDto.getTotalElements(), itemsDto.getTotalPages()));
  }

  @GetMapping("/user/{id}")
  public ResponseEntity<List<ItemDto>> getUsersItem(@PathVariable("id") UUID id) {
    List<ItemDto> itemDtos = itemService.getUserItems(id);
    return ResponseEntity.ok(itemDtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ItemDto> getItem(@PathVariable("id") UUID id) {
    ItemDto item = itemService.getItem(id);
    return ResponseEntity.ok(item);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable("id") UUID id) {
    itemService.deleteItem(id);
    return ResponseEntity.noContent().build();
  }

}
