# syntax=docker/dockerfile:1

FROM node:20-alpine

WORKDIR /app

# Install deps first for caching
COPY package*.json ./
RUN npm ci --only=production

# Copy source
COPY . .

# Document the port (not required, but helpful)
EXPOSE 8088

# Defaults (can be overridden in docker-compose)
ENV HOST=0.0.0.0
ENV PORT=8088

EXPOSE 8088

CMD ["node", "main.js"]
