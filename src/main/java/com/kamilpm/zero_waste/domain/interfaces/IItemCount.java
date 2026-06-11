package com.kamilpm.zero_waste.domain.interfaces;

import com.kamilpm.zero_waste.domain.entity.ItemState;

public interface IItemCount {
  ItemState getItemState();

  Long getTotalItem();

}
