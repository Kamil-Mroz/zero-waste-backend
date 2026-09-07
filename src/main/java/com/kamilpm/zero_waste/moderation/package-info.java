@ApplicationModule(allowedDependencies = {
    "user :: api",
    "auth :: api",
    "blog :: api",
    "item :: api",
    "review :: api",
    "notification :: api",
    "common :: exceptions",
    "common :: annotations",
    "common :: dtos",
    "common :: entity",
})
package com.kamilpm.zero_waste.moderation;

import org.springframework.modulith.ApplicationModule;
