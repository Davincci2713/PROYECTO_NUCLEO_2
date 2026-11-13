FROM node:22

# No usar /app: Next.js confunde esa ruta con el App Router y no compila app/layout.tsx.
WORKDIR /opt/portal

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Next incrusta NEXT_PUBLIC_* en el bundle del cliente durante el build.
ARG NEXT_PUBLIC_API_BASE=http://localhost:8080
ENV NEXT_PUBLIC_API_BASE=$NEXT_PUBLIC_API_BASE

RUN npm run build

EXPOSE 3000

CMD ["npx", "next", "start", "-H", "0.0.0.0", "-p", "3000"]
