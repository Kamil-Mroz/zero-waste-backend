package com.kamilpm.zero_waste.common.interfaces;

import java.util.Optional;

import com.kamilpm.zero_waste.common.dto.CurrentUser;

public interface CurrentUserProvider {

  Optional<CurrentUser> getAuthenticatedUser();

  CurrentUser getRequiredAuthenticatedUser();

}
