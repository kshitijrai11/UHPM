#!/bin/bash
# Flyway manual migration helper script

echo "Running manual Flyway migration..."
mvn flyway:migrate \
    -Dflyway.url=jdbc:postgresql://localhost:5432/ultrahpm_product \
    -Dflyway.user=ultrahpm_user \
    -Dflyway.password=ultrahpm_password \
    -Dflyway.locations=filesystem:../../product-service/src/main/resources/db/migration
echo "Done!"
