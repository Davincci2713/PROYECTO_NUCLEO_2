#!/bin/bash
# docker-entrypoint-initdb.d ejecuta este script fuera de una transacción.
# CREATE DATABASE no puede ir en un .sql del entrypoint.
set -euo pipefail

echo "Creando las siete bases GLD"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<'SQL'
SELECT format('CREATE DATABASE %I OWNER %I', nombre, current_user)
FROM (
    VALUES
        ('gld_identity'),
        ('gld_catastro'),
        ('gld_pagos'),
        ('gld_tramites'),
        ('gld_documentos'),
        ('gld_ciudad'),
        ('gld_incidencias')
) AS pedidas(nombre)
WHERE NOT EXISTS (
    SELECT 1 FROM pg_database WHERE datname = pedidas.nombre
)
\gexec
SQL

dir="/docker-entrypoint-initdb.d"

aplicar() {
    local base_datos="$1"
    local archivo="$2"
    echo "Aplicando ${archivo} en ${base_datos}"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$base_datos" -f "$archivo"
}

aplicar gld_identity    "$dir/gld_identity/01_tablas.sql"
aplicar gld_identity    "$dir/gld_identity/02_semilla.sql"
aplicar gld_catastro    "$dir/gld_catastro/01_tablas.sql"
aplicar gld_catastro    "$dir/gld_catastro/02_semilla.sql"
aplicar gld_pagos       "$dir/gld_pagos/01_tablas.sql"
aplicar gld_pagos       "$dir/gld_pagos/02_semilla.sql"
aplicar gld_tramites    "$dir/gld_tramites/01_tablas.sql"
aplicar gld_documentos  "$dir/gld_documentos/01_tablas.sql"
aplicar gld_ciudad      "$dir/gld_ciudad/01_tablas.sql"
aplicar gld_ciudad      "$dir/gld_ciudad/02_semilla.sql"
aplicar gld_incidencias "$dir/gld_incidencias/01_tablas.sql"
aplicar gld_incidencias "$dir/gld_incidencias/02_semilla.sql"

for base_datos in gld_pagos gld_tramites gld_ciudad gld_incidencias; do
    aplicar "$base_datos" "$dir/comun/bitacora.sql"
done

echo "Bases GLD listas"
