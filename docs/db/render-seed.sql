-- Demo data for the Render PostgreSQL database.
-- Run this script manually when the Render database needs demo records.
-- It is idempotent at the demo-data level and does not require fixed IDs.

INSERT INTO supermarkets (name, description, website, country, active, created_at, updated_at)
SELECT 'Carrefour Argentina',
       'Cadena de supermercados con presencia nacional y promociones semanales.',
       'https://www.carrefour.com.ar',
       'Argentina',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM supermarkets
    WHERE name = 'Carrefour Argentina'
      AND country = 'Argentina'
      AND active = true
);

INSERT INTO supermarkets (name, description, website, country, active, created_at, updated_at)
SELECT 'Coto',
       'Supermercado argentino con foco en ofertas de alimentos, bebidas y limpieza.',
       'https://www.coto.com.ar',
       'Argentina',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM supermarkets
    WHERE name = 'Coto'
      AND country = 'Argentina'
      AND active = true
);

INSERT INTO supermarkets (name, description, website, country, active, created_at, updated_at)
SELECT 'Disco',
       'Cadena de supermercados del grupo Cencosud con promociones digitales.',
       'https://www.disco.com.ar',
       'Argentina',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM supermarkets
    WHERE name = 'Disco'
      AND country = 'Argentina'
      AND active = true
);

INSERT INTO supermarkets (name, description, website, country, active, created_at, updated_at)
SELECT 'Jumbo Argentina',
       'Hipermercado con catalogo amplio y descuentos por categoria.',
       'https://www.jumbo.com.ar',
       'Argentina',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM supermarkets
    WHERE name = 'Jumbo Argentina'
      AND country = 'Argentina'
      AND active = true
);

INSERT INTO supermarkets (name, description, website, country, active, created_at, updated_at)
SELECT 'Dia Argentina',
       'Cadena de cercania con ofertas frecuentes para compras cotidianas.',
       'https://diaonline.supermercadosdia.com.ar',
       'Argentina',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM supermarkets
    WHERE name = 'Dia Argentina'
      AND country = 'Argentina'
      AND active = true
);

INSERT INTO supermarkets (name, description, website, country, active, created_at, updated_at)
SELECT 'ChangoMas',
       'Supermercado argentino orientado a precios competitivos y promociones masivas.',
       'https://www.masonline.com.ar',
       'Argentina',
       true,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM supermarkets
    WHERE name = 'ChangoMas'
      AND country = 'Argentina'
      AND active = true
);

WITH target_supermarket AS (
    SELECT MIN(id) AS id
    FROM supermarkets
    WHERE name = 'Carrefour Argentina'
      AND country = 'Argentina'
      AND active = true
)
INSERT INTO offers (
    title, description, category, discount_type, discount_value, original_price, final_price,
    start_date, end_date, offer_source, active, supermarket_id, created_at, updated_at
)
SELECT '2x1 en gaseosas seleccionadas',
       'Promocion valida en marcas seleccionadas para compra en sucursal o tienda online.',
       'Bebidas',
       'TWO_FOR_ONE',
       NULL,
       NULL,
       NULL,
       CURRENT_DATE - 2,
       CURRENT_DATE + 5,
       'MANUAL',
       true,
       target_supermarket.id,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM target_supermarket
WHERE target_supermarket.id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM offers
      WHERE title = '2x1 en gaseosas seleccionadas'
        AND supermarket_id = target_supermarket.id
  );

WITH target_supermarket AS (
    SELECT MIN(id) AS id
    FROM supermarkets
    WHERE name = 'Coto'
      AND country = 'Argentina'
      AND active = true
)
INSERT INTO offers (
    title, description, category, discount_type, discount_value, original_price, final_price,
    start_date, end_date, offer_source, active, supermarket_id, created_at, updated_at
)
SELECT '25% de descuento en lacteos',
       'Descuento aplicado en caja sobre leches, yogures y quesos adheridos.',
       'Lacteos',
       'PERCENTAGE',
       25.00,
       NULL,
       NULL,
       CURRENT_DATE - 1,
       CURRENT_DATE + 3,
       'MANUAL',
       true,
       target_supermarket.id,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM target_supermarket
WHERE target_supermarket.id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM offers
      WHERE title = '25% de descuento en lacteos'
        AND supermarket_id = target_supermarket.id
  );

WITH target_supermarket AS (
    SELECT MIN(id) AS id
    FROM supermarkets
    WHERE name = 'Disco'
      AND country = 'Argentina'
      AND active = true
)
INSERT INTO offers (
    title, description, category, discount_type, discount_value, original_price, final_price,
    start_date, end_date, offer_source, active, supermarket_id, created_at, updated_at
)
SELECT 'Precio especial en yerba mate',
       'Oferta por unidad hasta agotar stock en yerbas seleccionadas.',
       'Almacen',
       'SPECIAL_PRICE',
       NULL,
       3500.00,
       2899.99,
       CURRENT_DATE,
       CURRENT_DATE + 7,
       'IMPORT',
       true,
       target_supermarket.id,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM target_supermarket
WHERE target_supermarket.id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM offers
      WHERE title = 'Precio especial en yerba mate'
        AND supermarket_id = target_supermarket.id
  );

WITH target_supermarket AS (
    SELECT MIN(id) AS id
    FROM supermarkets
    WHERE name = 'Jumbo Argentina'
      AND country = 'Argentina'
      AND active = true
)
INSERT INTO offers (
    title, description, category, discount_type, discount_value, original_price, final_price,
    start_date, end_date, offer_source, active, supermarket_id, created_at, updated_at
)
SELECT 'Promo bancaria en carnes',
       'Beneficio con bancos seleccionados para cortes frescos.',
       'Carnes',
       'BANK_PROMO',
       30.00,
       NULL,
       NULL,
       CURRENT_DATE + 2,
       CURRENT_DATE + 12,
       'API',
       true,
       target_supermarket.id,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM target_supermarket
WHERE target_supermarket.id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM offers
      WHERE title = 'Promo bancaria en carnes'
        AND supermarket_id = target_supermarket.id
  );

WITH target_supermarket AS (
    SELECT MIN(id) AS id
    FROM supermarkets
    WHERE name = 'Dia Argentina'
      AND country = 'Argentina'
      AND active = true
)
INSERT INTO offers (
    title, description, category, discount_type, discount_value, original_price, final_price,
    start_date, end_date, offer_source, active, supermarket_id, created_at, updated_at
)
SELECT '3x2 en productos de limpieza',
       'Lleva tres unidades y paga dos en productos adheridos de limpieza del hogar.',
       'Limpieza',
       'THREE_FOR_TWO',
       NULL,
       NULL,
       NULL,
       CURRENT_DATE + 5,
       CURRENT_DATE + 15,
       'SCRAPER',
       true,
       target_supermarket.id,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM target_supermarket
WHERE target_supermarket.id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM offers
      WHERE title = '3x2 en productos de limpieza'
        AND supermarket_id = target_supermarket.id
  );

WITH target_supermarket AS (
    SELECT MIN(id) AS id
    FROM supermarkets
    WHERE name = 'ChangoMas'
      AND country = 'Argentina'
      AND active = true
)
INSERT INTO offers (
    title, description, category, discount_type, discount_value, original_price, final_price,
    start_date, end_date, offer_source, active, supermarket_id, created_at, updated_at
)
SELECT 'Descuento fijo en arroz y fideos',
       'Descuento directo por unidad en productos basicos de almacen.',
       'Almacen',
       'FIXED_AMOUNT',
       500.00,
       2200.00,
       1700.00,
       CURRENT_DATE - 10,
       CURRENT_DATE - 1,
       'MANUAL',
       true,
       target_supermarket.id,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM target_supermarket
WHERE target_supermarket.id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM offers
      WHERE title = 'Descuento fijo en arroz y fideos'
        AND supermarket_id = target_supermarket.id
  );
