@ApplicationModule(allowedDependencies = {
    "auth :: api",
    "common :: exceptions",
    "common :: annotations",
})
package com.kamilpm.zero_waste.ratelimit;

import org.springframework.modulith.ApplicationModule;
