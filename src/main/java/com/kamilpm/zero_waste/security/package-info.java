@ApplicationModule(allowedDependencies = {
    "auth :: api",
    "common :: exceptions",
    "common :: dtos",
})
package com.kamilpm.zero_waste.security;

import org.springframework.modulith.ApplicationModule;
