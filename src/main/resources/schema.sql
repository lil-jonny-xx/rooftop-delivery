-- RoofTop delivery schema.
-- Runs with MySQL, and with H2 started in MySQL compatibility mode for local use.

CREATE TABLE IF NOT EXISTS delivery_runs (
    run_id       BIGINT       NOT NULL AUTO_INCREMENT,
    driver_count INT          NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (run_id)
);

CREATE TABLE IF NOT EXISTS drivers (
    driver_id    BIGINT NOT NULL AUTO_INCREMENT,
    run_id       BIGINT NOT NULL,
    driver_index INT    NOT NULL,
    -- Minute this driver is next free. Status is derived from it, never stored.
    available_at INT    NOT NULL,
    PRIMARY KEY (driver_id),
    CONSTRAINT uk_driver_run_index UNIQUE (run_id, driver_index),
    CONSTRAINT fk_driver_run FOREIGN KEY (run_id) REFERENCES delivery_runs (run_id)
);

CREATE TABLE IF NOT EXISTS customer_orders (
    order_id       BIGINT      NOT NULL AUTO_INCREMENT,
    run_id         BIGINT      NOT NULL,
    customer_index INT         NOT NULL,
    order_time     INT         NOT NULL,
    travel_time    INT         NOT NULL,
    -- Null when no driver was free, which is the "No Food :-(" case.
    driver_id      BIGINT      NULL,
    status         VARCHAR(20) NOT NULL,
    PRIMARY KEY (order_id),
    CONSTRAINT uk_order_run_index UNIQUE (run_id, customer_index),
    CONSTRAINT fk_order_run FOREIGN KEY (run_id) REFERENCES delivery_runs (run_id),
    CONSTRAINT fk_order_driver FOREIGN KEY (driver_id) REFERENCES drivers (driver_id)
);
