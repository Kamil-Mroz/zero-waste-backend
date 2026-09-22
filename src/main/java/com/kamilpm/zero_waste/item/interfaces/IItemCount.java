package com.kamilpm.zero_waste.item.interfaces;

import com.kamilpm.zero_waste.common.dto.ItemState;

public interface IItemCount {
  ItemState getItemState();
  Long getTotalItem();

}
