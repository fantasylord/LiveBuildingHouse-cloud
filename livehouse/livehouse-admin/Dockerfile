ARG NODE_IMAGE=registry.cn-hangzhou.aliyuncs.com/library/node:20-alpine
ARG NGINX_IMAGE=registry.cn-hangzhou.aliyuncs.com/library/nginx:1.27-alpine

FROM ${NODE_IMAGE} AS build

WORKDIR /app

COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com \
    && npm ci

COPY . .
RUN npm run build

FROM ${NGINX_IMAGE}

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
