FROM node:24.12.0-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install -g pm2 && npm install

COPY . .

EXPOSE 8090 8091

CMD ["pm2-runtime", "start", "ecosystem.config.js"]