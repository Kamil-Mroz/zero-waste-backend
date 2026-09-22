package com.kamilpm.zero_waste;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModularityTest {
  @Test
  void writeDocumentation() {
    var modules = ApplicationModules.of(ZeroWasteApplication.class);
    new Documenter(modules).writeModulesAsPlantUml().writeModuleCanvases();
  }

  @Test
  void verifiesModularStructure() {
    ApplicationModules.of(ZeroWasteApplication.class).verify();
  }

}
