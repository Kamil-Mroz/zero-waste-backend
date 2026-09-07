@ApplicationModule(allowedDependencies = {
    "item :: api",
    "auth :: api",
    "review :: api",
    "notification :: api",
    "common :: exceptions",
    "common :: annotations",
    "common :: dtos",
    "common :: utils",
})
package com.kamilpm.zero_waste.user;

import org.springframework.modulith.ApplicationModule;
