# Base GLD

Un contenedor PostGIS y una base por servicio. `sql/00-create-databases.sh` crea las bases (CREATE DATABASE no puede ir en un `.sql` del entrypoint) y aplica las tablas. Los `.sql` viven en subcarpetas para que el entrypoint no los ejecute contra una sola base.

Bases: `gld_identity`, `gld_catastro`, `gld_pagos`, `gld_tramites`, `gld_documentos`, `gld_ciudad`, `gld_incidencias`. PostGIS solo en `gld_incidencias`. El municipio `05021` es la columna `tenant_id`. La bitácora está en la base del servicio que escribe.

```powershell
docker compose up -d
docker compose exec postgres psql -U gld -d postgres -c "\l"
```

Para reconstruir las bases desde los scripts:

```powershell
docker compose down -v
docker compose up -d
```

Conexión: `jdbc:postgresql://localhost:5432/<base>`, usuario `gld`, contraseña `gld`.
