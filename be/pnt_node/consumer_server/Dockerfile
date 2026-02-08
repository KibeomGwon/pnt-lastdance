FROM node:24.12.0-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

EXPOSE 8095

CMD ["npm", "run", "dev"]