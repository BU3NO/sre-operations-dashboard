INSERT INTO monitored_service (name, description, environment, url, status, created_at, updated_at)
VALUES
    ('Customer API', 'API principal de clientes', 'PRODUCTION', 'https://customer.example.com/health', 'UP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Billing Service', 'Servico de faturamento', 'HOMOLOGATION', 'https://billing-hml.example.com/health', 'DEGRADED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Notification Worker', 'Worker de envio de notificacoes', 'DEVELOPMENT', 'http://localhost:9000/health', 'DOWN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
