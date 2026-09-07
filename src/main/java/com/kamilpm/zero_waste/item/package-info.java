@ApplicationModule(allowedDependencies = {
    "offer :: api",
    "category :: api",
    "user :: api",
    "moderation :: api",
    "auth :: api",
    "image :: api",
    "common :: exceptions",
    "common :: annotations",
    "common :: dtos",
    "common :: utils",
    "common :: entity",
})
package com.kamilpm.zero_waste.item;

import org.springframework.modulith.ApplicationModule;
