# --- Stage 1: Asset Build (Compile GOV.UK Sass) ---
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# Compile GOV.UK Frontend Sass and copy required static JS/assets
RUN npx sass --load-path=node_modules src/scss/main.scss public/stylesheets/main.css --no-source-map
RUN mkdir -p public/assets public/javascripts && \
    cp -r node_modules/govuk-frontend/dist/govuk/assets/ public/assets/ && \
    cp node_modules/govuk-frontend/dist/govuk/govuk-frontend.min.js public/javascripts/

# --- Stage 2: Production Execution ---
FROM node:20-alpine
WORKDIR /app

# Install production dependencies only
COPY package*.json ./
RUN npm ci --only=production

# Copy compiled static assets, Nunjucks views, and app entrypoint
COPY --from=build /app/public ./public
COPY --from=build /app/views ./views
COPY --from=build /app/app.js ./

EXPOSE 3000

ENV PORT=3000
ENV KTOR_API_URL=http://backend.shortener.local:8080

CMD ["node", "app.js"]