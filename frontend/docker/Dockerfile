FROM nginx:1.25.3-alpine

EXPOSE 80

COPY --chmod=555 docker/40-create-runtime-env.sh /docker-entrypoint.d/
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY dist /usr/share/nginx/html
