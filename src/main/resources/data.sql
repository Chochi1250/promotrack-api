INSERT INTO supermarkets (id, name, description, website, country, active, created_at, updated_at)
VALUES
  (1, 'Carrefour Argentina', 'Cadena de supermercados con presencia nacional.', 'https://www.carrefour.com.ar', 'Argentina', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'Coto', 'Supermercado argentino con ofertas frecuentes.', 'https://www.coto.com.ar', 'Argentina', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'Disco', 'Cadena de supermercados del grupo Cencosud.', 'https://www.disco.com.ar', 'Argentina', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO offers (
  id, title, description, category, discount_type, discount_value, original_price, final_price,
  start_date, end_date, offer_source, active, supermarket_id, created_at, updated_at
)
VALUES
  (1, '2x1 en gaseosas seleccionadas', 'Promocion valida en marcas seleccionadas.', 'Bebidas', 'TWO_FOR_ONE', null, null, null, CURRENT_DATE, DATEADD('DAY', 3, CURRENT_DATE), 'MANUAL', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '20% de descuento en lacteos', 'Descuento aplicado en caja sobre productos adheridos.', 'Lacteos', 'PERCENTAGE', 20.00, null, null, CURRENT_DATE, DATEADD('DAY', 7, CURRENT_DATE), 'MANUAL', true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'Precio especial en yerba mate', 'Oferta por unidad hasta agotar stock.', 'Almacen', 'SPECIAL_PRICE', null, 3500.00, 2899.99, DATEADD('DAY', 1, CURRENT_DATE), DATEADD('DAY', 10, CURRENT_DATE), 'IMPORT', true, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE supermarkets ALTER COLUMN id RESTART WITH 4;
ALTER TABLE offers ALTER COLUMN id RESTART WITH 4;
