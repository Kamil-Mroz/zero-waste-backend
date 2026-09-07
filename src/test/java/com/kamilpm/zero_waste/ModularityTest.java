package com.kamilpm.zero_waste;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTest {
  @Test
  void verifiesModularStructure() {
    ApplicationModules.of(ZeroWasteApplication.class).verify();
  }

}
