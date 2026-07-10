FROM node:20-alpine
WORKDIR /app

COPY package*.json ./

# 🌟 Bypass the corporate proxy SSL restriction safely inside the container
RUN npm config set strict-ssl false

RUN npm install
COPY . .
EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host"]