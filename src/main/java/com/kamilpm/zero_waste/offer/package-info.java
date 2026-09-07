@ApplicationModule(allowedDependencies = {
    "user :: api",
    "auth :: api",
    "item :: api",
    "notification :: api",
    "common :: exceptions",
    "common :: annotations",
    "common :: dtos",
    "common :: entity",
})
package com.kamilpm.zero_waste.offer;

import org.springframework.modulith.ApplicationModule;
