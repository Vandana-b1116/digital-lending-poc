# ── Build stage ─────────────────────────────────────────────
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build:prod

# ── Runtime stage (nginx) ────────────────────────────────────
FROM nginx:1.25-alpine
COPY --from=build /app/dist/digital-lending-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
