@ApplicationModule(allowedDependencies = {
    "auth :: api",
    "item :: api",
    "common :: exceptions",
    "common :: annotations",
})
package com.kamilpm.zero_waste.category;

import org.springframework.modulith.ApplicationModule;
