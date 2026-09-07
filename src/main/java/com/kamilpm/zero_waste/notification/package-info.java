@ApplicationModule(allowedDependencies = {
    "auth :: api",
    "common :: exceptions",
    "common :: annotations",
    "common :: dtos",
    "common :: entity",
})
package com.kamilpm.zero_waste.notification;

import org.springframework.modulith.ApplicationModule;
