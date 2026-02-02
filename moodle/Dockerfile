# Use the official PHP 8.4 image as a base
FROM php:8.4-apache

# Install necessary extensions, Apache mod_rewrite, Composer, PostgreSQL extension, OPcache, and cron
ARG DEBIAN_FRONTEND=noninteractive
ARG MOODLE_BRANCH=MOODLE_500_STABLE
ARG MOODLE_REPO=https://github.com/moodle/moodle.git

RUN apt-get update && \
    apt-get install -y --no-install-recommends unzip git curl ca-certificates libzip-dev libjpeg-dev libpng-dev \
    libfreetype6-dev libicu-dev libxml2-dev libpq-dev cron && \
    docker-php-ext-configure gd --with-freetype --with-jpeg && \
    docker-php-ext-install mysqli zip gd intl soap exif pgsql pdo_pgsql opcache && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    git clone --depth 1 --branch ${MOODLE_BRANCH} ${MOODLE_REPO} /tmp/moodle && \
    cp -a /tmp/moodle/. /var/www/html/ && \
    rm -rf /tmp/moodle

# Set PHP settings for Moodle: max_input_vars and OPcache
RUN echo "max_input_vars=5000" >> /usr/local/etc/php/conf.d/docker-php-moodle.ini && \
    echo "opcache.enable=1" >> /usr/local/etc/php/conf.d/docker-php-opcache.ini && \
    echo "opcache.enable_cli=1" >> /usr/local/etc/php/conf.d/docker-php-opcache.ini && \
    echo "opcache.memory_consumption=128" >> /usr/local/etc/php/conf.d/docker-php-opcache.ini && \
    echo "opcache.interned_strings_buffer=8" >> /usr/local/etc/php/conf.d/docker-php-opcache.ini && \
    echo "opcache.max_accelerated_files=10000" >> /usr/local/etc/php/conf.d/docker-php-opcache.ini && \
    echo "opcache.revalidate_freq=60" >> /usr/local/etc/php/conf.d/docker-php-opcache.ini && \
    echo "opcache.validate_timestamps=1" >> /usr/local/etc/php/conf.d/docker-php-opcache.ini

# Create moodledata directory
RUN mkdir -p /var/www/moodledata

# Set up cron job for Moodle
RUN echo "* * * * * www-data /usr/local/bin/php /var/www/html/admin/cli/cron.php > /dev/null" > /etc/cron.d/moodle-cron && \
    chmod 0644 /etc/cron.d/moodle-cron

# Set working directory
WORKDIR /var/www/html

# Set the correct permissions
RUN chown -R www-data:www-data /var/www/ && chmod -R 755 /var/www

# Copy entrypoint script to start both Apache and cron
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose port 80
EXPOSE 80

# Use entrypoint to start cron and Apache
ENTRYPOINT ["/entrypoint.sh"]
CMD ["apache2-foreground"]