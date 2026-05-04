package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.ItemDto;
import com.kamilpm.zero_waste.domain.entity.Item;
import com.kamilpm.zero_waste.domain.mapper.ItemMapper;
import com.kamilpm.zero_waste.domain.request.ItemRequest;
import com.kamilpm.zero_waste.domain.request.UpdateItemRequest;
import com.kamilpm.zero_waste.service.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(path = "/api/v{version}/items", version = "1")
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;
  private final ItemMapper itemMapper;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ItemDto> createItem(@Valid @ModelAttribute ItemRequest itemRequest) {

    Item item = itemService.createItem(itemRequest);

    return new ResponseEntity<>(itemMapper.toDto(item), HttpStatus.CREATED);
  }

  @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ItemDto> updateItem(@PathVariable UUID id, @Valid @ModelAttribute UpdateItemRequest itemRequest) {
    Item item = itemService.updateItem(id, itemRequest);

    return ResponseEntity.ok(itemMapper.toDto(item));
  }

  @GetMapping
  public ResponseEntity<List<ItemDto>> getItems() {
    List<Item> items = itemService.getItems();

    List<ItemDto> itemsDto = items.stream().map(itemMapper::toDto).toList();

    return ResponseEntity.ok(itemsDto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ItemDto> getItem(@PathVariable UUID id) {
    Item item = itemService.getItem(id);

    return ResponseEntity.ok(itemMapper.toDtoWithOwner(item));
  }

  @GetMapping("/own")
  public ResponseEntity<List<ItemDto>> getOwnItems() {
    List<Item> items = itemService.getOwnItems();
    List<ItemDto> itemsDto = items.stream().map(itemMapper::toDto).toList();
    return ResponseEntity.ok(itemsDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
    itemService.deleteItem(id);

    return ResponseEntity.noContent().build();
  }
}
