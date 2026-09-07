@ApplicationModule(allowedDependencies = {
    "user :: api",
    "item :: api",
    "auth :: api",
    "offer :: api",
    "moderation :: api",
    "common :: exceptions",
    "common :: annotations",
    "common :: dtos",
    "common :: entity",

})
package com.kamilpm.zero_waste.review;

import org.springframework.modulith.ApplicationModule;
