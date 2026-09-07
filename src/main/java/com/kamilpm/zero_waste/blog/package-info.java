@ApplicationModule(allowedDependencies = {
    "user :: api",
    "auth :: api",
    "image :: api",
    "common :: exceptions",
    "common :: entity",
    "common :: annotations",
    "moderation :: api",
})
package com.kamilpm.zero_waste.blog;

import org.springframework.modulith.ApplicationModule;
